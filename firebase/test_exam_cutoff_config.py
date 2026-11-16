"""Date-only and single promotional banner validation (cutoff is home_cards)."""
import unittest
from manage_engagement import validate


class EngagementConfigTests(unittest.TestCase):
    def event(self):
        return {
            "id": "cgl4_prelims", "published": True,
            "nameEn": "BSSC CGL 4 Prelims",
            "nameHi": "बीएसएससी सीजीएल 4 प्रारंभिक",
            "officialConfirmed": False,
            "examAt": None,
            "sourceUrl": "https://bssc.bihar.gov.in/NoticeBoard.htm",
        }

    def test_unconfirmed_date_stays_hidden(self):
        fields=validate({"events":[self.event()]})[0][2]
        self.assertFalse(fields["officialConfirmed"])
        self.assertIsNone(fields["examAt"])
        self.assertNotIn("cutoffPublished",fields)

    def test_confirmed_date_requires_actual_time(self):
        item=self.event()
        item["officialConfirmed"]=True
        with self.assertRaises(ValueError):validate({"events":[item]})
        item["examAt"]="2027-02-10T09:00:00+05:30"
        self.assertIsNotNone(validate({"events":[item]})[0][2]["examAt"])

    def test_unverified_domain_is_not_official(self):
        item=self.event()
        item.update(officialConfirmed=True,examAt="2027-02-10T09:00:00+05:30",
                    sourceUrl="https://example.com/fake")
        with self.assertRaises(ValueError):validate({"events":[item]})

    def test_cutoffs_are_not_generated_as_old_fixed_panel(self):
        fields=validate({"events":[self.event()]})[0][2]
        self.assertEqual(set(fields),set(self.event())-{"id"})


if __name__=="__main__":
    unittest.main()
