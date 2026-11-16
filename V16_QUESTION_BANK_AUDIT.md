# V16 Question Bank Quality Audit

The V16 UI has been upgraded, but source content needs editorial attention before a commercial-scale launch.

Four bundled assets (`mock_01.json` through `mock_04.json`) were inspected:

| Measure | Result |
|---|---|
| Papers checked | 4 |
| Questions checked | 600 (150 per paper) |
| Empty Hindi questions/explanations or wrong-size Hindi choices | 0 |
| Invalid English option sets/correct-index entries | 0 |
| Repeated normalized English question texts | **224** |

Duplicates include both repeated prompts within a single paper and repeats across different mock papers. Examples include a basic multiplication question and questions about the President's eligibility age and Bodh Gaya. The repeated count is the number of surplus copies after the first occurrence of each normalized English prompt, not the total number of unique repeated topics.

**Do not claim the 100-paper library is unique, production quality or entirely published.** Only four starter papers are bundled in source; other Firebase papers need separate verification.

The audit utility `tools/audit_mock_bank.py` runs in GitHub Actions. It:
- Fails CI for broken 150-question lengths, missing bilingual content, bad answer keys and malformed options.
- Warns about duplicate question texts in current starter content.
- Supports `--strict-unique` for editorial release gating once duplicates are replaced and reviewed.

Avoid auto-replacing 224 questions with unverified material. Editor-reviewed bilingual replacements must maintain topic distribution, correct answer and explanation quality, and fair challenge. Before store rollout, replace duplicates, rerun strict mode and review each question set with an actual subject-matter reviewer.

This report concerns current bundled source files only; it does not certify nor audit unseen Firebase documents or user-provided external mock JSON files.
