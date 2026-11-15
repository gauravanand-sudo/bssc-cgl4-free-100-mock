#!/usr/bin/env python3
"""Validate and optionally publish up to 100 full-length mocks to Firestore.

Requires: pip install firebase-admin
Authentication: GOOGLE_APPLICATION_CREDENTIALS=/private/path/service-account.json
Example dry-run: python firebase/import_content.py --content-dir app/src/main/assets
Upload only after review: python firebase/import_content.py --content-dir content/reviewed --execute

Accepted filenames: mock_001.json ... mock_100.json (also mock_01.json for existing demos).
Optional sidecar metadata: content/reviewed/catalog.json: [{"testId": 1, "title": "...", ...}]
No credentials, service-account JSON, or admin secrets are stored in the Android app.
"""
import argparse
import json
import pathlib
import re
import sys

DEFAULT_TITLES = {
    1: ('The Foundation', 'Build exam-day confidence', 'Balanced'),
    2: ('Bihar Special', 'State-focused practice', 'Intermediate'),
    3: ('Speed & Accuracy', 'Sharpen your solving speed', 'Advanced'),
    4: ('The Grand Mock', 'Realistic full-length challenge', 'Advanced'),
}


def validate(path):
    rows = json.loads(path.read_text(encoding='utf-8'))
    if not isinstance(rows, list) or len(rows) != 150:
        raise ValueError(f'{path}: exactly 150 questions required')
    seen = set()
    required = ('id', 'section', 'topic', 'questionEn', 'questionHi',
                'optionsEn', 'optionsHi', 'correctIndex', 'explanationEn', 'explanationHi')
    for i, row in enumerate(rows):
        if not isinstance(row, dict) or any(k not in row for k in required):
            raise ValueError(f'{path}: question {i+1} missing required fields')
        if not isinstance(row['optionsEn'], list) or len(row['optionsEn']) != 4 or not isinstance(row['optionsHi'], list) or len(row['optionsHi']) != 4:
            raise ValueError(f'{path}: question {i+1} must have four options per language')
        if row['correctIndex'] not in (0,1,2,3):
            raise ValueError(f'{path}: question {i+1}: bad answer index')
        if not all(isinstance(row[k], str) and row[k].strip() for k in ('id','section','topic','questionEn','questionHi')):
            raise ValueError(f'{path}: question {i+1}: empty question fields')
        if row['id'] in seen:
            raise ValueError(f'{path}: duplicate question ID {row["id"]}')
        seen.add(row['id'])
    encoded = json.dumps(rows,ensure_ascii=False,separators=(',', ':'))
    if len(encoded.encode('utf-8')) > 950_000:
        raise ValueError(f'{path}: paper document may exceed Firestore 1 MiB limit')
    return encoded


def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--content-dir',default='app/src/main/assets')
    parser.add_argument('--execute',action='store_true',help='Write to Firestore (without this, dry-run)')
    parser.add_argument('--project-id',default=None)
    parser.add_argument('--publish',action='store_true',help='Make uploaded papers visible (review FIRST)')
    args=parser.parse_args()
    folder=pathlib.Path(args.content_dir)
    metadata_path=folder/'catalog.json'
    metadata={int(t['testId']):t for t in json.loads(metadata_path.read_text())} if metadata_path.exists() else {}
    inputs={}
    for path in sorted(folder.glob('mock_*.json')):
        m=re.fullmatch(r'mock_(\d{1,3})\.json',path.name)
        if not m: continue
        i=int(m.group(1))
        if not 1<=i<=100: raise ValueError(f'{path}: id out of 1..100')
        if i in inputs: raise ValueError(f'duplicate number: {i}')
        inputs[i]=(path,validate(path))
    if not inputs: raise ValueError('No valid mock_NNN.json files found')
    print(f'Validated {len(inputs)} complete papers = {150*len(inputs)} questions')
    if not args.execute:
        print('Dry-run: nothing uploaded. Add --execute to upload, and --publish only after editorial review.')
        return
    import firebase_admin
    from firebase_admin import firestore
    from firebase_admin import credentials
    options={'projectId':args.project_id} if args.project_id else None
    firebase_admin.initialize_app(options=options)
    db=firestore.client()
    for number,(path,raw) in inputs.items():
        doc=f'mock_{number:03d}'
        title,subtitle,difficulty=DEFAULT_TITLES.get(number,(f'Mock {number:03d}','Full-length BSSC CGL 4 practice','Balanced'))
        custom=metadata.get(number,{})
        # Paper first; publish catalogue last so a new visible mock always has questions.
        db.collection('mock_papers').document(doc).set({'questionsJson':raw,'version':int(custom.get('version',1))})
        db.collection('mock_catalog').document(doc).set({
            'testId':number,'title':custom.get('title',title),'subtitle':custom.get('subtitle',subtitle),
            'difficulty':custom.get('difficulty',difficulty),'questionCount':150,
            'durationMinutes':135,'sortOrder':number,'isFree':bool(custom.get('isFree',number<=2)),
            'version':int(custom.get('version',1)),'published':bool(args.publish)
        })
        print(f'{doc}: uploaded ({"PUBLISHED" if args.publish else "DRAFT"})')
    print('Done. Test from a device with Anonymous Auth enabled and rules deployed.')

if __name__=='__main__':
    main()
