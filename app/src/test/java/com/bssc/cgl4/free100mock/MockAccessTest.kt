package com.bssc.cgl4.free100mock

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MockAccessTest {
    @Test fun freePapersAlwaysAccessible() {
        assertTrue(MockAccess.isAccessible(1, emptySet()))
        assertTrue(MockAccess.isAccessible(2, emptySet()))
        assertFalse(MockAccess.isAccessible(3, emptySet()))
    }

    @Test fun rewardedRangeCanUnlockOnlyAfterReward() {
        assertTrue(MockAccess.isRewarded(3))
        assertTrue(MockAccess.isRewarded(50))
        assertFalse(MockAccess.isRewarded(51))
        assertTrue(MockAccess.isAccessible(3, setOf(3)))
        assertTrue(MockAccess.isAccessible(50, setOf(50)))
        assertFalse(MockAccess.isAccessible(49, setOf(50)))
    }

    @Test fun premiumCannotBeUnlockedByAnAdOrCatalogueFlag() {
        assertTrue(MockAccess.isPremium(51))
        assertTrue(MockAccess.isPremium(100))
        assertFalse(MockAccess.isAccessible(51, setOf(51)))
        assertFalse(MockAccess.isAccessible(100, (1..100).toSet()))
        assertFalse(MockAccess.isAccessible(0, setOf(0)))
        assertFalse(MockAccess.isAccessible(101, setOf(101)))
    }
}
