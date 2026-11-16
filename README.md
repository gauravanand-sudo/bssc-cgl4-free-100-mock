# BSSC CGL 4 Free 100 Mock · OneShotGS

**Kotlin / Jetpack Compose Android app · V19 (0.19.0)**

Independent exam-preparation application, part of **OneShotGS Learning Apps**. Not affiliated with or endorsed by the Bihar Staff Selection Commission or the Government of Bihar.

**For your phone:** [BUILD_AN_APK.md](BUILD_AN_APK.md) or the latest GitHub Actions **Build Android test APK** run. Debug APKs use Google's demo AdMob units, not live paid ads.

## App functionality

- Hindi and English question content and primary navigation; no registration page (Firebase Anonymous Authentication).
- Firebase-published mock catalogue with 150-question full-length papers; **Upcoming** cards visible but non-playable, controlled by `mock_catalog.status`.
- Four bundled offline starter papers, **not 100 completed validated papers**. A source audit identified repeated question texts that need editorial review before launch.
- Rewarded mock unlocks: 1–2 free; 3–50 one rewarded ad; 51–100 two rewarded ads. Locally saved unlocks are not backend-verified entitlements.
- Ad-free timed exams, 135 minutes, +4/−1; results, real attempt-based analytics, leaderboard prototype, streak and momentum cards.
- One promotional banner controlled from `promo_banners`, with a public HTTPS image and destination.
- **V19 dynamic Home cards** controlled from `home_cards`: horizontal swipeable sections or full-width stacked cards; bilingual text, icons/emojis, optional image, values, badges, themes, CTA links and scheduling. Cutoff cards are optional Firestore content — no fixed cutoff panel.
- Verified examination countdown from `exam_events` only when a real official date is confirmed.
- OneShotGS logo in splash/About and email support at **ruhionlabs@gmail.com**, with a **best-effort** resolution target of 30 minutes.
- Explicit independent-app / non-affiliation notice.

## Firebase controls

Read **[FIREBASE_DYNAMIC_HOME_V19.md](FIREBASE_DYNAMIC_HOME_V19.md)** for a field-by-field guide to creating horizontal Home cards, custom links and source-labeled cutoff estimates.

| Collection | What you control |
|---|---|
| `home_cards` | Optional Home rows, cutoffs, news, metrics, images, internal and HTTPS links |
| `promo_banners` | One highest-priority active promotional banner |
| `exam_events` | Official exam date countdown |
| `mock_catalog` | Published/mock Upcoming state, order, titles, difficulty |
| `mock_papers` | Reviewed 150-question bilingual paper JSON |

**Important:** You must deploy the latest [firebase/firestore.rules](firebase/firestore.rules) to the live Firebase Console. Changing these files in GitHub alone does **not** deploy Firestore rules or create live documents. The editor `firebase/manage_home_cards.py` runs dry by default; use `--execute` only with a private Firebase Admin credential.

Other Firebase scripts: `firebase/manage_engagement.py` (banner and exam date), `firebase/manage_mock_release.py` (Upcoming/available catalogue changes), `firebase/import_content.py` (reviewed mock upload).

## Build and QA

Android Studio / CI requirements: JDK 17, Android SDK 36, Gradle 8.11.1. Android application ID: `com.bssc.cgl4.free100mock`.

```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
python3 firebase/manage_engagement.py --file firebase/engagement.example.json
python3 firebase/manage_home_cards.py --file firebase/home_cards.example.json
python3 -m unittest discover -s firebase -p 'test_*.py'
```

CI also checks Firebase Android configuration, bilingual paper structure, released AdMob ID configuration and APK creation. These builds do not establish performance on every physical Android device.

## Before Google Play production release

A signed AAB and private upload key, store listing, truthful privacy/data-safety disclosures, device testing, content review, confirmed source citations for cutoff claims, AdMob approval and **trusted Firebase reward verification / entitlement checks** are still required. Available online papers are currently readable by anonymous-authenticated clients under Firestore rules even without server-side ad verification; do not claim anti-scraping or revenue guarantees.

This repository contains no private Firebase Admin service-account JSON.
