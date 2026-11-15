from pathlib import Path
import json, collections
root=Path(__file__).parent/'app/src/main/assets'
for test_id in range(1,5):
    path=root/f'mock_{test_id:02d}.json'
    questions=json.loads(path.read_text(encoding='utf8'))
    assert len(questions)==150, (path,len(questions))
    assert dict(collections.Counter(q['section'] for q in questions))=={
        'General Studies':50,'Science & Maths':50,'Reasoning':50}
    for q in questions:
        assert all(q.get(k) for k in ['id','topic','questionEn','questionHi','explanationEn','explanationHi'])
        assert len(q['optionsEn'])==len(q['optionsHi'])==4
        assert len(set(q['optionsEn']))==4
        assert 0<=q['correctIndex']<4
    print(path.name,'OK',len(questions),'questions')
print('All 600 mock slots validated (some concepts repeat between tests).')
