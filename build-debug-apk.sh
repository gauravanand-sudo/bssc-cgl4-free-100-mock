#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if command -v gradle >/dev/null 2>&1; then
  gradle --no-daemon :app:assembleDebug
elif [ -f ./gradlew ] && [ -f ./gradle/wrapper/gradle-wrapper.jar ]; then
  chmod +x ./gradlew
  ./gradlew --no-daemon :app:assembleDebug
else
  echo "Gradle isn't installed and this archive does not include a Gradle wrapper binary." >&2
  echo "Use GitHub Actions (.github/workflows/build-debug-apk.yml) or install Gradle 8.11.1 and Android SDK 36." >&2
  exit 1
fi
printf '\nAPK: %s\n' "$PWD/app/build/outputs/apk/debug/app-debug.apk"
