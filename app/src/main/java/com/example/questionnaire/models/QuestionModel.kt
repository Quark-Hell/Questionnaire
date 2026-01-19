package com.example.questionnaire.models

import androidx.room.Entity
import androidx.room.PrimaryKey

data class QuestionModel (
    val question: String,
    val answers: List<String> = emptyList(),
    val correctAnswerIndex: Int
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val question: String,
    val answers: List<String>,
    val correctAnswerIndex: Int
)