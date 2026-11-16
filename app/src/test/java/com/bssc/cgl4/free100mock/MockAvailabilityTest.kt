package com.bssc.cgl4.free100mock
import org.junit.Assert.*
import org.junit.Test
class MockAvailabilityTest {
    private fun paper(id:Int,status:String="available") =
        MockTest(id,"Mock $id","Practice","Balanced",id<=2,status=status)
    @Test fun comingSoonIsNotPlayable() {
        assertFalse(MockAvailability.canStart(paper(1,"upcoming"),emptySet()))
        assertFalse(MockAvailability.canStart(paper(51,"upcoming"),setOf(51)))
        assertFalse(MockAvailability.canWatchReward(paper(51,"upcoming"),emptySet()))
        assertFalse(MockAvailability.canWatchReward(paper(3,"draft"),emptySet()))
    }
    @Test fun availableFollowsRewardsAndFreeAccess() {
        assertTrue(MockAvailability.canStart(paper(1),emptySet()))
        assertFalse(MockAvailability.canStart(paper(51),emptySet()))
        assertTrue(MockAvailability.canWatchReward(paper(51),emptySet()))
        assertTrue(MockAvailability.canStart(paper(51),setOf(51)))
    }
}
