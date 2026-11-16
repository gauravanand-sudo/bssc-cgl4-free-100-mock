package com.bssc.cgl4.free100mock
/** A visible Upcoming listing never enables a mock or consumes an ad reward. */
object MockAvailability {
    fun canStart(test:MockTest,unlocked:Set<Int>):Boolean =
        !test.isUpcoming && MockAccess.isAccessible(test.id,unlocked)
    fun canWatchReward(test:MockTest,unlocked:Set<Int>):Boolean =
        !test.isUpcoming && MockAccess.isRewarded(test.id) && test.id !in unlocked
}
