package com.bssc.cgl4.free100mock

import org.junit.Assert.*
import org.junit.Test

class IndianMobileTest {
    @Test fun acceptsIndianMobileWithoutCountryCode() {
        assertEquals("+919876543210", IndianMobile.normalize("9876543210"))
    }
    @Test fun acceptsExistingIndianCountryCode() {
        assertEquals("+919876543210", IndianMobile.normalize("+91 98765 43210"))
    }
    @Test fun rejectsInvalidOrIncompleteNumber() {
        assertNull(IndianMobile.normalize("1234567890"))
        assertNull(IndianMobile.normalize("987654321"))
        assertNull(IndianMobile.normalize("9198765432100"))
    }
}
