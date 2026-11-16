import json
import tempfile
import unittest
from pathlib import Path
from audit_mock_bank import audit_files, normalize


class MockAuditTests(unittest.TestCase):
    def sample(self, text="Which planet is red?"):
        return {
            "id": "q1", "questionEn": text, "questionHi": "कौन सा ग्रह लाल है?",
            "optionsEn": ["Mars", "Earth", "Venus", "Mercury"],
            "optionsHi": ["मंगल", "पृथ्वी", "शुक्र", "बुध"],
            "correctIndex": 0,
            "explanationEn": "Mars is called the red planet.",
            "explanationHi": "मंगल लाल ग्रह कहलाता है."
        }

    def test_normalize_identifies_punctuation_variants(self):
        self.assertEqual(normalize("Which planet is red?"),
                         normalize("WHICH planet, is red!"))

    def test_cross_paper_duplicates_detected(self):
        with tempfile.TemporaryDirectory() as d:
            p1, p2 = Path(d) / "mock_01.json", Path(d) / "mock_02.json"
            p1.write_text(json.dumps([self.sample() for _ in range(150)]),encoding="utf-8")
            p2.write_text(json.dumps([self.sample("WHICH planet, is red!")
                for _ in range(150)]),encoding="utf-8")
            result = audit_files([p1, p2])
            self.assertEqual(result["count"], 300)
            self.assertEqual(len(result["duplicates"]), 299)
            self.assertGreater(len(result["issues"]), 0)  # repeated IDs within a paper

    def test_missing_hindi_is_an_error(self):
        with tempfile.TemporaryDirectory() as d:
            p = Path(d) / "mock_01.json"
            q = self.sample()
            q["optionsHi"] = []
            p.write_text(json.dumps([q]),encoding="utf-8")
            result = audit_files([p])
            self.assertTrue(any("optionsHi" in issue for issue in result["issues"]))


if __name__ == "__main__":
    unittest.main()
