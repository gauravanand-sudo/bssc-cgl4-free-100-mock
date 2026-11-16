package com.bssc.cgl4.free100mock

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Factual engagement derived only from completed local mock attempts. */
object PrepMomentum {
    private fun dateKey(timestamp:Long):String =
        SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(Date(timestamp))

    fun completedToday(attempts:List<Attempt>,now:Long=System.currentTimeMillis()):Boolean =
        attempts.any { dateKey(it.completedAt)==dateKey(now) }

    fun currentStreak(attempts:List<Attempt>,now:Long=System.currentTimeMillis()):Int {
        if(attempts.isEmpty()) return 0
        val days=attempts.map{dateKey(it.completedAt)}.toSet()
        val calendar=Calendar.getInstance().apply {
            timeInMillis=now
            set(Calendar.HOUR_OF_DAY,12)
            set(Calendar.MINUTE,0)
            set(Calendar.SECOND,0)
            set(Calendar.MILLISECOND,0)
        }
        if(dateKey(calendar.timeInMillis) !in days)
            calendar.add(Calendar.DAY_OF_YEAR,-1)
        var streak=0
        while(dateKey(calendar.timeInMillis) in days){
            streak++
            calendar.add(Calendar.DAY_OF_YEAR,-1)
        }
        return streak
    }
    fun accuracyPercent(attempts:List<Attempt>):Int? {
        val correct=attempts.sumOf{it.correct}
        val answered=attempts.sumOf{it.correct+it.wrong}
        return if(answered==0)null else (correct*100f/answered).toInt()
    }
    fun chronologicalScores(attempts:List<Attempt>,limit:Int=8):List<Int> =
        attempts.sortedBy{it.completedAt}.takeLast(limit).map{it.score}
}
