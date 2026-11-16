package com.bssc.cgl4.free100mock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class PrepMomentumTest {
    private fun at(daysAgo:Int):Long=Calendar.getInstance().apply{
        set(Calendar.HOUR_OF_DAY,12)
        set(Calendar.MINUTE,0)
        set(Calendar.SECOND,0)
        add(Calendar.DAY_OF_YEAR,-daysAgo)
    }.timeInMillis
    private fun a(daysAgo:Int,score:Int=120,correct:Int=40,wrong:Int=40)=
        Attempt(3,at(daysAgo),1400,List(150){-1},score,correct,wrong,70)

    @Test fun streakIsBasedOnRealCalendarDays() {
        val now=at(0)
        assertEquals(3,PrepMomentum.currentStreak(listOf(a(0),a(1),a(2),a(2)),now))
        assertEquals(2,PrepMomentum.currentStreak(listOf(a(1),a(2)),now))
        assertEquals(0,PrepMomentum.currentStreak(listOf(a(2),a(3)),now))
        assertFalse(PrepMomentum.completedToday(listOf(a(1)),now))
        assertTrue(PrepMomentum.completedToday(listOf(a(0)),now))
    }
    @Test fun accuracyAndScoreTrendNeverInventData() {
        assertEquals(null,PrepMomentum.accuracyPercent(emptyList()))
        assertEquals(50,PrepMomentum.accuracyPercent(listOf(a(0),a(1))))
        assertEquals(listOf(100,200),PrepMomentum.chronologicalScores(
            listOf(a(0,200),a(1,100))))
        assertEquals(emptyList<Int>(),PrepMomentum.chronologicalScores(emptyList()))
    }
}
