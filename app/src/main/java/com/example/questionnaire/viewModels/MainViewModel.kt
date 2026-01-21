package com.example.questionnaire.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.questionnaire.dataBase.QuestionRepository
import com.example.questionnaire.viewers.MainModel
import com.example.questionnaire.viewers.Screen
import com.example.questionnaire.viewers.TopBarItem
import com.example.questionnaire.models.QuestionEntity
import com.example.questionnaire.models.QuestionModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: QuestionRepository
) : ViewModel() {

    private fun loadSaves() {
        viewModelScope.launch {
            try {
                val questionsFromDb = repository.getQuestions()

                if (questionsFromDb.isNotEmpty()) {
                    // Конвертируем Entity в Model
                    val questions = questionsFromDb.map { entity ->
                        QuestionModel(
                            question = entity.question,
                            answers = entity.answers,
                            correctAnswerIndex = entity.correctAnswerIndex
                        )
                    }
                    setQuestions(questions, "loaded_from_db")
                } else {
                    loadTestQuestions()
                }
            } catch (e: Exception) {
                loadTestQuestions()
            }
        }
    }

    private fun loadTestQuestions() {
        val testQuestions = listOf(
            QuestionModel(
                question = "Какой язык используется для разработки под Android?",
                answers = listOf("Kotlin", "Swift", "JavaScript", "Python"),
                correctAnswerIndex = 0
            ),
            QuestionModel(
                question = "Какой компонент используется для компоновки элементов по горизонтали в Compose?",
                answers = listOf("Column", "Box", "Row", "LazyColumn"),
                correctAnswerIndex = 2
            ),
            QuestionModel(
                question = "Что такое ViewModel в Android?",
                answers = listOf(
                    "UI-компонент",
                    "Класс для хранения состояния экрана",
                    "Сервис Android",
                    "База данных"
                ),
                correctAnswerIndex = 1
            ),
            QuestionModel(
                question = "Какой метод используется для подписки на StateFlow в Compose?",
                answers = listOf("collect()", "observe()", "collectAsState()", "subscribe()"),
                correctAnswerIndex = 2
            )
        )

        setQuestions(testQuestions, "test_questions")
    }

    fun saveQuestionsToDatabase(questions: List<QuestionModel>) {
        viewModelScope.launch {
            try {
                val entities = questions.map { model ->
                    QuestionEntity(
                        question = model.question,
                        answers = model.answers,
                        correctAnswerIndex = model.correctAnswerIndex
                    )
                }
                repository.insertQuestions(entities)
            } catch (e: Exception) {

            }
        }
    }

    fun clearDatabase() {
        viewModelScope.launch {
            try {
                repository.clearQuestions()
            } catch (e: Exception) {

            }
        }
    }

    private val _mainState = MutableStateFlow(MainModel())
    val mainState: StateFlow<MainModel> = _mainState

    init {
        loadSaves()
    }

    fun setTopBar(items: List<TopBarItem>) {
        _mainState.update {
            it.copy(topBarItems = items)
        }
    }

    fun clearTopBar() {
        setTopBar(emptyList())
    }

    fun navigateTo(screen: Screen) {
        _mainState.value = _mainState.value.copy(currentScreen = screen)
    }

    fun setQuestions(questions: List<QuestionModel>, fileName: String?) {
        _mainState.update { current ->
            current.copy(
                questionsList = questions,
                fileName = fileName
            )
        }
    }
}