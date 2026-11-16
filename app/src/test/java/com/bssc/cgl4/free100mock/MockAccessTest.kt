package com.bssc.cgl4.free100mock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MockAccessTest {
    @Test fun twoFreeMocks() {
        assertEquals(0, MockAccess.requiredAds(1))
        assertEquals(0, MockAccess.requiredAds(2))
        assertTrue(MockAccess.isAccessible(1, emptySet()))
        assertTrue(MockAccess.isAccessible(2, emptySet()))
    }

    @Test fun threeThroughFiftyNeedExactlyOneAd() {
        for (id in 3..50) {
            assertEquals(1, MockAccess.requiredAds(id))
            assertEquals(1, MockAccess.nextCredit(id, 0))
            assertEquals(1, MockAccess.nextCredit(id, 1))
            assertFalse(MockAccess.isAccessible(id, emptySet()))
            assertTrue(MockAccess.isAccessible(id, setOf(id)))
        }
    }

    @Test fun fiftyOneThroughHundredNeedTwoAds() {
        for (id in 51..100) {
            assertEquals(2, MockAccess.requiredAds(id))
            assertEquals(1, MockAccess.nextCredit(id, 0))
            assertEquals(2, MockAccess.nextCredit(id, 1))
            assertEquals(2, MockAccess.nextCredit(id, 2))
            assertFalse(MockAccess.isAccessible(id, emptySet()))
            assertTrue(MockAccess.isAccessible(id, setOf(id)))
        }
    }

    @Test fun noRewardCanUnlockOtherMockOrInvalidIds() {
        assertFalse(MockAccess.isAccessible(51, setOf(50)))
        assertFalse(MockAccess.isAccessible(100, setOf(99)))
        assertEquals(0, MockAccess.requiredAds(101))
        assertEquals(0, MockAccess.nextCredit(101, 1))
        assertFalse(MockAccess.isAccessible(101, setOf(101)))
    }
}
