# V4 Branding Integration — Implemented in Android source

**Brand name:** BSSC CGL 4 Free 100 Mock (Android app label unchanged)

**Original logo:** The branding-pack `app_icon_concept_512.png` is installed as 48–192px density launcher icons, a full-resolution header icon, and `PLAY_STORE_ICON_512.png` for store artwork. This is an independent Bihar-inspired design, **not an official BSSC logo**.

**Officer artwork:** The Android `AnimatedStudyHero` now displays a locally bundled original fictional aspirant-to-administrative-professional illustration. The image is retained offline and is not remotely downloaded.

**Language and motivation:** `BrandCopy.kt` reads the actual `assets/app_copy_en_hi.json` shipped with the app. `Future ASO Starts Here` and its Hindi copy appear in the hero; the homepage rotates eight English/Hindi officer-journey motivations; loading messages rotate using the branding pack; the top-right language toggle from V3 is retained on all routes.

**Footer:** `MainActivity` displays `BiharLoveFooter` at the bottom of every non-exam route, showing “Made with ❤️ for Bihar” / “बिहार के लिए ❤️ से बनाया गया” and an independent-app disclaimer. The footer is intentionally hidden during the timed exam to preserve focus.

**Firebase editorial banners:** `promo_banners` can carry either legacy flat copy or new nested `{title:{en,hi}, subtitle:{en,hi}, cta:{en,hi}}` fields. Actions are safe HTTPS links or allowlisted app destinations (`mock_list`, `insights`, `ranking`, `home`). `firebase/engagement.example.json` seeds two aspirational examples as active and keeps “new mock” draft inactive until content is published. Uploads require your private service-account credentials outside the source archive.

**Push messages:** `firebase/send_notification.py --template practice_reminder --language hi` pulls the bilingual text from the same branding copy file; `--send` remains explicitly required for a real push.

**Preview:** The self-contained interactive `preview.html` shows the same icon, officer artwork, brand copy, rotating motivation, footer and mocked banner/countdown; it is NOT connected live to Firebase/AdMob. The browser preview was smoke-tested on Chromium, including Hindi switching on home and on a running test.

**What is not represented as complete:** Only 4 sample papers (with repeated concepts) are bundled; 96 additional reviewed papers are not supplied. Android compilation/device testing and connecting your real AdMob, Firebase, FCM and Play developer accounts still require an Android Studio environment and your configuration. No official BSSC emblem, exam date or false rank is used.
