"""Admin-only FCM sender; dry-run by default. Never embed credentials in the app.

python firebase/send_notification.py --title "New mock released" --body "Try Mock 005"
python firebase/send_notification.py --title "New mock released" --body "Try Mock 005" --send
"""
import argparse
import os
import json
from pathlib import Path


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--title')
    parser.add_argument('--body')
    parser.add_argument('--template', choices=('new_mock','official_notice','practice_reminder'))
    parser.add_argument('--language', choices=('en','hi'), default='en')
    parser.add_argument('--send',action='store_true')
    args=parser.parse_args()
    if args.template:
        content=json.loads((Path(__file__).resolve().parents[1]/'app/src/main/assets/app_copy_en_hi.json').read_text(encoding='utf-8'))
        entry=next((item for item in content['notification_templates'] if item['key']==args.template),None)
        if entry is None: raise SystemExit('Notification template not found')
        args.title=entry[f'{args.language}_title']
        args.body=entry[f'{args.language}_body']
    if not args.title or not args.body or not args.title.strip() or not args.body.strip() or len(args.title)>100 or len(args.body)>320:
        raise SystemExit('Title 1–100 characters, body 1–320 characters required')
    if args.template=='official_notice':
        print('VERIFY the official BSSC notice before sending this template.')
    if args.template=='new_mock':
        print('VERIFY a new paper is published before sending this template.')
    print('Audience: opted-in FCM topic bssc_cgl4_updates')
    print(f'Title: {args.title}\nBody: {args.body}')
    if not args.send:
        print('DRY RUN: nothing sent; add --send after reviewing the target and content')
        return
    if not os.environ.get('GOOGLE_APPLICATION_CREDENTIALS'):
        raise SystemExit('GOOGLE_APPLICATION_CREDENTIALS must reference your private service-account JSON')
    import firebase_admin
    from firebase_admin import credentials, messaging
    firebase_admin.initialize_app(credentials.ApplicationDefault())
    message=messaging.Message(topic='bssc_cgl4_updates',notification=messaging.Notification(title=args.title,body=args.body),
        android=messaging.AndroidConfig(priority='normal'))
    print('FCM message ID:', messaging.send(message))

if __name__=='__main__':main()
