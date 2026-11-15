# Build a test APK for your Android phone

The repository contains **Android source code, not a precompiled APK**. The included workflow builds a **debug APK** signed by the Android debug key. It is installable on an Android phone but **not a Google Play release**.

## Easiest option: Build online using GitHub Actions (no Android Studio needed)

1. Create a **private** GitHub repository for your project. Don't upload any Firebase service-account JSON files or private AdMob credentials.
2. Unzip this archive and **upload the CONTENTS of the `BSSC-CGL4-Free-100-Mock` folder to the repository root**, including its hidden `.github` folder. `app/`, `build.gradle.kts`, and `.github/workflows/build-debug-apk.yml` must all be at the top level.
3. Open the repository's **Actions** tab and choose **Build Android test APK**. Use **Run workflow** if the workflow hasn't run on push.
4. When the build succeeds, open that workflow run, scroll down to **Artifacts** and select **BSSC-CGL4-Free-100-Mock-DEBUG-APK**.
5. Extract the downloaded artifact ZIP. It will contain `app-debug.apk`. Send the APK to your Android phone and tap it to install. If asked, enable **Install unknown apps** for your file manager/browser.

If GitHub Actions reports build errors, copy the relevant log lines. This source project has **not yet been Android-compiled or device-tested**, so source-level errors may need fixing.

## Build locally using Android Studio

1. Install the latest Android Studio, JDK 17, Android SDK platform 36, build-tools 36.0.0, and Gradle 8.11.1.
2. Open the unzipped `BSSC-CGL4-Free-100-Mock` project folder (not the ZIP) in Android Studio.
3. This project does not ship a Gradle wrapper JAR/scripts. If Android Studio cannot use its installed Gradle, install **Gradle 8.11.1** and run `gradle wrapper --gradle-version 8.11.1` in a terminal inside the project. Then resync.
4. Select the **debug** build variant and use **Build > Build Bundle(s) / APK(s) > Build APK(s)**, or run `gradle :app:assembleDebug` in Terminal.
5. Find the result in `app/build/outputs/apk/debug/app-debug.apk`.

## Debug build behavior

- Uses **Google-provided AdMob sample/test IDs**, not your personal IDs.
- The four bundled starter papers are available as offline sample content; live catalogue and ranking require your Firebase project configuration.
- No signup screen; online Firebase functions are not guaranteed without Firebase credentials.
- The countdown remains 'official date awaited' without verified cloud data.
- This isn't ready for public release or a signed `.aab`. Test all mock screens, crashes, back navigation, notifications, ad placements and performance on a real device.

**App name:** BSSC CGL 4 Free 100 Mock  
**Source version:** V5, reference UI  
**Application ID:** `com.bssc.cgl4.free100mock`  
**APK output:** `app/build/outputs/apk/debug/app-debug.apk`

**Independence:** The app is not affiliated with BSSC or the Government of Bihar.
