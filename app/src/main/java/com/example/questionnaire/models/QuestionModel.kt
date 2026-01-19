package com.example.questionnaire.models

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
//import com.google.gson.Gson

data class QuestionModel (
    val question: String,
    val answers: List<String> = emptyList(),
    val correctAnswerIndex: Int
)
