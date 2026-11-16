# Privacy Policy — publication preparation, not a final legal document

The repository PRIVACY_POLICY_TEMPLATE.md contains a draft. Do not publish it before the legal owner completes the following unknown details:

- Legal publishing entity: [PUBLISHER TO CONFIRM]
- Effective date: [PUBLISHER TO CONFIRM]
- Data-retention period and deletion request process: [PUBLISHER TO CONFIRM]
- Target user age and countries: [PUBLISHER TO CONFIRM]
- Official public HTTPS privacy-policy URL: [PUBLISHER TO CONFIRM]

Known app data flows:

- Google Auth is used to authenticate a Google identity, including Google email and account UID.
- The student provides a displayed name and Indian mobile number. The mobile is not OTP-verified.
- Student registration also stores WhatsApp opt-in flag (default OFF) and timestamps in Firestore.
- Nickname and user-submitted scores are shared on practice leaderboard pages; email and phone are not intentionally displayed there.
- Mock answers, results and rewarded-unlock credits are stored locally and not presently synchronized across devices.
- AdMob and Google UMP process consent, advertising identifiers, interaction and diagnostic data according to actual SDK behavior and user choices.
- Firebase Messaging may process push notification delivery information when student enables alerts.
- Private Firestore profile is intended to be accessible only to its Firebase owner and publisher administrators with privileged backend access.

Data Safety review must include first-party collection and SDK collection/sharing; review Google Ads SDK declarations. Do not mark “no data collected”.

Account deletion must be possible in-app and via a public HTTPS URL even after uninstall. The current deletion implementation needs live testing for Firebase reauthentication failures and cloud cleanup, so do not claim guaranteed automatic deletion before verification.

Independent exam-prep publisher; not affiliated with BSSC or the Government of Bihar.
Contact: ruhionlabs@gmail.com.
