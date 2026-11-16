package com.bssc.cgl4.free100mock

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MockAccessTest {
    @Test fun firstTwoMocksAreFree() {
        assertTrue(MockAccess.isAccessible(1, emptySet()))
        assertTrue(MockAccess.isAccessible(2, emptySet()))
        assertFalse(MockAccess.isRewarded(1))
        assertFalse(MockAccess.isRewarded(2))
    }
    @Test fun everyMockThreeThroughHundredCanEarnAdUnlock() {
        for (id in 3..100) {
            assertTrue("Mock $id should use reward unlock", MockAccess.isRewarded(id))
            assertFalse(MockAccess.isAccessible(id, emptySet()))
            assertTrue(MockAccess.isAccessible(id, setOf(id)))
        }
    }
    @Test fun aRewardUnlockAppliesToOnlyItsMock() {
        assertFalse(MockAccess.isAccessible(3, setOf(4)))
        assertFalse(MockAccess.isAccessible(100, setOf(99)))
        assertTrue(MockAccess.isAccessible(50, setOf(50)))
        assertTrue(MockAccess.isAccessible(100, setOf(100)))
    }
    @Test fun invalidMockNumbersCannotBeUnlocked() {
        assertFalse(MockAccess.isAccessible(0, setOf(0)))
        assertFalse(MockAccess.isAccessible(101, setOf(101)))
        assertFalse(MockAccess.isRewarded(-1))
        assertFalse(MockAccess.isRewarded(101))
    }
}
