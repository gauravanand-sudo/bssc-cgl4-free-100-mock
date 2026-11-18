# OneShotGS — native Kotlin Compose UI review

The Android app uses Jetpack Compose/Material3, not HTML (preview.html is only a mockup).
This preview branch has clean light surfaces, consistent corners, typography hierarchy and a compact white five-tab bar. Personalized Home, three concise quick actions and a modern mock catalogue header replace clutter. Firebase, OAuth, registration, AdMob placements, timed exams and release artifacts remain unchanged.

Do NOT publish this branch before Android device QA, screenshots on various screen sizes, registration/login/logout testing and explicit approval. Main remains the production preparation baseline.

Branding: the Android resource bssc_bilingual_logo.webp is now directly built from the publisher-approved OneShotGS BSSC CGL 4 artwork (256x256 source derivative), instead of the older 200-pixel image. Original PNG is retained by the publisher outside GitHub; re-export from master artwork for a future higher-density Play asset if needed.

## November 2026 closed-test candidate
Applied GitHub PR #3's remote Google login configuration identity fix directly to this modern Compose branch (StudentRegistration.kt). When an account has signed out, the fallback for a non-embedded OAuth Web Client ID uses CloudBackend.withIdentity before reading auth_public/google. The embedded client-ID path remains unchanged. Requires a real Play-installed device login/logout/relogin smoke test. Prepare the 14-day closed test using play-store/CLOSED_TEST_14_DAY_PLAN.md; no Play test has started.
