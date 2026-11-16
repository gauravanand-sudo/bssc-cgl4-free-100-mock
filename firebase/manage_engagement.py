"""Safely upsert Firebase-controlled promotional banners and official countdown events.

Usage: python firebase/manage_engagement.py --file firebase/engagement.example.json
       python firebase/manage_engagement.py --file firebase/engagement.example.json --execute
Always read the official BSSC notice before setting officialConfirmed true.
"""
import argparse
import datetime as dt
import json
import os
import re
from pathlib import Path
from urllib.parse import urlparse


def https_url(value, required=False):
    if not value:
        if required:
            raise ValueError('targetUrl/sourceUrl is required')
        return None
    if not isinstance(value,str) or len(value)>2048:
        raise ValueError('URL must be a string at most 2048 characters')
    p = urlparse(value)
    if p.scheme != 'https' or not p.hostname or p.username or p.password:
        raise ValueError(f'Only safe HTTPS URLs allowed: {value!r}')
    return value


def as_time(value):
    if not value:
        return None
    result = dt.datetime.fromisoformat(value.replace('Z', '+00:00'))
    if result.tzinfo is None:
        raise ValueError('Date-time must include timezone, e.g. 2027-01-10T09:00:00+05:30')
    return result.astimezone(dt.timezone.utc)


def validate(payload):
    patches = []
    for source, collection, ids in [('banners', 'promo_banners', 'active'), ('events', 'exam_events', 'published')]:
        for raw in payload.get(source, []):
            obj=dict(raw)
            ident=obj.pop('id')
            if not re.fullmatch(r'[a-z][a-z0-9_-]{2,60}', ident):
                raise ValueError(f'Invalid {source} ID: {ident}')
            if not isinstance(obj.get(ids), bool):
                raise ValueError(f'{ident}: {ids} must be true/false')
            if source=='banners':
                image_only=obj.get('imageOnly',False)
                if not isinstance(image_only,bool):
                    raise ValueError(f'{ident}: imageOnly must be boolean')
                action_type=obj.get('action_type','external')
                action_value=obj.get('action_value','')
                allowed_routes={'mock_list','insights','ranking','home'}
                if action_type not in {'internal','external'}:
                    raise ValueError(f'{ident}: action_type must be internal or external')
                if action_type=='internal':
                    if action_value not in allowed_routes:
                        raise ValueError(f'{ident}: unsupported internal destination')
                    https_url(obj.get('targetUrl'),required=False)
                else:
                    https_url(obj.get('targetUrl'),required=True)
                https_url(obj.get('imageUrl'),required=image_only)
                # The legacy flat schema is still accepted for older banners.
                if 'title' in obj or 'subtitle' in obj or 'cta' in obj:
                    for field in ('title','subtitle','cta'):
                        values=obj.get(field)
                        if not isinstance(values,dict) or not all(
                            isinstance(values.get(lang),str) and len(values[lang])<=200
                            for lang in ('en','hi')):
                            raise ValueError(f'{ident}: {field} must have en and hi strings (max 200)')
                    # Keep fields nested for Android; no need to duplicate legacy fields.
                else:
                    for field in ('titleEn','titleHi','descriptionEn','descriptionHi','buttonEn','buttonHi'):
                        value=obj.get(field,'' if image_only else None)
                        if not isinstance(value,str) or len(value)>200:
                            raise ValueError(f'{ident}: {field} must be a string (max 200)')
                        obj[field]=value
                if not image_only:
                    title=obj.get('title')
                    if title is None and not (obj.get('titleEn') or obj.get('titleHi')):
                        raise ValueError(f'{ident}: text banner needs a title')
                if not isinstance(obj.get('priority'),int):
                    raise ValueError(f'{ident}: priority must be an integer')
                obj['startsAt']=as_time(obj.get('startsAt'))
                obj['endsAt']=as_time(obj.get('endsAt'))
                if obj['startsAt'] and obj['endsAt'] and obj['startsAt']>=obj['endsAt']:
                    raise ValueError('startsAt must be earlier than endsAt')
            else:
                https_url(obj.get('sourceUrl'),required=True)
                obj['examAt']=as_time(obj.get('examAt'))
                if obj.get('officialConfirmed'):
                    if not obj['examAt']:
                        raise ValueError(f'{ident}: confirmed exam needs a date')
                    if not obj['sourceUrl'].startswith('https://bssc.bihar.gov.in/'):
                        raise ValueError(f'{ident}: confirmation requires official BSSC notice source')
                if not isinstance(obj.get('officialConfirmed'),bool):
                    raise ValueError('officialConfirmed must be true/false')

                # Cutoff scores can be published independently of the exam date.
                obj.setdefault('cutoffPublished',False)
                obj.setdefault('cutoffType','expected')
                obj.setdefault('cutoffMarks',{})
                obj.setdefault('cutoffSourceUrl','')
                obj.setdefault('cutoffNoteEn','')
                obj.setdefault('cutoffNoteHi','')
                if not isinstance(obj['cutoffPublished'],bool):
                    raise ValueError(f'{ident}: cutoffPublished must be true/false')
                if obj['cutoffType'] not in {'expected','official'}:
                    raise ValueError(f'{ident}: cutoffType must be expected or official')
                if not isinstance(obj['cutoffMarks'],dict):
                    raise ValueError(f'{ident}: cutoffMarks must be a category/marks map')
                if len(obj['cutoffMarks'])>16:
                    raise ValueError(f'{ident}: max 16 cutoff categories')
                for category,score in obj['cutoffMarks'].items():
                    if not isinstance(category,str) or not category.strip() or len(category)>30:
                        raise ValueError(f'{ident}: cutoff category must be 1–30 characters')
                    if type(score) not in (int,float) or not 0<=score<=600:
                        raise ValueError(f'{ident}: cutoff {category!r} must be a number 0–600')
                https_url(obj['cutoffSourceUrl'],required=False)
                for field in ('cutoffNoteEn','cutoffNoteHi'):
                    if not isinstance(obj[field],str) or len(obj[field])>200:
                        raise ValueError(f'{ident}: {field} must be text under 200 characters')
                if obj['cutoffPublished']:
                    if not obj['cutoffMarks']:
                        raise ValueError(f'{ident}: published cutoff requires category scores')
                    if obj['cutoffType']=='official':
                        https_url(obj['cutoffSourceUrl'],required=True)
                        if not obj['cutoffSourceUrl'].startswith('https://bssc.bihar.gov.in/'):
                            raise ValueError(f'{ident}: official cutoff requires BSSC source')
            patches.append((collection,ident,obj))
    return patches


def main():
    ap=argparse.ArgumentParser(description=__doc__)
    ap.add_argument('--file',default='firebase/engagement.example.json')
    ap.add_argument('--execute',action='store_true',help='Actually publish writes, otherwise dry run')
    args=ap.parse_args()
    patches=validate(json.loads(Path(args.file).read_text(encoding='utf-8')))
    for coll,doc,obj in patches:
        print(f'{coll}/{doc} => {"active" if obj.get("active",obj.get("published")) else "inactive/draft"}')
    if not args.execute:
        print('DRY RUN: no writes performed. Use --execute after review.')
        return
    if not os.environ.get('GOOGLE_APPLICATION_CREDENTIALS'):
        raise SystemExit('GOOGLE_APPLICATION_CREDENTIALS must point to your private Admin SDK service account JSON (outside repo).')
    import firebase_admin
    from firebase_admin import credentials, firestore
    app=firebase_admin.initialize_app(credentials.ApplicationDefault())
    db=firestore.client(app)
    batch=db.batch()
    for collection,ident,obj in patches:
        batch.set(db.collection(collection).document(ident),obj,merge=True)
    batch.commit()
    print(f'Uploaded {len(patches)} Firebase documents successfully.')

if __name__=='__main__':
    main()
