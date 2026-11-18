# OneShotGS closed-test QA report — copy for each genuine tester

Do not include students' or testers' phone numbers, emails, OAuth tokens, IDs or other private records in GitHub issues.

**Tester reference (nonidentifying):** TESTER-___  
**Play-installed version / versionCode:** ___  
**Install source:** Google Play closed-test track (not sideloaded debug APK)  
**Android version and device model:** ___  
**Date and local time:** ___

**Choose what you exercised:** Registration, Returning login, Logout/relogin, Slow network, Profile/mobile, Hindi/English, Home/navigation, Test unlock/rewarded ad, Exam, Results, Insights, Rankings, Ads/consent, Account deletion.

**Steps to reproduce:** ___  
**Expected result:** ___  
**Actual result:** ___  
**Frequency:** Always / Sometimes / Once  
**Severity:** Blocker / High / Medium / Low  
**Screenshot or redacted screen recording:** ___  
**Suggested improvement:** ___

## Release acceptance gates

- [ ] Name entered once on new signup, saved to Firebase, and shown in Home/Profile/rankings.
- [ ] Mobile required on new signup and persisted; no OTP verification is claimed.
- [ ] Google login -> logout -> immediate Google login works without app restart.
- [ ] Registered returning user can sign in without re-registering.
- [ ] Incomplete registration remains blocked outside authenticated state.
- [ ] Offline/errors are recoverable and do not give phantom access.
- [ ] Logo, navigation, accessibility, scrolling and screen density visually checked.
- [ ] Ads load when consent/network permit; no ads during timed exam and no accidental ad-click traps.
- [ ] Account data deletion and public web deletion request work.
- [ ] No sensitive data appears in leaderboards or logs.
- [ ] Closed test Play App Signing certificate is registered in Firebase.
- [ ] Play Console reports enough opted-in testers and all policy declarations completed.

Production submission is a separate decision after tests and Google production-access approval.
