# V15 — Compact navigation, vibrant UI and your own image banners

## UI refinements
- Bottom bar is **56 dp** instead of ~83 dp; accessible 50 dp touch targets.
- Removed the always-visible footer below navigation; the independent-app attribution is still displayed under Profile.
- Smaller top bar and slightly more compact hero make room for content.
- Higher contrast purple/gold branding, darker text, stronger tags and more colorful Home shortcuts.
- Profile shows the actual app build version instead of hardcoded v0.4.
- AdMob banners are separated from buttons in white labeled cards; no ad shown inside timed exams.

## Configure clickable promotional images from Firebase

Go to Firebase Console → Firestore Database → `promo_banners`. Create a document such as `my_promo_001`:

```json
{
  "active": true,
  "priority": 40,
  "imageOnly": true,
  "imageUrl": "https://YOUR-PUBLIC-HOST/banner.png",
  "targetUrl": "https://YOUR-WEBSITE-OR-YOUTUBE-LINK",
  "titleEn": "My announcement",
  "titleHi": "मेरी सूचना",
  "buttonEn": "Visit",
  "buttonHi": "देखें"
}
```

All fields correspond to Firestore types: `active` and `imageOnly` are Boolean, `priority` is Number (integer), URLs and labels are String. The omitted `startsAt`/`endsAt` means always active. If dates are added, use Firestore **Timestamp** fields.

- Entire image is clickable, opening any public **HTTPS** link (YouTube, website, announcement, public landing page).
- `imageOnly=true` shows a full-width 16:9-ish image with a small clickable-link action label. You may omit the title and button (fallback text is supplied).
- `imageOnly=false` (or omitted) shows a strong purple banner with optional image thumbnail and bilingual text. Supply `titleEn`, `titleHi`, `descriptionEn`, `descriptionHi`, `buttonEn`, `buttonHi` or corresponding nested language maps.
- For internal destinations, set `action_type="internal"`, `action_value="mock_list"` (or `insights`, `ranking`, `home`) and omit `targetUrl`.
- Five highest-priority active banners appear in a swipeable carousel with a page counter and dots; Home refresh/restart reloads the active campaign list. URL changes need no new APK.
- Image URL must be a **public direct HTTPS image** (e.g. a public Firebase Storage download URL), not a private `gs://` path or Google Drive file preview page. Try 1200×675 px, less than 500 KB for good cellular performance.
- The example file `firebase/engagement.example.json` contains `custom_image_link`, **inactive** with `example.com` placeholders. Replace those before publication.
- The app allows normal HTTPS links you choose, not unsafe `file:`, `javascript:` or non-HTTPS schemes. Avoid using external links for selling in-app digital content outside Google Play's applicable billing rules.
- `promo_banners` are **your custom editorial cards**, separate from paid AdMob ad units. Custom promotions do not generate AdMob revenue on their own.

## Remaining production launch work
- This is a debug APK using Google test ads. Before earning revenue, publish a signed Android App Bundle on Google Play and complete AdMob app readiness and privacy steps.
- Only four starter papers are bundled; the remaining reviewed mocks must be uploaded/published, or the UI will not pretend they exist.
- Deploy current `firebase/firestore.rules` separately; signed-in clients read only active promotion documents and cannot write them.
- Ad rewards are stored locally, not verified by AdMob SSV. Review on multiple Android screen sizes and font scales before production release.
