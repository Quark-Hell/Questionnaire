package com.example.questionnaire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.example.questionnaire.dataBase.AppDatabase
import com.example.questionnaire.dataBase.QuestionRepository
import com.example.questionnaire.ui.theme.QuestionnaireTheme
import com.example.questionnaire.viewModels.CardsViewModel
import com.example.questionnaire.viewModels.MainViewModel
import com.example.questionnaire.viewModels.ResultsViewModel
import com.example.questionnaire.viewModels.TesterViewModel
import com.example.questionnaire.viewModels.ViewerViewModel
import com.example.questionnaire.viewers.AppViewModelFactory
import com.example.questionnaire.viewers.MainScreen
import kotlin.getValue

class MainActivity : ComponentActivity() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "questionnaire_db" // имя базы
        )
            .fallbackToDestructiveMigration() // ⬅ позволяет Room удалять старую БД при изменении схемы
            .build()
    }

    private val repository by lazy {
        QuestionRepository(
            questionDao = database.questionDao(),
            testResultDao = database.testResultDao(),
        )
    }

    private val factory by lazy {
        AppViewModelFactory(repository)
    }

    private val mainViewModel: MainViewModel by viewModels { factory }

    private val testerViewModel: TesterViewModel by viewModels()

    private val viewerViewModel: ViewerViewModel by viewModels()
    private val resultsViewModel: ResultsViewModel by viewModels { factory }

    private val cardsViewModel: CardsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QuestionnaireTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        mainViewModel = mainViewModel,
                        viewerViewModel = viewerViewModel,
                        testerViewModel = testerViewModel,
                        resultsViewModel = resultsViewModel,
                        cardsViewModel = cardsViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}