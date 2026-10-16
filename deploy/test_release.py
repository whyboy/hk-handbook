"""Exercise release, backup, rejected archives and rollback without touching system services."""
import hashlib
import os
from pathlib import Path
import sqlite3
import subprocess
import tempfile
import unittest
import zipfile


class ReleaseTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        for name in ('incoming', 'releases', 'data', 'backups', 'bin'):
            (self.root / name).mkdir()
        self.script = self.root / 'release.sh'
        self.script.write_text(Path(__file__).with_name('release.sh').read_text().replace(
            'root=/srv/hk-handbook', f'root={self.root}'))
        self.env = dict(os.environ, TEST_ROOT=str(self.root),
                        PATH=str(self.root / 'bin') + ':' + os.environ['PATH'])
        self.mock('sudo', '#!/bin/sh\nprintf "%s\\n" "$*" >> "$TEST_ROOT/service.log"\n')
        self.mock('sleep', '#!/bin/sh\nexit 0\n')
        self.mock('curl', '''#!/usr/bin/env python3
import os, pathlib, sys
root = pathlib.Path(os.environ['TEST_ROOT'])
current = root / 'current'
if (current / 'app.jar').read_text() == 'broken':
    sys.exit(22)
if '-o' in sys.argv:
    pathlib.Path(sys.argv[sys.argv.index('-o') + 1]).write_bytes((current / 'h5/index.html').read_bytes())
else:
    print('{"status":"UP"}')
''')
        with sqlite3.connect(self.root / 'data/handbook.db') as c:
            c.execute('create table retained (value text)')
            c.execute("insert into retained values ('keep me')")

    def mock(self, name, content):
        p = self.root / 'bin' / name
        p.write_text(content)
        p.chmod(0o755)

    def package(self, sha, broken=False, unsafe=False):
        folder = self.root / 'incoming' / sha
        folder.mkdir()
        bundle = folder / 'release.zip'
        with zipfile.ZipFile(bundle, 'w') as z:
            z.writestr('app.jar', 'broken' if broken else 'working')
            z.writestr('h5/index.html', '<html>hello</html>')
            z.writestr('version.txt', sha + '\n')
            if unsafe:
                z.writestr('../escaped', 'bad')
        (folder / 'release.zip.sha256').write_text(hashlib.sha256(bundle.read_bytes()).hexdigest() + '  release.zip\n')

    def run_release(self, sha):
        return subprocess.run(['bash', str(self.script), sha], env=self.env,
                              capture_output=True, text=True, timeout=15)

    def test_release_backup_and_failed_upgrade_rollback(self):
        self.package('a' * 40)
        result = self.run_release('a' * 40)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        previous = (self.root / 'current').resolve()
        backup = next((self.root / 'backups').glob('*.db'))
        with sqlite3.connect(backup) as c:
            self.assertEqual(c.execute('select value from retained').fetchone()[0], 'keep me')
        self.package('b' * 40, broken=True)
        result = self.run_release('b' * 40)
        self.assertNotEqual(result.returncode, 0)
        self.assertEqual((self.root / 'current').resolve(), previous)
        self.assertIn('Previous backend is healthy', result.stdout)
        with sqlite3.connect(self.root / 'data/handbook.db') as c:
            self.assertEqual(c.execute('select value from retained').fetchone()[0], 'keep me')

    def test_first_release_failure_stops_service(self):
        self.package('c' * 40, broken=True)
        result = self.run_release('c' * 40)
        self.assertNotEqual(result.returncode, 0)
        self.assertFalse((self.root / 'current').exists())
        self.assertIn('First deployment failed; service stopped.', result.stdout)

    def test_path_traversal_rejected_before_service_stop(self):
        self.package('d' * 40, unsafe=True)
        result = self.run_release('d' * 40)
        self.assertNotEqual(result.returncode, 0)
        self.assertFalse((self.root / 'service.log').exists())
        self.assertFalse((self.root / 'escaped').exists())

    def test_checksum_rejected_before_service_stop(self):
        sha = 'e' * 40
        self.package(sha)
        with (self.root / 'incoming' / sha / 'release.zip').open('ab') as f:
            f.write(b'tampered')
        result = self.run_release(sha)
        self.assertNotEqual(result.returncode, 0)
        self.assertFalse((self.root / 'service.log').exists())


if __name__ == '__main__':
    unittest.main()
