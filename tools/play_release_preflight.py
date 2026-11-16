#!/usr/bin/env python3
"""Offline Play release source checks; live account/policy checks remain separate."""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]

def check() -> list[str]:
    errors = []
    gradle = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
    manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
    props = (ROOT / "gradle.properties").read_text(encoding="utf-8")
    for label, value in [
        ("package", 'applicationId = "com.bssc.cgl4.free100mock"'),
        ("target API", "targetSdk = 36"),
        ("version code", "versionCode = 30"),
        ("version name", 'versionName = "1.0.0"'),
        ("release signing", 'signingConfigs.getByName("playUpload")'),
    ]:
        if value not in gradle:
            errors.append("Missing expected %s: %s" % (label, value))
    if 'android:allowBackup="false"' not in manifest:
        errors.append("Android backup setting changed; review before release")
    if not re.search(r"ADMOB_APP_ID=ca-app-pub-\d{16}~\d{10}", props):
        errors.append("Production AdMob app ID missing")
    if not (ROOT / ".github/workflows/build-play-aab.yml").exists():
        errors.append("Signed AAB workflow missing")
    return errors

if __name__ == "__main__":
    if sys.argv[1:] != ["--source-check"]:
        print("Usage: play_release_preflight.py --source-check", file=sys.stderr)
        sys.exit(2)
    mistakes = check()
    for error in mistakes:
        print("FAIL:", error, file=sys.stderr)
    if mistakes:
        sys.exit(1)
    print("Source preflight PASS: API, package, version, AdMob and signing wiring.")
    print("NOT VERIFIED: OAuth, Firebase rules, signing secret, policy URLs, logo, device QA.")
