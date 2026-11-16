# V28 — Stable registration and returning student login

**Create account:** student enters chosen name FIRST → Google authentication → required 10-digit Indian mobile field and optional separate WhatsApp opt-in → Firestore write success → Home. The entered name becomes the student display name used on Home and profile and future rankings.

**Already registered? Sign in with Google:** students with a complete profile (matching account email, saved valid name and mobile) go straight Home, without retyping their name or number. If an existing account is missing name/phone or has invalid data, the registration form requires those details before Home unlocks.

**Logout:** clear Firebase session, credential state and local progress so another student cannot see it. Auth-generation/UID guards ignore stale database callbacks. Startup auto-resume for fully registered sessions works. No public test data or fake registration is created.

**Layout:** exact OneShotGS logo centered independently of language control, compact centered card on short and tall screens.

Google OAuth and Firestore security rules must be deployed by Firebase owner; this debug APK uses Google sample ads only. Actual physical phone test still required.
