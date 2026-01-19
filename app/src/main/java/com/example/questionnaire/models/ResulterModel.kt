package com.example.questionnaire.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

data class QuestionResult (
    val questionItem: QuestionModel,
    val selectedAnswer: Int? = null,
)

data class TestResult(
    val testResults: List<QuestionResult> = emptyList(),
    val date: LocalDate = LocalDate.now(),
    val rightAnswerCount: Int = -1,
    val wrongAnswerCount: Int = -1,
    val unansweredCount: Int = -1,
)

data class AllTestResult (
    val allTestResults: List<TestResult> = emptyList()
)

@Entity(tableName = "results")
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val testResults: List<QuestionResult>,
    val date: LocalDate,

    val rightAnswerCount: Int,
    val wrongAnswerCount: Int,
    val unansweredCount: Int
)