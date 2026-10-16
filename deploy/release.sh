#!/usr/bin/env bash
set -Eeuo pipefail
umask 022
root=/srv/hk-handbook
version=${1:?commit SHA required}
[[ $version =~ ^[0-9a-f]{40}$ ]] || exit 2
exec 9>"$root/deploy.lock"
flock -n 9 || { echo 'Another deployment is running'; exit 1; }
cd "$root/incoming/$version"
sha256sum --check release.zip.sha256
release="$root/releases/$version-$(date -u +%Y%m%dT%H%M%S)-$$"
mkdir "$release"
python3 - "$release" "$version" <<'PY'
import pathlib, sys, zipfile
out = pathlib.Path(sys.argv[1])
with zipfile.ZipFile('release.zip') as z:
    names = z.namelist()
    assert len(names) == len(set(names)), 'Duplicate files'
    assert {'app.jar', 'version.txt', 'h5/index.html'} <= set(names), 'Incomplete release'
    assert z.read('version.txt').decode().strip() == sys.argv[2], 'Version mismatch'
    assert sum(i.file_size for i in z.infolist()) < 1024**3, 'Release too large'
    for i in z.infolist():
        p = pathlib.PurePosixPath(i.filename)
        assert not p.is_absolute() and '..' not in p.parts, 'Unsafe archive path'
        assert i.filename in ('app.jar', 'version.txt') or i.filename.startswith('h5/'), 'Unexpected file'
        assert (i.external_attr >> 16) & 0o170000 in (0, 0o100000, 0o040000), 'Links are forbidden'
    z.extractall(out)
PY
chmod -R u=rwX,go=rX "$release"
previous=
if [[ -L "$root/current" ]]; then
    previous=$(readlink -f "$root/current")
fi
switched=false
stopped=false
healthy() {
    local attempt
    for attempt in {1..30}; do
        if curl --fail --silent --max-time 3 http://127.0.0.1:9000/api/health | python3 -c 'import json,sys; assert json.load(sys.stdin)["status"] == "UP"' 2>/dev/null; then
            return 0
        fi
        sleep 2
    done
    return 1
}
rollback() {
    local code=$?
    trap - EXIT
    if (( code != 0 )) && { $stopped || $switched; }; then
        echo 'Deployment failed; restoring previous application (database is preserved).'
        if [[ -n "$previous" && -d "$previous" ]]; then
            ln -sfn "$previous" "$root/current.next"
            mv -Tf "$root/current.next" "$root/current"
            sudo -n /usr/bin/systemctl restart hk-handbook.service
            if healthy; then echo 'Previous backend is healthy'; else echo 'ROLLBACK HEALTH CHECK FAILED'; fi
        else
            sudo -n /usr/bin/systemctl stop hk-handbook.service
            rm -f "$root/current"
            echo 'First deployment failed; service stopped.'
        fi
    fi
    exit "$code"
}
trap rollback EXIT
trap 'exit 130' INT
trap 'exit 143' TERM
trap 'exit 129' HUP
# Stop writes before backup and version change. This single-instance deployment has downtime.
stopped=true
sudo -n /usr/bin/systemctl stop hk-handbook.service
if [[ -f "$root/data/handbook.db" ]]; then
    python3 - "$root/data/handbook.db" "$root/backups/$(basename "$release").db" <<'PY'
import sqlite3, sys
with sqlite3.connect(sys.argv[1]) as source, sqlite3.connect(sys.argv[2]) as dest:
    source.backup(dest)
    assert dest.execute('PRAGMA integrity_check').fetchone()[0] == 'ok'
PY
    chmod 600 "$root/backups/$(basename "$release").db"
fi
ln -sfn "$release" "$root/current.next"
mv -Tf "$root/current.next" "$root/current"
switched=true
sudo -n /usr/bin/systemctl restart hk-handbook.service
healthy
index_check=$(mktemp)
curl --fail --silent --max-time 5 http://127.0.0.1:8000/ -o "$index_check"
cmp "$index_check" "$release/h5/index.html"
rm "$index_check"
curl --fail --silent --max-time 5 http://127.0.0.1:8000/api/health | python3 -c 'import json,sys; assert json.load(sys.stdin)["status"] == "UP"'
printf '%s %s\n' "$(date -u +%FT%TZ)" "$version" >> "$root/deployments.log"
echo "Deployed $version"
