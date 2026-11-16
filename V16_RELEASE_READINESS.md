# V16 premium experience — shipped UI vs. remaining release blockers

## Implemented and built in Android source
- Animated, true-to-record Home stats: best score, total distinct mocks, overall answered-question accuracy and calendar-day streak.
- Daily single-mock mission; energetic purple/gold hero and compact floating bottom navigation.
- Rich mock catalogue cards with real score, difficulty, and 1-of-2 rewarded-ad progress.
- Bilingual pre-exam briefing; confirms timer, negative marking and explicit exam start.
- More graphical ad-free examination screen with live timer, animated options, answered progress, adaptive question palette.
- Post-exam celebration with one-time particles, animated score ring, correct/wrong/skipped, subject donuts, comparison with the last older attempt (if any).
- Graphical analytics with real score trend, real mean completion time, topic weaknesses and subject donuts.
- More expressive leaderboard and profile; publisher remains explicitly unaffiliated with official BSSC.
- Custom Firebase image-only banners can link to your own public HTTPS URL; carousel gently auto-rotates when Home visible.
- More space through compact navigation and removing the fixed attribution footer.
- AdMob remains separated from navigation and exam questions, and is demo-only in debug.
- Existing access: mock 1–2 free; 3–50 one voluntary earned ad; 51–100 two voluntary earned ads; credits saved per mock.

## Validated by CI
Gradle Android debug compile, unit tests for MockAccess and PrepMomentum, Firebase Android project configuration, Firestore banner-example dry-run and production AdMob manifest processing.

## Still required for real production launch
- Real-device QA across screen sizes, TalkBack, 200% font scaling, Hindi/English and low-end devices.
- Signed Android App Bundle/upload key, Google Play listing, test track, app privacy policy/Data Safety/Ads declarations and AdMob app-readiness process.
- Upload and review full bilingual mock catalogue; only four offline papers currently bundled in this source. Do not claim all 100 papers are published.
- Live Firebase Firestore rules must be deployed (a GitHub commit alone does not deploy rules).
- Rewarded completion remains locally persisted; anonymous Firebase content reads are NOT protected by AdMob SSV/verified backend entitlements.
- Rankings remain client-submitted and not anti-cheat verified.
- Custom external links need Play policy review, privacy checks and no misleading payment funnel.
- Do not click real ads or incentivize ad clicks. Do not introduce ads while the exam timer runs.

This is a substantial aesthetic/product polish pass, not a claim of guaranteed revenue, certification or complete production readiness.
