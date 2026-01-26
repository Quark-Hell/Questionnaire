package com.example.questionnaire.viewModels

import androidx.lifecycle.ViewModel
import com.example.questionnaire.models.QuestionModel
import com.example.questionnaire.models.QuestionResult
import com.example.questionnaire.models.TestModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class TesterViewModel : ViewModel()  {
    private val _testerState = MutableStateFlow(TestModel())
    val testerState: StateFlow<TestModel> = _testerState

    fun shuffleAnswers(question: QuestionModel): QuestionModel {
        val oldAnswers = question.answers
        val correctAnswer = oldAnswers[question.correctAnswerIndex]

        val newAnswers = oldAnswers.shuffled()

        val newCorrectIndex = newAnswers.indexOf(correctAnswer)

        return question.copy(
            answers = newAnswers,
            correctAnswerIndex = newCorrectIndex
        )
    }

    fun nextQuestion() {
        _testerState.update { current ->
            val questSize = _testerState.value.displayedQuestions.size
            val currQuestIndex = _testerState.value.currentQuestionIndex

            val newQuestIndex = if (currQuestIndex + 1 < questSize)
                currQuestIndex + 1
            else
                currQuestIndex

            current.copy(
                currentQuestionIndex = newQuestIndex,
                selectedAnswerIndex = null
            )
        }
    }

    fun startTest(
        rangeStart: Int,
        rangeEnd: Int,
        questionCount: Int,
        isShuffleQuestions: Boolean,
        isShuffleAnswers: Boolean,
        mainViewModel: MainViewModel
    ) {

        val questList = mainViewModel.mainState.value.questionsList
            .subList(rangeStart, rangeEnd)
            .toList()
            .run {
                if (isShuffleQuestions) shuffled() else this
            }
            .run {
                if (questionCount > 0) takeLast(questionCount) else this
            }
            .map { question ->
                if (isShuffleAnswers) shuffleAnswers(question) else question
            }

        val testRes: List<QuestionResult> = questList.map { question ->
            QuestionResult(
                questionItem = question
            )
        }

        _testerState.update { current ->
            current.copy(
                isTestStarted = true,
                currentQuestionIndex = 0,
                displayedQuestions = testRes,
                selectedAnswerIndex = null
            )
        }
    }

    fun endTest(
        resulterViewModel: ResultsViewModel
    ){
        resulterViewModel.addTestResults(_testerState.value.displayedQuestions)

        _testerState.update { current ->
            current.copy(
                isTestStarted = false,
                currentQuestionIndex = 0,
                displayedQuestions = emptyList(),
                selectedAnswerIndex = null
            )
        }
    }

    fun selectAnswer(
        answerIndex: Int?,
        questionIndex: Int
    ){
        _testerState.update { current ->

            val updatedQuestions = current.displayedQuestions.mapIndexed { index, question ->
                if (index == questionIndex) {
                    question.copy(selectedAnswer = answerIndex)
                } else {
                    question
                }
            }

            current.copy(
                displayedQuestions = updatedQuestions,
                selectedAnswerIndex = answerIndex
            )
        }
    }
}