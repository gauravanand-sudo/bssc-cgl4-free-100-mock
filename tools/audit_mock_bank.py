#!/usr/bin/env python3
"""Audit bundled bilingual question papers without altering the answer keys.

Usage: python3 tools/audit_mock_bank.py
       python3 tools/audit_mock_bank.py --strict-unique
A strict run is suitable after editors have resolved repeated questions.
"""
import argparse
import json
import re
from pathlib import Path


def normalize(text: str) -> str:
    return re.sub(r"[^a-z0-9]+", " ", text.lower()).strip()


def audit_files(files):
    issues = []
    duplicates = []
    seen_questions = {}
    count = 0
    summary = []
    for path in files:
        data = json.loads(Path(path).read_text(encoding="utf-8"))
        questions = data if isinstance(data, list) else data.get("questions", [])
        if len(questions) != 150:
            issues.append(f"{path}: expected exactly 150 questions, got {len(questions)}")
        ids = set()
        for idx, q in enumerate(questions, start=1):
            count += 1
            position = f"{Path(path).name}:{idx}"
            if not isinstance(q, dict):
                issues.append(f"{position}: invalid question object")
                continue
            question_id = q.get("id")
            if not isinstance(question_id, str) or not question_id.strip():
                issues.append(f"{position}: missing question ID")
            elif question_id in ids:
                issues.append(f"{position}: duplicate question ID within same paper")
            else:
                ids.add(question_id)
            for field in ("questionEn", "questionHi", "explanationEn", "explanationHi"):
                if not isinstance(q.get(field), str) or not q[field].strip():
                    issues.append(f"{position}: missing {field}")
            for field in ("optionsEn", "optionsHi"):
                if not isinstance(q.get(field), list) or len(q[field]) != 4 or any(
                    not isinstance(v, str) or not v.strip() for v in q[field]
                ):
                    issues.append(f"{position}: {field} must contain 4 nonempty options")
            key = q.get("correctIndex")
            if type(key) is not int or key not in range(4):
                issues.append(f"{position}: correctIndex must be 0..3")
            title = q.get("questionEn", "")
            if isinstance(title, str) and normalize(title):
                normalized = normalize(title)
                if normalized in seen_questions:
                    duplicates.append((seen_questions[normalized], position, title))
                else:
                    seen_questions[normalized] = position
        summary.append((Path(path).name, len(questions)))
    return {"issues": issues, "duplicates": duplicates, "count": count, "summary": summary}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--strict-unique", action="store_true",
                        help="Fail on repeated English question texts across and within mocks")
    args = parser.parse_args()
    paths = sorted(Path("app/src/main/assets").glob("mock_*.json"))
    if not paths:
        parser.error("No bundled mock assets found")
    report = audit_files(paths)
    for name, total in report["summary"]:
        print(f"{name}: {total} bilingual questions")
    print(f"TOTAL questions: {report['count']}")
    print(f"STRUCTURAL ISSUES: {len(report['issues'])}")
    print(f"REPEATED ENGLISH TEXTS: {len(report['duplicates'])}")
    for issue in report["issues"][:12]:
        print("ERROR:", issue)
    for original, repeated, title in report["duplicates"][:12]:
        print(f"REPEAT: {original} == {repeated} | {title[:80]}")
    if report["issues"]:
        raise SystemExit("Failed: fix structural issues before shipping.")
    if report["duplicates"]:
        print("WARNING: repeated questions need editorial replacement and bilingual validation.")
        if args.strict_unique:
            raise SystemExit("Failed strict uniqueness policy.")
    else:
        print("All bundled questions have unique normalized English text.")


if __name__ == "__main__":
    main()
