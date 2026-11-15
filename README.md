> **Need an APK for your phone?** Follow [BUILD_AN_APK.md](BUILD_AN_APK.md). A GitHub Actions workflow can generate a debug APK with test ads, without needing Android Studio. This source ZIP is not itself an APK.

# BSSC CGL 4 Free 100 Mock — Android project (v0.5)

An animated, reference-matched purple-and-gold **Kotlin + Jetpack Compose** BSSC CGL 4 mock-exam Android app with the V5 visual design based on the two user-supplied phone mockups integrated as runtime resources. Independent preparation tool; **not an official BSSC/government app**.

## Deliverables

- Android Studio source with the **icon artwork cropped from the user-supplied concept poster as launcher icon** (`mipmap-*/ic_launcher.png`), app header icon (`drawable-nodpi/brand_icon.png`), and new aspirant-to-professional **officer-journey illustration** (`officer_journey.jpg`). The icon comes from the supplied user artwork; it is not represented as an official BSSC seal. Review trademark/logo rights and the Google Play impersonation policy before publishing.
- Brand-copy JSON integrated into the actual Android app: `app/src/main/assets/app_copy_en_hi.json`, read by `BrandCopy.kt`. Includes bilingual hero text, screen labels, rotating motivation, loading lines, banner samples and notification templates. The bottom of every non-exam screen displays a **Made with ❤️ for Bihar** footer and independent-app disclaimer.
- Motion: soft image breathing, rotating officer mindset quotes, animated startup/download lines and page transitions. Timed exams remain ad-free and animation-light.
- A **Hindi / English toggle fixed at the top-right of every app screen**, including an active exam, result, ranks and personal insights. Question language and key navigation text switch immediately; some secondary interface strings are still English and need final Hindi localization before claiming fully translated UI.
- **Admin-controlled clickable Firebase promo banners**, optional HTTPS artwork/CTA, publication periods, priority sorting, and multiple exam countdown cards that only show verified dates.
- **Firebase Cloud Messaging topic notifications**, opt-in via bell/profile, Android 13+ permission request, a notification channel, and a private admin-only send script. No student signup and no registration tokens stored in your Firestore. NOTE: active devices require a real Firebase project and notification permission.
- A dedicated no-ad focus exam, results and weak-area analytics. Result values come from actual user answers, not synthetic promotional rankings.
- **Dynamic Firebase Firestore content**: published mock catalogue (supports 1–100), questions loaded on demand, offline paper cache and version fields, and local bundled fallback for mocks 1–4.
- No signup or login page. Firestore uses **silent anonymous Firebase Authentication** for authenticated reads and leaderboard posting. No real name, email, or password required.
- 6 **implemented AdMob SDK formats**: anchored adaptive banner; inline native; opted-in rewarded for permanent per-device mock unlock; rewarded interstitial only with clear intro and skip option; frequency-capped interstitial after completed result; app-open on return from background (only when safe to show; never mid-exam).
- Google User Messaging Platform (UMP) consent flow and privacy-options control.
- Starter paper samples: **4 x 150 questions**, not 100 editorially reviewed papers. They contain some repeated concepts. The app **supports publishing 100**, but do not claim 100 completed live mocks until you have uploaded 100 distinct, reviewed papers.
- Source files: `firebase/firestore.rules`, `firebase/import_content.py` with validation, `app/src/main/assets/mock_01.json`–`mock_04.json`, optional leaderboard and data deletion, interactive HTML UI preview.

## Open with Android Studio

1. Install Android Studio, Android SDK **36**, JDK 17, and Gradle **8.11.1**.
2. Open the project root in Android Studio. The local package/namespace remains `com.bssc.cgl4.free100mock` internally; **visible app name is exactly `BSSC CGL 4 Free 100 Mock`**. The application ID is `com.bssc.cgl4.free100mock`. Confirm the ID is available and finalize before publishing; do not change it after first Play Store release.
3. Gradle wrapper config is supplied, but wrapper JAR/scripts are absent from this archive. Use an installed Gradle 8.11.1 or run `gradle wrapper --gradle-version 8.11.1` first.
4. Sync Gradle, run on an emulator or phone. The debug build uses **Google's demo ad IDs** and bundled sample papers even if Firebase is not connected. Actual ads are not guaranteed to fill.
5. Test, fix any SDK compatibility issues, then generate a signed `.aab` (`Build > Generate Signed Bundle / APK`) for Play Console.

**Verification limitation:** Source JSON and import validation were checked; the preview JS syntax and interactive navigation (home, Hindi toggle, ranks, profile and exam) were checked in a browser. **No APK/AAB was compiled or device-tested in this environment** (Android SDK/Gradle build toolchain unavailable). The cloud backend is **not deployed to your account**.

## 1. Connect your Firebase project (cloud content + rankings)

Create a Firebase project and register an Android application with your chosen app ID. In Firebase Console:

1. Enable **Authentication > Sign-in method > Anonymous**. Users won't see a login page.
2. Create **Cloud Firestore**. Deploy `firebase/firestore.rules` and `firebase/firestore.indexes.json` (e.g. `firebase use YOUR_PROJECT_ID` then `firebase deploy --only firestore:rules,firestore:indexes` from the project root). The published catalogue query uses `published == true` and order by `sortOrder`.
3. Add these keys to your machine's private **`~/.gradle/gradle.properties`** (not this project repository):

```properties
FIREBASE_API_KEY=your-web-or-android-api-key
FIREBASE_APP_ID=1:123456789:android:yourfirebaseappid
FIREBASE_PROJECT_ID=your-firebase-project-id
FIREBASE_SENDER_ID=your-numeric-firebase-cloud-messaging-sender-id
```

The app uses explicit `FirebaseOptions`, so this version does not require `google-services.json`. These are client configuration values, **not admin credentials**. **Never embed Firebase service-account credentials into the Android APK**. Cloud Messaging uses this sender ID and the default Firebase app; the Android app does not require `google-services.json` with these explicit options.

### Content schema

`mock_catalog/mock_001` (one document per test, up to `mock_100`):

```json
{
  "testId": 1,
  "title": "The Foundation",
  "subtitle": "Build exam-day confidence",
  "difficulty": "Balanced",
  "questionCount": 150,
  "durationMinutes": 135,
  "sortOrder": 1,
  "version": 1,
  "isFree": true,
  "published": true
}
```

`mock_papers/mock_001`: `{"questionsJson": "[ ... 150 Question objects JSON string ... ]", "version": 1}`.

Each Question object needs `id`, `section`, `topic`, `questionEn`, `questionHi`, four `optionsEn`, four `optionsHi`, `correctIndex` 0–3, `explanationEn`, and `explanationHi`. See bundled samples. Firestore documents are capped near 1 MiB; the import script checks a 950 KB JSON limit. If a paper grows beyond that, split it into paginated question documents and update the repository.

The Android catalogue query only loads published papers. **Whenever you revise a paper, increment `version`**; the client refreshes its disk cache on the next test launch. Users who have already completed a paper may have results computed against its older version; for production rankings use immutable exam version IDs.

### Upload papers without releasing a new app build

Set `GOOGLE_APPLICATION_CREDENTIALS` to a **private Firebase Admin SDK service account file outside this repo**, install the Python Admin SDK, and run from project root:

```bash
pip install firebase-admin
python firebase/import_content.py --content-dir app/src/main/assets
# Dry-run validates the 4 demo papers only.
python firebase/import_content.py --content-dir content/reviewed --execute
# Uploads as UNPUBLISHED drafts by default.
python firebase/import_content.py --content-dir content/reviewed --execute --publish
# Publishes only AFTER you have reviewed the papers.
```

`content/reviewed` can hold files `mock_001.json` to `mock_100.json` and an optional `catalog.json` with metadata overrides. Any count from 1–100 is supported. **The starter pack only has four papers; you must create/license and fact-check the other 96** before promising 100 live mocks.

## 1B. Control the clickable banner and exam countdown from Firebase

There are two new Firestore collections. The student app reads *active* `promo_banners` and *published* `exam_events`, and Firestore rules deny all client writes. Banners can link either to an allowlisted in-app route (`mock_list`, `insights`, `ranking`, `home`) or to a safe HTTPS URL; optional image URLs require HTTPS. An external URL is mandatory only for an external-action banner. The app hides a banner outside `startsAt` to `endsAt` and sorts active banners by numeric `priority`; currently the highest-priority banner is displayed on the home page. Date records can be added or changed at any time without publishing a new APK.

Example documents and their exact field names are in **`firebase/engagement.example.json`**. Edit the JSON or create documents in Firebase Console. Then deploy Firestore rules and upload via the Admin SDK:

```bash
pip install firebase-admin
python firebase/manage_engagement.py --file firebase/engagement.example.json
# Validates / previews writes, without connecting
python firebase/manage_engagement.py --file firebase/engagement.example.json --execute
# Upload after REVIEWING/ACTIVATING your banner/event entries
```

The V4 example includes **two active inspirational banners** (`aspirant`, `weak_topics`), and keeps `new_mock` inactive until a new reviewed paper is actually available. The Firestore admin script accepts both the old flat banner fields (`titleEn`, `descriptionHi`, etc.) and the new nested bilingual fields (`title.en`, `subtitle.hi`, `cta.en`, etc.). For in-app links, set `action_type: "internal"` and `action_value: "mock_list"`/`"insights"`/`"ranking"`/`"home"`. App reads are one-way; students cannot publish Firebase banners. The included `cgl4_prelims` event has `officialConfirmed: false` and `examAt: null`, so users see **Official date awaited**. Only enter an exam date after checking the official BSSC notice board: [BSSC Notice Board](https://bssc.bihar.gov.in/NoticeBoard.htm). Then set `examAt` to a timezone-aware ISO date-time string such as `YYYY-MM-DDTHH:MM:SS+05:30`, `officialConfirmed: true` and a `sourceUrl` on `https://bssc.bihar.gov.in/`. The Android app only shows countdown digits when all three conditions are met; otherwise it shows the date-awaiting state. Multiple published events are supported, with the three closest shown on the home card. **Do not use an invented date or imply it is official.**

The browser preview integrates the same icon, slogans, officer artwork, footer, and a **simulated** clickable Firebase banner and a "Simulate a date" control for design review. Those are not live Firebase data. Promo banners are editorial in-app links, *not* Google AdMob ad units and must not be misrepresented as those ads.

## 1C. Push notifications, no signup

Firebase Messaging uses topic **`bssc_cgl4_updates`**; only users who explicitly enable alerts subscribe. The app defaults FCM auto-init to off, requests `POST_NOTIFICATIONS` on Android 13+, and offers an opt-out in Profile. Notifications open the app when tapped. It does not request contact lists or user email. Firebase SDKs can process app-instance identifiers and device/network metadata, so disclose FCM in your hosted privacy policy and Google Play Data Safety form.

V4 also includes `--template` support in the admin sender, using the included bilingual branding copy:

```bash
python firebase/send_notification.py --template practice_reminder --language hi
python firebase/send_notification.py --template new_mock --language en
# Add --send only after content and audience have been verified.
```

You can send an opt-in notification from **Firebase Console > Messaging** by targeting topic `bssc_cgl4_updates`, or use the included admin script (server-only):

```bash
python firebase/send_notification.py --title "New mock released" --body "Try the newest full-length practice test"
# Dry-run only. Review message and audience before actually sending:
python firebase/send_notification.py --title "New mock released" --body "Try the newest full-length practice test" --send
```

`--send` requires the same private `GOOGLE_APPLICATION_CREDENTIALS` as the upload scripts; never include the service-account JSON inside the APK or publicly in this project. Configure Firebase Cloud Messaging in your project and test on a physical Android device with Play services. Device notification settings can override the app preference, and FCM delivery is not guaranteed. If a user denies Android permission, the app remains usable without alerts.

## 2. Connect your existing AdMob account

The debug source defaults to Google's published demo IDs. Create separate live ad units in your AdMob application for:

| Property in `~/.gradle/gradle.properties` | Format | Safe placement |
|---|---|---|
| `ADMOB_APP_ID` | app metadata ID | Manifest |
| `ADMOB_BANNER_ID` | anchored adaptive banner | bottom of home catalogue |
| `ADMOB_NATIVE_ID` | native | marked 'Ad · Sponsored' in home feed |
| `ADMOB_REWARDED_ID` | rewarded | explicit 'Watch to unlock' tap |
| `ADMOB_REWARDED_INTERSTITIAL_ID` | rewarded interstitial | optional quick unlock after intro + skip choice |
| `ADMOB_INTERSTITIAL_ID` | interstitial | after completed score screen, frequency limited |
| `ADMOB_APP_OPEN_ID` | app open | returning to home after 2 minutes away, not cold splash or active exam |

Example private properties:

```properties
ADMOB_APP_ID=ca-app-pub-YOURPUBLISHER~YOURAPP
ADMOB_BANNER_ID=ca-app-pub-YOURPUBLISHER/YOURBANNER
ADMOB_NATIVE_ID=ca-app-pub-YOURPUBLISHER/YOURNATIVE
ADMOB_REWARDED_ID=ca-app-pub-YOURPUBLISHER/YOURREWARDED
ADMOB_REWARDED_INTERSTITIAL_ID=ca-app-pub-YOURPUBLISHER/YOURQUICKREWARD
ADMOB_INTERSTITIAL_ID=ca-app-pub-YOURPUBLISHER/YOURINTERSTITIAL
ADMOB_APP_OPEN_ID=ca-app-pub-YOURPUBLISHER/YOURAPPOPEN
```

**Release Gradle tasks fail** if any live ad ID or Firebase configuration is missing, or a Google demo ad ID remains. AdMob availability and fills are never guaranteed. Configure UMP messages and applicable privacy and consent settings before launch. **Do not show any ad inside a 135-minute mock, interrupt answer selection, or reward if the SDK reward callback is not received.** Remember rewarded-unlocked tests are stored locally on each device: reinstalling or clearing data may reset unlocked access. Cloud-linked unlock entitlements and server-side verification (SSV) would be necessary for transferable entitlements and abuse resistance.

## 3. Ranking integrity and user privacy

Leaderboard uses Firebase Anonymous Auth with one best-score row per test per anonymous UID. **Rankings are REAL submissions, but client-graded and spoofable**. Before public competitive prizes, move grading and leaderboard submission to trusted Cloud Functions with App Check, rate limits, private correct answers, and immutable test versions. Rules restrict score shape, not whether a score is truthful.

Profile has optional nickname, language toggle, ad/privacy options, data deletion. Local app data is deleted only when cloud identity deletion succeeds if connected. Confirm your privacy policy includes anonymous identifiers, ads, consent, question downloads, ranking display names, retention and deletion. Complete Play Data safety; review `PRIVACY_POLICY_TEMPLATE.md` before hosting a live page.

## 4. Google Play release checklist

- Review 100 **distinct** exam-quality mocks and correct bilingual explanations before advertising 100 as available.
- Confirm the official BSSC syllabus, pattern and latest notification before release. No exam date is claimed here, and unknown dates intentionally display "awaited".
- Check the original prep-app crest (no BSSC government emblem or affiliation), create compliant feature graphic, screenshots, listing, support email, and public privacy-policy URL.
- Test on real Android devices: lifecycle/background timer, full 135-minute session, offline cache, paper version upgrades, slow network, consent refused/granted, ad fill failure, reward callbacks, deletion and Firestore rules.
- Supply Play 'Contains ads' disclosure, Data safety form, government affiliation disclaimer, age/content rating, store policies, and signing credentials. Confirm current target SDK and closed-testing requirements in Play Console.
- Publish a signed AAB under the final package name. Existing AdMob and Play accounts don't automatically connect to this source until IDs and signing are configured.

**Official docs:**
[Android AdMob test ads](https://developers.google.com/admob/android/test-ads) · [Rewarded interstitial intro requirement](https://developers.google.com/admob/android/rewarded-interstitial) · [Firestore Android offline caching](https://firebase.google.com/docs/firestore/manage-data/enable-offline) · [Firebase anonymous auth](https://firebase.google.com/docs/auth/android/anonymous-auth) · [Play Ads policy](https://support.google.com/googleplay/android-developer/answer/9857753) · [BSSC](https://bssc.bihar.gov.in/)

## V4 visual identity integration map

| Branding pack content | Native Android location | Preview |
| --- | --- | --- |
| Original Bihar preparation icon | `mipmap-*/ic_launcher.png`, `drawable-nodpi/brand_icon.png`, `AndroidManifest.xml`, `GlobalExamHeader()` | header and splash |
| Fictional officer-journey illustration | `drawable-nodpi/officer_journey.jpg`, `AnimatedStudyHero()`, `MotivationalLoading()` | home hero and splash |
| Future ASO Starts Here | `BrandCopy.tagline()` and home hero | hero |
| Made with ❤️ for Bihar | `BiharLoveFooter()` in common screen shell | bottom of non-exam pages |
| Hindi/English copies & rotating messages | `assets/app_copy_en_hi.json`, `BrandCopy.kt`, `OfficerMotivationCard()` | bilingual motivational card |
| Remotely controllable banners | `EngagementRepository`, `FirebasePromotion`, `firebase/engagement.example.json` | clearly marked demo banner |
| Branded notification messages | `firebase/send_notification.py --template` | notifications simulation |
| Accurate exam countdowns | `ExamCountdown()` and verified Firestore event fields | simulator explicitly marked demo |

**Editorial and release caveats:** Only four starter papers exist in this source; "100" reflects the requested in-app name and catalogue capacity, not 100 completed papers. The banner preview and Android project are not connected to your live Firebase/AdMob projects until your configurations are added. All 100 advertised mocks must exist before a Play release under a 100-mock promise. The candidate-to-officer illustrations are fictional aspirational art, not depictions of actual serving officials. Android APK/AAB compilation remains unverified without an Android SDK/Gradle environment.


## V5 reference-design implementation (added 15 Nov 2026)

The two supplied posters are **visual design references**. They are composite marketing images (not original exported Android screen assets), so the project reproduces their **layout, colors, hierarchy, emblem appearance and interaction patterns** in actual Compose components rather than placing static poster crops behind invisible buttons. Minor typography/illustration differences remain, and Android pixel density/font rendering will vary by device.

What changed from V4:

- Royal-purple status/header and five-tab navigation: **Home · Tests · Analysis · Rank · More**; gold active indicators; white/lavender content cards
- User-supplied branding-art crop installed in all launcher icon densities, the top bar and the branded startup experience. Treat it as a **concept/logo supplied by the app owner**, not as an official commission logo
- Compact future-ASO illustrated hero, exam date countdown with pending-date safeguards, optional live Firebase promotions, six colorful functional dashboard shortcuts
- Dedicated TestCatalogueScreen with compact paper rows, real published mocks and opt-in rewarded unlock controls; the HOME view shows featured real test records
- Exam answers styled as rounded pills and a mint-green selected state (**selection, not correctness feedback**); timed exam remains ad-free
- Trophy-led real results, detailed subject analysis and a live-data-only leaderboard podium; no fabricated ranks in production
- Motivational splash with real asset animation and persistent bilingual Bihar heart footer
- An interactive, standalone HTML preview that uses the **four bundled 150-question sample papers** for actual response selection and graded results. Preview ad unlocking is explicitly simulated; Android AdMob uses the configured SDK

The screenshot gallery and preview are included at the root (`V5_REFERENCE_SCREEN_GALLERY.png` and `preview.html`). The screenshot gallery illustrates a result using synthetic example answers solely for showcasing the design, **not a live exam/rank claim**. The production rank view remains blank until real Firebase records are loaded.

### Known gaps before release

1. This environment lacks the Android SDK/Gradle runtime; **no compiled APK/AAB has been tested**. Import into Android Studio, sync Gradle, run through all five navigation paths, check Unicode/Devanagari fonts on real handsets, and verify rewarded-ad callbacks/FCM.
2. Four sample papers are provided. The UI's "100" name describes planned capacity, **not 100 tested, reviewed available papers**; do not advertise 100 live exams before uploading them.
3. Connect your **own** AdMob identifiers and Firebase credentials, deploy rules, submit a real privacy policy and Play Console Data Safety declarations, and review branding for impersonation/trademark risk. There is no government affiliation.
4. An exam date is never guessed; the countdown stays in "official date awaited" until an admin enters a verified official date.
