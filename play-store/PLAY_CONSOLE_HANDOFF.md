# OneShotGS BSSC CGL 4 · first Play test release

Package `com.bssc.cgl4.free100mock` · version code 30 · version 1.0.0

## Console entries for publisher review

| Form | Entry or evidence needed |
| --- | --- |
| Store listing | Use STORE_LISTING.md. Supply the approved 512 px icon, feature graphic and genuine phone screenshots from the candidate. |
| Ads | Select “Yes, my app contains ads”. The production configuration includes banner, rewarded and interstitial formats. |
| App access | Registration gates the app. Supply working, reusable reviewer access instructions and a dedicated review account through the private Console form. Demonstrate a sign-in path independent of personal-device approvals. Check access to ad-gated features too. |
| Account creation and deletion | Google account sign-in followed by name and self-entered Indian phone number. Provide the verified public deletion-request URL and the in-app deletion route. |
| Data safety | Review name, email, phone, UID, WhatsApp opt-in, public leaderboard name/scores and timestamps. Distinguish cloud submissions from attempts and unlocks held on-device. Assess approximate location inferred from IP, device identifiers, app interactions and diagnostics from Google Mobile Ads and identifiers used for messaging. Confirm collection, sharing, purpose and optionality separately for each category against the release SDKs and actual publisher usage. |
| Target audience, rating and distribution | Publisher to confirm intended age bands, countries and rating questionnaire answers. App content is independent exam practice. |
| Support | ruhionlabs@gmail.com |
| Privacy | Enter a publicly reachable HTTPS privacy page and link it in the app. Verify both privacy and deletion pages while signed out. |

Google's SDK guidance: https://developers.google.com/admob/android/privacy/play-data-disclosure and https://firebase.google.com/docs/android/play-data-disclosure

## Track and acceptance evidence

Start with an internal track for the signed AAB. From Play App Signing copy the **app-signing certificate** SHA-1 and SHA-256 into Firebase for this exact package. Install via the Play opt-in link to exercise Play's signature. Determine closed-testing eligibility from the publisher's Play dashboard.

All following results are **pending**. Record the build, device, Android version, date and observed outcome for each run. Use disposable accounts. Keep email, phone, credentials and tokens out of screenshots and issue logs.

| Check | Pass evidence |
| --- | --- |
| First registration | Complete name, Google sign-in and phone entry from the Play install. Verify one Firestore student_profiles document keyed to that test UID, correct opt-in, and phoneVerified false. |
| Returning account and logout | Reopen and check the saved profile. Sign out, sign in again, then switch accounts. The second account should see its own profile and local attempt history. |
| Account deletion | With disposable data and a leaderboard entry present, exercise the confirmation and any reauthentication prompt. Verify profile, leaderboard entry and Firebase Auth identity are gone and local data cleared. Repeat after an interrupted connection to check recovery. |
| Ads and consent | Use an AdMob test device. Exercise consent, banner load, interstitial boundaries, rewarded completion, cancellation and no-fill. Confirm credit is granted once on completion, and questions stay ad-free during timed attempts. Avoid clicks on live ads. |
| External deletion path | Open the deletion URL without signing in or installing the app. Send a disposable test request and verify ownership checks and actual handling before claiming success. |

Capture real test evidence and review the Play pre-launch report before requesting production approval.
