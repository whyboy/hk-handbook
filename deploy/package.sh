#!/usr/bin/env bash
set -euo pipefail
# Run from repository root after both builds succeed.
version=${1:?commit SHA required}
[[ $version =~ ^[0-9a-f]{40}$ ]] || exit 2
mkdir -p release
python3 - "$version" <<'PY'
import hashlib, pathlib, sys, zipfile
root = pathlib.Path('.')
jars = list(root.glob('backend/target/*.jar'))
assert len(jars) == 1, 'Expected one executable JAR'
assert (root / 'frontend/dist/build/h5/index.html').is_file()
with zipfile.ZipFile('release/release.zip', 'w', zipfile.ZIP_DEFLATED) as z:
    z.write(jars[0], 'app.jar')
    z.writestr('version.txt', sys.argv[1] + '\n')
    for p in sorted(root.glob('frontend/dist/build/h5/**/*')):
        if p.is_file():
            z.write(p, 'h5/' + str(p.relative_to('frontend/dist/build/h5')))
p = pathlib.Path('release/release.zip')
pathlib.Path('release/release.zip.sha256').write_text(hashlib.sha256(p.read_bytes()).hexdigest() + '  release.zip\n')
PY
