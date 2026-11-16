import copy
import unittest
from manage_home_cards import validate,validate_card

BASE={"id":"study_demo","active":True,"sectionId":"study","titleEn":"Study",
      "actionType":"internal","actionValue":"mock_list"}

class HomeCardEditorTests(unittest.TestCase):
    def test_full_custom_card(self):
        item=copy.deepcopy(BASE)
        ident,data=validate_card(item)
        self.assertEqual(ident,"study_demo")
        self.assertEqual(data["theme"],"white")
        self.assertEqual(data["layout"],"horizontal")
        self.assertEqual(data["placement"],"middle")
        self.assertTrue(data["active"])

    def test_cutoff_requires_status_and_source(self):
        item={**BASE,"id":"cutoff_ur","type":"cutoff","value":"342 / 600"}
        with self.assertRaises(ValueError):validate_card(item)
        item["claim"]="expected"
        self.assertEqual("expected",validate_card(item)[1]["claim"])
        item["claim"]="official"
        with self.assertRaises(ValueError):validate_card(item)
        item["sourceUrl"]="https://example.com/cutoff"
        with self.assertRaises(ValueError):validate_card(item)
        item["sourceUrl"]="https://bssc.bihar.gov.in/NoticeBoard.htm"
        self.assertEqual("official",validate_card(item)[1]["claim"])

    def test_unpublished_cutoff_may_be_empty(self):
        item={**BASE,"id":"cutoff_ur","type":"cutoff","claim":"expected",
              "value":"","active":False}
        self.assertFalse(validate_card(item)[1]["active"])
        item["active"]=True
        with self.assertRaises(ValueError):validate_card(item)

    def test_bad_urls_and_actions_fail(self):
        for url in ("http://example.com","javascript:alert(1)","file:///etc/passwd"):
            with self.assertRaises(ValueError):
                validate_card({**BASE,"actionType":"external","actionValue":url})
        with self.assertRaises(ValueError):
            validate_card({**BASE,"actionType":"internal","actionValue":"arbitrary"})

    def test_import_count_and_unique_ids(self):
        with self.assertRaises(ValueError):validate({"cards":[BASE,BASE]})
        with self.assertRaises(ValueError):validate({"cards":[BASE]*101})

    def test_scheduling_requires_timezone(self):
        with self.assertRaises(ValueError):
            validate_card({**BASE,"startsAt":"2027-01-05T10:00:00"})
        self.assertIsNotNone(validate_card({**BASE,
            "startsAt":"2027-01-05T10:00:00+05:30"})[1]["startsAt"])

if __name__=="__main__":
    unittest.main()
