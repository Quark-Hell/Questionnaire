package com.example.questionnaire.models

data class TestModel (
    val isTestStarted: Boolean = false,
    val currentQuestionIndex: Int = 0,
    val displayedQuestions: List<QuestionResult> = emptyList(),
    val selectedAnswerIndex: Int? = null
)
