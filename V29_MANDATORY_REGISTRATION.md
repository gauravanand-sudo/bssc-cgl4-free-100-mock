# V29 Mandatory registration and logout

- Removed the debug-only registration bypass; students cannot open Home, mocks, leaderboard or ad-bearing tabs without completing required registration, even in review APK.
- New student: enter chosen name, sign in Google, confirm valid Indian mobile, explicitly save in Firestore, then enter Home.
- Returning student: "Already registered? Sign in" restores profile only if email, name and phone are valid.
- Profile editor still allows phone/opt-in change; the student-entered name replaces legacy profile display names during explicit signup.
- Firebase login generation and UID guards prevent stale callbacks restoring old user after signout.
- Centered OneShotGS approved logo, English/Hindi toggle retained.
- This APK requires enabled Firebase Google Authentication, registered APK SHA and deployed Firestore security rules for a real login. No bypass. Debug AdMob test placements earn no revenue.
