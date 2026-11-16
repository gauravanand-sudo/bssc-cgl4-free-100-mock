"""Validation tests for the one-banner, confirmed-date and cutoff editor."""
import copy
import unittest
from manage_engagement import validate


class EngagementConfigTests(unittest.TestCase):
    def event(self):
        return {
            "id": "cgl4_prelims",
            "published": True,
            "nameEn": "BSSC CGL 4 Prelims",
            "nameHi": "बीएसएससी सीजीएल 4 प्रारंभिक",
            "officialConfirmed": False,
            "examAt": None,
            "sourceUrl": "https://bssc.bihar.gov.in/NoticeBoard.htm",
            "cutoffPublished": False,
            "cutoffType": "expected",
            "cutoffMarks": {},
            "cutoffSourceUrl": "",
            "cutoffNoteEn": "",
            "cutoffNoteHi": "",
        }

    def test_unconfirmed_date_has_no_countdown(self):
        data=self.event()
        patches=validate({"events":[data]})
        self.assertIsNone(patches[0][2]["examAt"])
        self.assertFalse(patches[0][2]["officialConfirmed"])

    def test_official_date_requires_timestamp(self):
        data=self.event()
        data["officialConfirmed"]=True
        with self.assertRaises(ValueError):
            validate({"events":[data]})
        data["examAt"]="2027-04-15T09:00:00+05:30"
        patches=validate({"events":[data]})
        self.assertIsNotNone(patches[0][2]["examAt"])

    def test_cutoff_official_requires_source(self):
        data=self.event()
        data.update(cutoffPublished=True,cutoffType="official",cutoffMarks={"UR":370})
        with self.assertRaises(ValueError):
            validate({"events":[data]})
        data["cutoffSourceUrl"]="https://bssc.bihar.gov.in/NoticeBoard.htm"
        self.assertEqual(370,validate({"events":[data]})[0][2]["cutoffMarks"]["UR"])

    def test_expected_cutoff_never_requires_guess_or_source(self):
        data=self.event()
        data.update(cutoffPublished=True,cutoffType="expected",
                    cutoffMarks={"UR":341,"EWS":336})
        self.assertEqual(2,len(validate({"events":[data]})[0][2]["cutoffMarks"]))

    def test_invalid_cutoff_blocked(self):
        data=self.event()
        for value in (-1,601,"350",True):
            sample=copy.deepcopy(data)
            sample.update(cutoffPublished=True,cutoffMarks={"UR":value})
            with self.assertRaises(ValueError):
                validate({"events":[sample]})
        data["cutoffPublished"]=True
        with self.assertRaises(ValueError):
            validate({"events":[data]})

    def test_exam_and_cutoff_independent(self):
        data=self.event()
        data.update(cutoffPublished=True,cutoffMarks={"SC":315})
        patches=validate({"events":[data]})
        self.assertFalse(patches[0][2]["officialConfirmed"])
        self.assertTrue(patches[0][2]["cutoffPublished"])


if __name__ == "__main__":
    unittest.main()
