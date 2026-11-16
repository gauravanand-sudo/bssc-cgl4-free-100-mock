package com.bssc.cgl4.free100mock

import org.junit.Assert.*
import org.junit.Test

class HomeCardPolicyTest {
    @Test fun onlySupportedLayoutsAndActionsAllowed() {
        assertTrue(HomeCardPolicy.types.contains("metric"))
        assertTrue(HomeCardPolicy.layouts.contains("horizontal"))
        assertFalse(HomeCardPolicy.layouts.contains("html"))
        assertTrue(HomeCardPolicy.routes.contains("mock_list"))
        assertFalse(HomeCardPolicy.routes.contains("javascript:alert(1)"))
    }
    @Test fun cutoffClaimsAreExplicit() {
        assertTrue("expected" in HomeCardPolicy.claims)
        assertTrue("official" in HomeCardPolicy.claims)
        assertFalse("verified by us" in HomeCardPolicy.claims)
    }
    @Test fun requiredFieldsDefaultToKnownFiniteValues() {
        assertTrue(HomeCardPolicy.MAX_VISIBLE in 1..100)
        assertTrue(HomeCardPolicy.placements.containsAll(listOf("top","middle","bottom")))
    }
}
