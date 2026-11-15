package com.bssc.cgl4.free100mock

import org.junit.Assert.assertEquals
import org.junit.Test

class GradingTest {
    private fun question(i: Int) = Question("q$i", "Reasoning", "Series", "Question $i", "प्रश्न $i",
        listOf("A","B","C","D"),listOf("A","B","C","D"),0,"Answer A","उत्तर A")

    @Test fun correctWrongSkippedAndNegativeMarking() {
        val questions = List(150) {question(it)}
        val answers = List(150) {when(it) { in 0..49->0; in 50..89->2;else->-1 }}
        val attempt=Grading.grade(1,0,answers,questions,1_000)
        assertEquals(50,attempt.correct)
        assertEquals(40,attempt.wrong)
        assertEquals(60,attempt.skipped)
        assertEquals(160,attempt.score) // 50*4 - 40
    }
    @Test fun perfectScore() {
        val questions=List(150) {question(it)}
        assertEquals(600,Grading.grade(1,0,List(150){0},questions,0).score)
    }
    @Test fun fullyIncorrectScoreIsNegative150() {
        val questions=List(150){question(it)}
        assertEquals(-150,Grading.grade(1,0,List(150){1},questions,0).score)
    }
}
