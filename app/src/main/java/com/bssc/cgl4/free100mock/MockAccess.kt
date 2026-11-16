package com.bssc.cgl4.free100mock

/** Free mocks 1–2; rewarded 3–49; Firebase switches 50–100 between
 *  rewarded and ₹25 premium mode. The ₹25 checkout is not integrated yet.
 *  Client-side ad rewards are NOT a production server-verified entitlement.
 */
object MockAccess {
    data class Mode(val premiumEnabled: Boolean = false, val verified: Boolean = false)
    enum class Tier { FREE, REWARDED, PREMIUM, CHECKING, INVALID }
    fun isFree(id: Int) = id in 1..2
    fun tier(id: Int, mode: Mode): Tier = when {
        id in 1..2 -> Tier.FREE
        id in 3..49 -> Tier.REWARDED
        id in 50..100 && !mode.verified -> Tier.CHECKING
        id in 50..100 && mode.premiumEnabled -> Tier.PREMIUM
        id in 50..100 -> Tier.REWARDED
        else -> Tier.INVALID
    }
    fun isRewarded(id: Int, mode: Mode) = tier(id, mode) == Tier.REWARDED
    fun isPremium(id: Int, mode: Mode) = tier(id, mode) == Tier.PREMIUM
    fun isAccessible(id: Int, rewardedUnlocked: Set<Int>, mode: Mode): Boolean =
        when (tier(id, mode)) {
            Tier.FREE -> true
            Tier.REWARDED -> id in rewardedUnlocked
            else -> false
        }
}
