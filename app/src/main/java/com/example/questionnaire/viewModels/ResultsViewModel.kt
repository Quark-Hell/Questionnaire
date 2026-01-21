package com.example.questionnaire.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.questionnaire.dataBase.QuestionRepository
import com.example.questionnaire.models.AllTestResult
import com.example.questionnaire.models.QuestionModel
import com.example.questionnaire.models.QuestionResult
import com.example.questionnaire.models.TestResult
import com.example.questionnaire.models.TestResultEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResultsViewModel(
    private val repository: QuestionRepository
) : ViewModel() {

    private fun loadTestResults() {
        val testData = TestResult(
            testResults = listOf(
                QuestionResult(
                    questionItem = QuestionModel(
                        question = "What is the capital of France?",
                        answers = listOf("Berlin", "Paris", "Madrid", "Rome"),
                        correctAnswerIndex = 1
                    ),
                    selectedAnswer = 2
                ),
                QuestionResult(
                    questionItem = QuestionModel(
                        question = "Which planet is known as the Red Planet?",
                        answers = listOf("Earth", "Mars", "Jupiter", "Venus"),
                        correctAnswerIndex = 1
                    ),
                    selectedAnswer = 0
                ),
                QuestionResult(
                    questionItem = QuestionModel(
                        question = "Which language is primarily used for Android development?",
                        answers = listOf("Kotlin", "Swift", "Python", "C#"),
                        correctAnswerIndex = 0
                    ),
                    selectedAnswer = 0
                ),
                QuestionResult(
                    questionItem = QuestionModel(
                        question = "What is 2 + 2?",
                        answers = listOf("3", "4", "5", "22"),
                        correctAnswerIndex = 1
                    ),
                    selectedAnswer = 3
                )
            )
        )

        addTestResults(testData)
        addTestResults(testData)
        addTestResults(testData)
        addTestResults(testData)
    }


    private fun loadSaves() {
        viewModelScope.launch {
            try {
                val resultsFromDb = repository.getAllResults()

                if (resultsFromDb.isNotEmpty()) {
                    // Конвертируем Entity в Model
                    val results = resultsFromDb.map { entity ->
                        TestResult(
                            testResults = entity.testResults,
                            date = entity.date,

                            rightAnswerCount = entity.rightAnswerCount,
                            wrongAnswerCount = entity.wrongAnswerCount,
                            unansweredCount = entity.unansweredCount
                        )
                    }
                    setTestResults(results)
                } else {
                    loadTestResults()
                }
            } catch (e: Exception) {
                loadTestResults()
            }
        }
    }

    fun saveResultsToDatabase(results: List<TestResult>) {
        viewModelScope.launch {
            try {
                val entities = results.map { model ->
                    TestResultEntity(
                        testResults = model.testResults,
                        date = model.date,

                        rightAnswerCount = model.rightAnswerCount,
                        wrongAnswerCount = model.wrongAnswerCount,
                        unansweredCount = model.unansweredCount
                    )
                }
                repository.insertAllResults(entities)
            } catch (e: Exception) {

            }
        }
    }

    fun saveResultsToDatabase(results: TestResult) {
        viewModelScope.launch {
            try {
                val entity = TestResultEntity(
                    testResults = results.testResults,
                    date = results.date,
                    rightAnswerCount = results.rightAnswerCount,
                    wrongAnswerCount = results.wrongAnswerCount,
                    unansweredCount = results.unansweredCount
                )

                repository.insertResult(entity)
            } catch (e: Exception) {

            }
        }
    }

    fun clearDatabase() {
        viewModelScope.launch {
            try {
                repository.clearResults()
            } catch (e: Exception) {

            }
        }
    }



    private val _resulterState = MutableStateFlow(AllTestResult())
    val resulterState: StateFlow<AllTestResult> = _resulterState

    init {
        loadSaves()
    }

    private fun setTestResults(testResults: List<TestResult>){
        _resulterState.update { current ->
            current.copy(
                allTestResults = current.allTestResults + testResults
            )
        }
    }

    fun addTestResults(testResults: List<QuestionResult>){
        val rightAnswered = testResults.count { it.selectedAnswer == it.questionItem.correctAnswerIndex }
        val wrongAnswered = testResults.count { it.selectedAnswer != it.questionItem.correctAnswerIndex && it.selectedAnswer != null }
        val unanswered = testResults.count { it.selectedAnswer == null }

        val test = TestResult(
            rightAnswerCount = rightAnswered,
            wrongAnswerCount = wrongAnswered,
            unansweredCount = unanswered,
            testResults = testResults
        )

        _resulterState.update { current ->
            current.copy(
                allTestResults = current.allTestResults + test
            )
        }

        saveResultsToDatabase(test);
    }

    private fun addTestResults(testResults: TestResult){
        _resulterState.update { current ->
            current.copy(
                allTestResults = current.allTestResults + testResults
            )
        }

        saveResultsToDatabase(testResults);
    }
}