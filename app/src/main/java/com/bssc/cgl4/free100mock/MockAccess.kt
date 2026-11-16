package com.bssc.cgl4.free100mock

/**
 * BSSC CGL launch policy: mocks 1–2 free; 3–50 require one earned
 * rewarded ad, and 51–100 require two separately opted-in rewarded ads.
 * Each earned ad grants one persistent local credit toward that mock.
 * Completed unlocks are grandfathered from previous app versions.
 *
 * Local rewards are not tamper-proof; use AdMob SSV and backend-verified
 * entitlements before claiming secure server-enforced paper access.
 */
object MockAccess {
    fun isFree(id: Int): Boolean = id in 1..2
    fun isRewarded(id: Int): Boolean = id in 3..100

    fun requiredAds(id: Int): Int = when (id) {
        in 3..50 -> 1
        in 51..100 -> 2
        else -> 0
    }

    fun nextCredit(id: Int, current: Int): Int =
        if (!isRewarded(id)) 0
        else (current.coerceAtLeast(0) + 1).coerceAtMost(requiredAds(id))

    fun isAccessible(id: Int, unlocked: Set<Int>): Boolean =
        isFree(id) || (isRewarded(id) && id in unlocked)
}
