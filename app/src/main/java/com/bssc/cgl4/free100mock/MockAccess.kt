package com.bssc.cgl4.free100mock

/**
 * Ad-supported launch access:
 * Mocks 1–2 are free; every published mock 3–100 requires one completed
 * rewarded ad to unlock locally on this device (also accepts previously
 * earned per-device rewards). No paid pack or Firebase payment switch in V12.
 *
 * IMPORTANT: Local rewarded unlocks are not tamper-proof. For protected
 * cloud content, add AdMob rewarded server-side verification and authenticated
 * entitlements before claiming verified access control.
 */
object MockAccess {
    fun isFree(id: Int): Boolean = id in 1..2
    fun isRewarded(id: Int): Boolean = id in 3..100
    fun isAccessible(id: Int, unlocked: Set<Int>): Boolean =
        isFree(id) || (isRewarded(id) && id in unlocked)
}
