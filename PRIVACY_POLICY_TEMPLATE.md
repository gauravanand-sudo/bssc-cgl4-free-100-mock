# Draft privacy policy — BSSC CGL 4 Free 100 Mock

> **TEMPLATE ONLY — COMPLETE THE BLANKS, VERIFY THE SDK BEHAVIOUR, SEEK APPROPRIATE LEGAL REVIEW, AND HOST AT A PUBLIC HTTPS URL BEFORE PUBLISHING.**

Last updated: [DATE]

BSSC CGL 4 Free 100 Mock is independently developed by [PUBLISHER LEGAL NAME], reachable at [CONTACT EMAIL]. It is not affiliated with or endorsed by Bihar Staff Selection Commission.

## Information stored locally

The app stores your nickname, language preference, exam answers, marked questions, unlocked mock IDs, cached question papers, downloaded test metadata, and attempt results on your device. The app uses Android local preferences for these records. Device backups are disabled by the app manifest. Uninstalling normally deletes this local app data.

## Online leaderboard data (if enabled)

If Firestore is enabled, Firebase Authentication silently creates a pseudonymous anonymous user ID (there is no signup screen). The ID is also used to request published test content and, if enabled, to participate in rankings. Cloud Firestore stores that ID, your chosen display name/nickname, best score for each mock, correct-answer count and submission time. Leaderboard names and results may be visible to other app users. This data is used for ranking and app functionality.

## Ads and third-party services

Google AdMob may process advertising identifiers, device and interaction information as described by Google's privacy practices. The app supports adaptive banners, inline native ads, optional rewarded and rewarded-interstitial unlock ads, frequency-capped interstitials at natural post-result breaks, and app-open ads when returning to the home screen. Timed exams contain no advertising. Native ads are visibly identified as sponsored. A rewarded video grants content only after an SDK reward callback. Where applicable, Google User Messaging Platform obtains privacy choices. See Google's privacy information: https://policies.google.com/privacy.

## Optional exam alerts and controlled banners

If you opt into alerts, Firebase Cloud Messaging may create an app-instance messaging token and use it to subscribe this installation to the exam-update topic. The token can be used to deliver notifications about exam announcements and new mock tests; this app does not save the FCM token in its Firestore database or ask for an email address. Android 13+ asks for system notification permission. You can switch off notifications in Profile or device settings. Google Firebase may process information needed to deliver messages; see https://firebase.google.com/support/privacy .

The app may show promotional banners supplied by the publisher through Firebase Firestore. Tapping an active banner opens an external HTTPS page. That site may have its own privacy practices. A banner is separate from AdMob ads. The publisher can update the banner title, image, link or its active period without releasing a new version of the app. Exam countdown dates are drawn from publisher-managed Firestore documents, and are shown as confirmed only when linked to official BSSC notices.

## Retention and deletion

Local exam results remain until the user clears app data or uninstalls. Optional Firebase leaderboard data persists according to [DEFINE YOUR ACTUAL RETENTION POLICY]. You can choose **Profile > Delete app & ranking data** to delete local results and your optional Firebase anonymous leaderboard account and scores. This action also clears downloaded paper/cache files and local catalogue metadata after successful cloud identity deletion, where configured. When online rankings are enabled, this requires internet access; if cloud deletion fails, the app keeps data so you can retry. You may also request assistance from [CONTACT EMAIL]. [VERIFY ACTUAL BACKEND DATA DELETION AND APPLICABLE GOOGLE PLAY REQUIREMENTS BEFORE RELEASE.]

## Data security

Data is protected through platform and cloud security features but no internet service offers absolute security. Cloud rules restrict writes to authenticated user rows; client-side exam scores in this prototype are not independently verified.

## Children

The app is intended for competitive exam aspirants, not specifically for children. [DEFINE THE ACTUAL TARGET AUDIENCE AND AD CONTENT RATING.]

## Contact

For privacy questions and data deletion requests: [CONTACT EMAIL].

## Changes

We may update this policy and will publish the effective date and new policy at [PUBLIC URL].
