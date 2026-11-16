package com.bssc.cgl4.free100mock

import org.junit.Assert.*
import org.junit.Test

class StudentNameTest {
    @Test fun acceptsHindiAndEnglishNames() {
        assertEquals("Rahul Kumar",StudentName.normalize("   Rahul   Kumar   "))
        assertEquals("राहुल कुमार",StudentName.normalize(" राहुल कुमार "))
    }
    @Test fun rejectsTooShortAndControlChars() {
        assertNull(StudentName.normalize(" "))
        assertNull(StudentName.normalize("A"))
        assertNull(StudentName.normalize("Name\nExtra"))
        assertNull(StudentName.normalize("01234567890123456789012345678"))
    }
}
