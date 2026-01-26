package com.example.questionnaire.dataBase

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.questionnaire.models.QuestionEntity
import com.example.questionnaire.models.QuestionResult
import com.example.questionnaire.models.TestResultEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromList(list: List<String>): String =
        Gson().toJson(list)

    @TypeConverter
    fun toList(json: String): List<String> =
        Gson().fromJson(
            json,
            object : TypeToken<List<String>>() {}.type
        )

    @TypeConverter
    fun fromQuestionResultList(list: List<QuestionResult>): String {
        return Gson().toJson(list)
    }

    // String (JSON) -> List<QuestionResult>
    @TypeConverter
    fun toQuestionResultList(json: String): List<QuestionResult> {
        val type = object : TypeToken<List<QuestionResult>>() {}.type
        return Gson().fromJson(json, type)
    }


    @TypeConverter
    fun fromLocalDate(date: LocalDate): String =
        date.toString()

    @TypeConverter
    fun toLocalDate(value: String): LocalDate =
        LocalDate.parse(value)
}

@Dao
interface TestResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: TestResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(result: List<TestResultEntity>)

    @Query("SELECT * FROM results ORDER BY date DESC")
    suspend fun getAll(): List<TestResultEntity>

    @Query("DELETE FROM results")
    suspend fun clearAll()
}


@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Query("SELECT * FROM questions")
    suspend fun getAll(): List<QuestionEntity>

    @Query("DELETE FROM questions")
    suspend fun clearAll()
}

@Database(
    entities = [
        QuestionEntity::class,
        TestResultEntity::class
    ],
    version = 1,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun testResultDao(): TestResultDao
    abstract fun questionDao(): QuestionDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}

class QuestionRepository(
    private val questionDao: QuestionDao,
    private val testResultDao: TestResultDao
) {

    suspend fun insertQuestions(questions: List<QuestionEntity>) =
        questionDao.insertAll(questions)

    suspend fun getQuestions(): List<QuestionEntity> =
        questionDao.getAll()

    suspend fun clearQuestions() = questionDao.clearAll()



    suspend fun insertResult(result: TestResultEntity) =
        testResultDao.insert(result)
    suspend fun insertAllResults(result: List<TestResultEntity>) =
        testResultDao.insertAll(result)

    suspend fun getAllResults(): List<TestResultEntity> =
        testResultDao.getAll()

    suspend fun clearResults() =
        testResultDao.clearAll()
}




