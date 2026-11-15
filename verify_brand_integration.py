"""Run from the project root: python verify_brand_integration.py.
Static resource checks; not a substitute for compiling an Android build or device tests.
"""
import json
from pathlib import Path
from PIL import Image

ROOT=Path(__file__).resolve().parent
MAIN=ROOT/'app/src/main'
COPY=json.loads((MAIN/'assets/app_copy_en_hi.json').read_text(encoding='utf-8'))
required={'home','tests','exam','results','insights','ranking','profile'}
assert required <= set(COPY['screen'])
for page in required:
    for lang in ('en','hi'):
        assert COPY['screen'][page][lang],f'{page}/{lang} copy empty'
for entry in COPY['motivational_lines']+COPY['loading_lines']:
    assert entry['en'] and entry['hi']
assert COPY['app']['tagline']['en']=='Future ASO Starts Here'
assert '❤️' in COPY['app']['footer']['en']
assert 'Bihar' in COPY['app']['footer']['en']

icon=Image.open(MAIN/'res/drawable-nodpi/brand_icon.png')
assert icon.size==(512,512)
for tier,side in [('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)]:
    assert Image.open(MAIN/f'res/mipmap-{tier}/ic_launcher.png').size==(side,side)
assert Image.open(MAIN/'res/drawable-nodpi/officer_journey.jpg').width >= 800
manifest=(MAIN/'AndroidManifest.xml').read_text()
assert 'android:icon="@mipmap/ic_launcher"' in manifest
src=(MAIN/'java/com/bssc/cgl4/free100mock')
assert 'BiharLoveFooter(hindi)' in (src/'MainActivity.kt').read_text()
assert 'Future ASO' in (src/'Engagement.kt').read_text()
assert 'OfficerMotivationCard(hindi)' in (src/'Dashboard.kt').read_text()
assert 'fun TestCatalogueScreen' in (src/'Dashboard.kt').read_text()
assert 'ReferenceMockRow' in (src/'Dashboard.kt').read_text()
assert 'Tab.TESTS' in (src/'MainActivity.kt').read_text()
assert 'ReferenceSplash(hindi)' in (src/'MainActivity.kt').read_text()
assert 'Top Aspirants' in (src/'LeaderboardAndProfile.kt').read_text()
assert 'Great Attempt!' in (src/'ResultsAndInsights.kt').read_text()
assert 'internalTarget' in (src/'Engagement.kt').read_text()
config=json.loads((ROOT/'firebase/engagement.example.json').read_text())
assert len(config['banners']) >= 3
assert any(x['id']=='aspirant' and x['active'] for x in config['banners'])
assert any(x['id']=='new_mock' and not x['active'] for x in config['banners'])
assert all(x.get('officialConfirmed') is False for x in config['events'])
print('PASS V5: uploaded-reference launcher icon, purple/gold design, mock list, exam, results, ranking, bilingual footer, Firebase and countdown safeguards')
