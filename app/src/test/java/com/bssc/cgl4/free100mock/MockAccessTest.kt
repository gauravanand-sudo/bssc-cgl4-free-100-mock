package com.bssc.cgl4.free100mock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MockAccessTest {
    private val ad = MockAccess.Mode(false, true)
    private val paid = MockAccess.Mode(true, true)
    @Test fun freeTests() {
        assertTrue(MockAccess.isAccessible(1, emptySet(), ad))
        assertTrue(MockAccess.isAccessible(2, emptySet(), paid))
    }
    @Test fun defaultRewardedTestsThreeToHundred() {
        assertTrue(MockAccess.isRewarded(3, ad))
        assertTrue(MockAccess.isRewarded(49, ad))
        assertTrue(MockAccess.isRewarded(50, ad))
        assertTrue(MockAccess.isRewarded(100, ad))
        assertTrue(MockAccess.isAccessible(100, setOf(100), ad))
        assertFalse(MockAccess.isAccessible(100, emptySet(), ad))
    }
    @Test fun switchingPaidCoversMock50Too() {
        assertTrue(MockAccess.isRewarded(49, paid))
        assertTrue(MockAccess.isPremium(50, paid))
        assertTrue(MockAccess.isPremium(51, paid))
        assertTrue(MockAccess.isPremium(100, paid))
        assertFalse(MockAccess.isRewarded(50, paid))
        assertFalse(MockAccess.isAccessible(50, setOf(50), paid))
        assertFalse(MockAccess.isAccessible(100, (1..100).toSet(), paid))
        assertTrue(MockAccess.isAccessible(49, setOf(49), paid))
    }
    @Test fun unavailableServerBlocksSwitchableMocks() {
        val unknown = MockAccess.Mode()
        assertEquals(MockAccess.Tier.CHECKING, MockAccess.tier(50, unknown))
        assertFalse(MockAccess.isAccessible(100, setOf(100), unknown))
        assertTrue(MockAccess.isRewarded(49, unknown))
        assertEquals(MockAccess.Tier.INVALID, MockAccess.tier(101, ad))
    }
}
