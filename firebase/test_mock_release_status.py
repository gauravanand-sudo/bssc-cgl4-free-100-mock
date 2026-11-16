import datetime
import unittest

from manage_mock_release import make_update, parse_date


class MockStatusTests(unittest.TestCase):
    def test_upcoming_is_visible_but_not_open(self):
        obj = make_update(55, "upcoming", available_at="2027-01-10T10:00:00+05:30")
        self.assertTrue(obj["published"])
        self.assertEqual("upcoming", obj["status"])
        self.assertEqual(55, obj["testId"])
        self.assertTrue(isinstance(obj["availableAt"], datetime.datetime))

    def test_releasing_does_not_create_questions(self):
        obj = make_update(55, "available")
        self.assertEqual("available", obj["status"])
        self.assertNotIn("questionsJson", obj)
        self.assertNotIn("availableAt", obj)

    def test_invalid_status_and_dates_rejected(self):
        for status in ("paid", "draft", "", "comingsoon"):
            with self.assertRaises(ValueError):
                make_update(55, status)
        for number in (0, 101):
            with self.assertRaises(ValueError):
                make_update(number, "upcoming")
        with self.assertRaises(ValueError):
            parse_date("2027-01-10T10:00:00")


if __name__ == "__main__":
    unittest.main()
