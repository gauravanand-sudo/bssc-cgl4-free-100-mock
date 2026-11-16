# V26 — Name once + centered login screen

## User experience
- At the start of registration, students type their chosen name (2–28 characters) before Google sign-in; we disclose that it appears on Home, Profile and public mock rankings.
- After Google sign-in, the number/consent page greets the student by the entered name. The saved name is written in private Firestore profile, not silently taken from Google identity.
- If an account is already Google-authenticated without a completed profile, name can be entered when finishing the registration (recovery path).
- Name is not solicited a second time in the Profile tab; only phone and opt-in editing remain.
- MainActivity restores the registered name from Firestore on every login/restart and stores it locally for Home greeting and mock leaderboard submissions.
- Saves and edits of existing profiles preserve the saved registration name. Existing V25 profiles remain readable.
- The welcome screen is re-laid out as a compact center-aligned vertical composition, max 430dp width, one primary card, scroll/IME support for short phones, simple motivational header and less filler.
- Existing AdMob placements and original OneShotGS resource reference are retained.

## Firebase
Firestore rules changed displayName validation to 2–28 characters (source change only; project owner must deploy new rules). Existing rules allow up to 100 chars, so V26 values still match unmodified V25 rules for creation; production deployment recommended.

## APK
Gradle version 0.26.0, version code 26; debug builds use Google demo ad IDs. Repackaging approved artwork may change the signing certificate; use one permanent production signing key eventually.
