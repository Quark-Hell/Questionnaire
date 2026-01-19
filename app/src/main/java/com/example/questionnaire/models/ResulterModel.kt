package com.example.questionnaire.models

import java.time.LocalDate

data class QuestionResult (
    val questionItem: QuestionModel,
    val selectedAnswer: Int? = null,
)

data class TestResult(
    val testResults: List<QuestionResult> = emptyList(),
    val data: LocalDate = LocalDate.now(),
    val rightAnswerCount: Int = -1,
    val wrongAnswerCount: Int = -1,
    val unansweredCount: Int = -1,
)

data class AllTestResult (
    val allTestResults: List<TestResult> = emptyList()
)