# V27 Login & logout stability fix

- Google account authentication alone is not considered completed signup.
- Every explicitly initiated Google sign-in requires name and mobile confirmation; returning users with a completed saved profile can resume on app restart without repeating the form.
- The exact student-typed name is written in `student_profiles/{uid}.displayName` and replaces a previous default Google name on explicit save.
- No Home access until the Firestore write confirms a valid saved name, mobile and email.
- Auth epoch and UID checks ignore stale reads/writes after signout/account switching.
- Logout immediately hides the student's app, signs out from Firebase and Credential Manager, clears local progress and identity.
- Login layout: approved logo independent and centered, with the language switch on its own row.
- Test APK ad placements from V25 remain unchanged. No ads during timed exam or signup.
- Use the signing SHA of the actual V27 APK in Firebase. Google OAuth and Firestore rules must be activated separately.

Test: Fresh signup name→Google→mobile→Save, reload, logout, sign in again (mobile confirmation), wrong phone, offline save, canceled Google picker.
