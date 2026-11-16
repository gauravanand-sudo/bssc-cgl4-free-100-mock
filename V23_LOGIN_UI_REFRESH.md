# V23 — Aspirational OneShotGS login UI

- Redesigned only onboarding presentation. Credential Manager, Firebase Auth, Firestore profile validation, phone normalization and WhatsApp opt-in callbacks are unchanged.
- Uses the publisher-approved \`R.drawable.bssc_bilingual_logo\` asset. No invented new logo and no extra "100 planned mocks" goal banner.
- Bilingual English/Hindi tagline, welcome message, motivational quotes, mobile form, privacy text; on-screen language switch works before login.
- Premium royal-purple gradient backdrop, soft ambient accents, gold highlights, clear 2-step progress, high-contrast Google CTA and telephone input.
- Small screens and keyboard supported via scroll/IME padding; review-only bypass remains debug-only.
- V23 versionCode=23, versionName 0.23.0. Debug build uses test ads.
- **Note**: Google Firebase OAuth provider, APK certificate SHA-1 and Firestore rules must be configured by the Firebase owner. A passing Android build does not imply activated login.
- **Asset caveat**: GitHub source contains the older compressed raster for \`bssc_bilingual_logo\`. Deliver a branded APK with the exact publisher uploaded image patched into this resource using an aligned, re-signed APK. Do not issue an unpatched GitHub workflow APK as the approved-logo build.
