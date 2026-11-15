# BSSC CGL 4 — Final Branding & UI Copy Pack

## Identity

**In-app name (requested):** BSSC CGL 4 Free 100 Mock  
**Suggested Google Play title until 100 verified papers are live:** BSSC CGL 4 Mock Tests  
**Hero tagline:** Future ASO Starts Here  
**Hindi tagline:** भविष्य के ASO की शुरुआत यहीं से  
**Supporting line:** Practice Today. Serve Bihar Tomorrow. / आज तैयारी, कल बिहार की सेवा।  
**Footer:** Made with ❤️ for Bihar / बिहार के लिए ❤️ से बनाया गया

The ASO theme is **aspirational**, not a selection guarantee. This independent app is not affiliated with BSSC or the Government of Bihar.

## Original app icon direction

Use the supplied original Bihar-shaped exam-prep emblem — book, graduation cap, growth star, and laurels on a deep navy-to-purple ground. **Do not use the official BSSC seal, the State Emblem of India or an unlicensed government insignia**. Avoid uniforms or nameplates that could suggest the app was issued by the government.

**Palette:** Deep navy `#131B4F`, violet `#6752DC`, sunrise gold `#F4AB27`, Bihar heritage maroon `#8C2844`, cream `#FFF8EC`, success green `#169B74`.
**Typography:** Clear, modern Latin + Devanagari fonts; prioritize legibility over decoration.
**Animation:** 200–350ms crossfades and slide transitions, gentle progress ring/trophy animations, reduced-motion support, no blinking distractions during timed exams.
**Hero artwork:** A fictional aspirant-to-administrative-officer journey, professional formal attire, paper files/desk, not a depicted real government official.

## Homepage copy

**English:** Hello, future officer! · Future ASO Starts Here · Practice today. Serve Bihar tomorrow. · Start a Mock Test · Explore Mock Tests · Official exam date awaited.
**Hindi:** नमस्ते, भावी अधिकारी! · भविष्य के ASO की शुरुआत यहीं से · आज तैयारी, कल बिहार की सेवा। · मॉक टेस्ट शुरू करें · सभी मॉक टेस्ट देखें · आधिकारिक परीक्षा तिथि की प्रतीक्षा है।

## Rotating motivation — use between sections, not as ad-like overlays

1. From aspirant to officer — one day at a time. / अभ्यर्थी से अधिकारी — हर दिन एक कदम आगे।
2. Practice today. Serve Bihar tomorrow. / आज तैयारी, कल बिहार की सेवा।
3. Your future is built by the practice you do today. / आपका भविष्य आज की मेहनत से बनता है।
4. Small improvements create big results. / छोटे-छोटे सुधार बड़े परिणाम लाते हैं।
5. Discipline turns dreams into progress. / अनुशासन सपनों को प्रगति में बदलता है।
6. Focus on progress, not perfection. / पूर्णता नहीं, प्रगति पर ध्यान दें।

## Dynamic Firebase promotional banner examples

| Theme | English title | Hindi title | CTA | Destination |
| --- | --- | --- | --- | --- |
| New mock | New Mock Test Available | नया मॉक टेस्ट उपलब्ध | Start Practice / अभ्यास शुरू करें | `mock_list` |
| Improve | Turn Weak Topics into Strengths | कमजोर विषयों को अपनी ताकत बनाएँ | View Analysis / विश्लेषण देखें | `insights` |
| Motivation | Future ASO Starts Here | भविष्य के ASO की शुरुआत यहीं से | Explore Tests / मॉक देखें | `mock_list` |

**Recommended Firestore fields:** `title.en`, `title.hi`, `subtitle.en`, `subtitle.hi`, `cta.en`, `cta.hi`, `imageUrl`, `destination`, `published`, `priority`, `startAt`, `endAt`. Open only allowlisted HTTPS links or verified app destinations; do not let arbitrary Firestore values invoke privileged actions.

## Notification content

- **New paper:** New Mock Available! / नया मॉक उपलब्ध है! — notify only after the paper is genuinely published.
- **Official notice:** Exam Notice Update / परीक्षा सूचना अपडेट — only after verifying the notice on the official BSSC website.
- **Gentle reminder:** A Little Practice Today? / आज थोड़ा अभ्यास करें? — send only to users who opt in.

Do not use unverified exam dates, fictitious rankings or promises of selection. Keep push opt-in separate from ad consent.

## Loading copy

- Preparing your next step toward your goal… / आपके लक्ष्य की ओर अगला कदम तैयार हो रहा है…
- Great preparation starts with a single question. / अच्छी तैयारी की शुरुआत एक सवाल से होती है।
- Loading your practice space. Stay focused! / आपका अभ्यास तैयार हो रहा है। ध्यान बनाए रखें!

## Play Store-ready copy

**Suggested Play Store title:** BSSC CGL 4 Mock Tests

**English short description:** BSSC CGL 4 practice mocks, exam timer, solutions and progress insights.

**Hindi short description:** BSSC CGL 4 मॉक टेस्ट, टाइमर, हल और प्रदर्शन विश्लेषण।

### Full description — English

Future ASO Starts Here.

Prepare for BSSC CGL 4 with a focused practice experience made for Bihar aspirants. Work through timed mock tests, review answers and track your progress across topics.

• Timed mock tests with exam-style navigation
• Answer review and explanations for available papers
• Subject-wise performance and weak-area insights
• Hindi and English navigation
• Practice history on your device
• Additional tests published through the online catalogue
• Optional rewarded ads to unlock selected practice papers
• Exam-related reminders when you opt in

You can start practicing without creating an account. Some features, including online rankings and newly published papers, require an internet connection and service availability. The number of available mocks can change as content is published.

Made with love for Bihar.

IMPORTANT: This is an independent exam-preparation app. It is not affiliated with or endorsed by the Bihar Staff Selection Commission (BSSC) or the Government of Bihar. Check the official BSSC website for authentic notices, eligibility, dates and recruitment details.

### Full description — Hindi

भविष्य के ASO की शुरुआत यहीं से।

BSSC CGL 4 की तैयारी करें एक ऐसे ऐप के साथ जो बिहार के अभ्यर्थियों के अभ्यास को ध्यान में रखकर बनाया गया है। समय-सीमा वाले मॉक टेस्ट दें, अपने उत्तर देखें और विषयवार प्रगति समझें।

• परीक्षा जैसा टाइमर और प्रश्न नेविगेशन
• उपलब्ध प्रश्नपत्रों के उत्तर और व्याख्या
• विषयवार प्रदर्शन तथा कमजोर क्षेत्रों का विश्लेषण
• हिंदी और अंग्रेज़ी इंटरफेस
• आपके डिवाइस पर अभ्यास का इतिहास
• ऑनलाइन कैटलॉग से नए मॉक प्रश्नपत्र
• चुनिंदा मॉक अनलॉक करने के लिए वैकल्पिक रिवॉर्ड विज्ञापन
• आपकी अनुमति से परीक्षा-संबंधी सूचनाएँ

शुरू करने के लिए अलग से खाता बनाने की जरूरत नहीं है। ऑनलाइन रैंकिंग और नए प्रश्नपत्र जैसी सुविधाएँ इंटरनेट तथा सेवा उपलब्धता पर निर्भर करती हैं। प्रकाशित मॉक की संख्या समय-समय पर बदल सकती है।

बिहार के लिए प्यार से बनाया गया।

महत्वपूर्ण: यह एक स्वतंत्र परीक्षा-तैयारी ऐप है। इसका बिहार कर्मचारी चयन आयोग (BSSC) या बिहार सरकार से कोई आधिकारिक संबंध या समर्थन नहीं है। आधिकारिक सूचना, पात्रता, तिथियों और भर्ती विवरण के लिए BSSC की आधिकारिक वेबसाइट देखें।

## Mandatory clarity notes

- The user-requested in-app name includes **“Free 100 Mock”**, but the current starter project has **four example papers**, not 100 reviewed papers. Do not claim 100 available in store descriptions/screenshots or marketing until all are real and published.
- Google Play limits app titles to 30 characters, short descriptions to 80 and full descriptions to 4,000. Its metadata guidance discourages promotional “Free” terms in titles. The suggested Play title avoids both that term and an unsupported 100-mock promise.
- Do not use a copied BSSC emblem, real government seal, official-looking badge, or misleading implied endorsement.
- Render a clearly visible *unofficial preparation app* disclaimer, including on your store listing.
- This pack is copy and image assets; it is **not** a compiled Android update. Integrate its localization strings, icon and banner copy into the existing project.
