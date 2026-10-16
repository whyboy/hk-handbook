#!/usr/bin/env bash
# One-time installation, run as root after reviewing ports and existing services.
set -euo pipefail
[[ $EUID == 0 ]] || { echo 'Run with sudo'; exit 1; }
java_home=${1:?absolute path of installed JDK 21 required}
[[ "$java_home" == /* && -x "$java_home/bin/java" ]]
"$java_home/bin/java" -version 2>&1 | head -1 | grep -E 'version "21\.' >/dev/null
for tool in nginx python3 curl flock; do command -v "$tool" >/dev/null; done
source_dir=$(cd -- "$(dirname -- "$0")" && pwd)
# Fail rather than overwrite any existing installation.
for path in /srv/hk-handbook /etc/hk-handbook /etc/nginx/sites-available/hk-handbook /etc/nginx/sites-enabled/hk-handbook /etc/systemd/system/hk-handbook.service /opt/hk-handbook-java /usr/local/bin/hk-handbook-release /etc/sudoers.d/hk-handbook; do
    [[ ! -e "$path" && ! -L "$path" ]] || { echo "Already exists: $path"; exit 1; }
done
if ss -ltnH | awk '{print $4}' | grep -Eq ':(8000|9000)$'; then
    echo '8000 or 9000 is already in use'; exit 1
fi
getent passwd hk-deploy >/dev/null && { echo 'hk-deploy already exists'; exit 1; }
getent passwd hk-handbook >/dev/null && { echo 'hk-handbook already exists'; exit 1; }
useradd --create-home --shell /bin/bash hk-deploy
useradd --system --no-create-home --shell /usr/sbin/nologin --gid hk-deploy hk-handbook
install -d -o hk-deploy -g hk-deploy -m 755 /srv/hk-handbook /srv/hk-handbook/releases
install -d -o hk-deploy -g hk-deploy -m 700 /srv/hk-handbook/incoming /srv/hk-handbook/backups
install -d -o hk-handbook -g hk-deploy -m 2770 /srv/hk-handbook/data
install -d -m 755 /etc/hk-handbook
# Keep the runtime independent of the developer's home directory.
cp -a "$java_home" /opt/hk-handbook-java
chown -R root:root /opt/hk-handbook-java
cat > /etc/hk-handbook/runtime.env <<'ENV'
SERVER_ADDRESS=127.0.0.1
PORT=9000
DATABASE_URL=jdbc:sqlite:/srv/hk-handbook/data/handbook.db
ENV
chmod 600 /etc/hk-handbook/runtime.env
install -m 644 "$source_dir/hk-handbook.service" /etc/systemd/system/hk-handbook.service
install -m 755 "$source_dir/release.sh" /usr/local/bin/hk-handbook-release
cat > /etc/sudoers.d/hk-handbook <<'SUDO'
hk-deploy ALL=(root) NOPASSWD: /usr/bin/systemctl restart hk-handbook.service, /usr/bin/systemctl stop hk-handbook.service
SUDO
chmod 440 /etc/sudoers.d/hk-handbook
visudo -cf /etc/sudoers.d/hk-handbook
install -m 644 "$source_dir/nginx.conf" /etc/nginx/sites-available/hk-handbook
ln -s /etc/nginx/sites-available/hk-handbook /etc/nginx/sites-enabled/hk-handbook
if ! nginx -t; then
    rm /etc/nginx/sites-enabled/hk-handbook
    exit 1
fi
systemctl daemon-reload
systemctl enable hk-handbook.service
systemctl reload nginx
printf '%s\n' 'Server prepared. Install a dedicated deployment SSH public key for hk-deploy; then deploy a verified release.'
