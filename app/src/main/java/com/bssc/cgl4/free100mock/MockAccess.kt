package com.bssc.cgl4.free100mock

/** Access policy independent of editable Firestore catalogue flags.
 *  Rewarded unlocks are currently cached per device; production enforcement for
 *  rewarded/premium paper downloads requires a server-verified entitlement.
 */
object MockAccess {
    fun isFree(id: Int) = id in 1..2
    fun isRewarded(id: Int) = id in 3..50
    fun isPremium(id: Int) = id in 51..100
    fun isAccessible(id: Int, rewardedUnlocked: Set<Int>): Boolean =
        isFree(id) || (isRewarded(id) && id in rewardedUnlocked)
}
