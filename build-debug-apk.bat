@echo off
cd /d "%~dp0"
where gradle >nul 2>nul
if errorlevel 1 (
  if exist gradlew.bat if exist gradle\wrapper\gradle-wrapper.jar (
    call gradlew.bat --no-daemon :app:assembleDebug
  ) else (
    echo Gradle was not found. Use the GitHub Actions build or install Gradle 8.11.1 and Android SDK 36.
    exit /b 1
  )
) else (
  call gradle --no-daemon :app:assembleDebug
)
if errorlevel 1 exit /b 1
echo APK: %CD%\app\build\outputs\apk\debug\app-debug.apk
