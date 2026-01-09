package com.example.questionnaire.tester

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.questionnaire.docxParser.QuestionItem
import com.example.questionnaire.main.LocalAppColors
import com.example.questionnaire.main.MainViewModel
import com.example.questionnaire.main.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class TestModel (
    val isTestStarted: Boolean = false,
    val currentQuestionIndex: Int = 0,
    val displayedQuestions: List<QuestionItem> = emptyList(),
    val selectedAnswerIndex: Int? = null
)

open class TesterViewModel : ViewModel()  {
    private val _testerState = MutableStateFlow(TestModel())
    val testerState: StateFlow<TestModel> = _testerState

    fun startTest(
        rangeStart: Int,
        rangeEnd: Int,
        isShuffleQuestions: Boolean,
        mainViewModel: MainViewModel
    ) {
        val questList = if(isShuffleQuestions)
            mainViewModel.mainState.value.questionsList.subList(rangeStart, rangeEnd).shuffled()
        else
            mainViewModel.mainState.value.questionsList.subList(rangeStart, rangeEnd)

        _testerState.update { current ->
            current.copy(
                isTestStarted = true,
                currentQuestionIndex = 0,
                displayedQuestions = questList,
                selectedAnswerIndex = null
            )
        }
    }
}

@Composable
fun TestSettingsScreen(
    mainViewModel: MainViewModel,
    testerViewModel: TesterViewModel,
    modifier: Modifier = Modifier
) {
    if (testerViewModel.testerState.collectAsState().value.isTestStarted) {
        TestingScreen(
            mainViewModel = mainViewModel,
            testerViewModel = testerViewModel,
            modifier = modifier)
        return
    }

    val mainState by mainViewModel.mainState.collectAsState()

    var rangeStart by remember { mutableStateOf("") }
    var rangeEnd by remember { mutableStateOf("") }
    var shuffleQuestions by remember { mutableStateOf(false) }

    val fromStateTotal = mainState.questionsList.size

    val start = rangeStart.toIntOrNull()
    val end = rangeEnd.toIntOrNull()

    val validationError = when {
        rangeStart.isBlank() || rangeEnd.isBlank() ->
            "Укажите диапазон вопросов"

        start == null || end == null ->
            "Введите корректные номера вопросов"

        start < 1 || end < 1 ->
            "Номера вопросов начинаются с 1"

        start > fromStateTotal || end > fromStateTotal ->
            "В тесте всего $fromStateTotal вопросов"

        start >= end ->
            "Начальный номер должен быть меньше конечного"

        else -> null
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Настройки теста", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextField(
                    value = rangeStart,
                    onValueChange = { rangeStart = it },
                    label = { Text("С вопроса №") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                TextField(
                    value = rangeEnd,
                    onValueChange = { rangeEnd = it },
                    label = { Text("По вопрос №") },
                    modifier = Modifier.weight(1f)
                )
            }

            if (validationError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = validationError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = shuffleQuestions,
                    onCheckedChange = { shuffleQuestions = it }
                )
                Text("Случайный порядок вопросов")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val startIndex = rangeStart.toInt() - 1
                    val endIndex = rangeEnd.toInt() - 1

                    testerViewModel.startTest(
                        rangeStart = startIndex,
                        rangeEnd = endIndex,
                        isShuffleQuestions = shuffleQuestions,
                        mainViewModel = mainViewModel
                    )
                },
                enabled = validationError == null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Начать тестирование")
            }
        }
    }
}

@Composable
fun TestingScreen(
    mainViewModel: MainViewModel,
    testerViewModel: TesterViewModel,
    modifier: Modifier = Modifier
) {
    val mainState by mainViewModel.mainState.collectAsState()
    val testerState by testerViewModel.testerState.collectAsState()

    val questionsCount = testerState.displayedQuestions.size

    val appColors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.standardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val question = testerState.displayedQuestions[testerState.currentQuestionIndex]

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "${mainState.questionsList.indexOf(question) + 1}. ${question.question}",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Top
            ) {
                question.answers.forEachIndexed { index, answer ->
                    val backgroundColor = when {
                        testerState.selectedAnswerIndex == null -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        index == question.correctAnswerIndex -> Color(0xFF4CAF50)
                        index == testerState.selectedAnswerIndex -> Color(0xFFF44336)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                    val contentColor = if (testerState.selectedAnswerIndex == null) {
                        MaterialTheme.colorScheme.onPrimary
                    } else if (index == question.correctAnswerIndex || index == testerState.selectedAnswerIndex) {
                        Color.White
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Button(
                        onClick = {
                            if (testerState.selectedAnswerIndex == null) {
                                //onTestStateChanged(
                                //    isTestStarted,
                                //    currentQuestionIndex,
                                //    displayedQuestions,
                                //    index,
                                //    currentTestResults
                                //)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = backgroundColor,
                            contentColor = contentColor
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(answer)
                    }
                }
            }
            //Spacer(modifier = Modifier.height(16.dp))
        }
    }

//
//                    if (selectedAnswerIndex != null) {
//                        Button(
//                            onClick = {
//                                val newResults = currentTestResults.toMutableList()
//                                newResults.add(
//                                    TestResult(
//                                        question = question,
//                                        originalIndex = questionsList.indexOf(question),
//                                        selectedAnswerIndex = selectedAnswerIndex,
//                                        isCorrect = selectedAnswerIndex == question.correctAnswerIndex
//                                    )
//                                )
//                                if (currentQuestionIndex < displayedQuestions.lastIndex) {
//                                    onTestStateChanged(
//                                        isTestStarted,
//                                        currentQuestionIndex + 1,
//                                        displayedQuestions,
//                                        null,
//                                        newResults
//                                    )
//                                } else {
//                                    onTestComplete(newResults)
//                                }
//                            },
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = MaterialTheme.colorScheme.primary,
//                                contentColor = MaterialTheme.colorScheme.onPrimary
//                            ),
//                            shape = RoundedCornerShape(4.dp),
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            Text(if (currentQuestionIndex < displayedQuestions.lastIndex) "Далее" else "Завершить тест")
//                        }
//                    }
//                }
//
//                Button(
//                    onClick = {
//                        val newResults = currentTestResults.toMutableList()
//                        // Добавляем текущий вопрос если он был отвечен
//                        if (selectedAnswerIndex != null) {
//                            newResults.add(
//                                TestResult(
//                                    question = question,
//                                    originalIndex = questionsList.indexOf(question),
//                                    selectedAnswerIndex = selectedAnswerIndex,
//                                    isCorrect = selectedAnswerIndex == question.correctAnswerIndex
//                                )
//                            )
//                        }
//                        // Добавляем неотвеченные вопросы
//                        for (i in (currentQuestionIndex + if (selectedAnswerIndex != null) 1 else 0) until displayedQuestions.size) {
//                            newResults.add(
//                                TestResult(
//                                    question = displayedQuestions[i],
//                                    originalIndex = questionsList.indexOf(displayedQuestions[i]),
//                                    selectedAnswerIndex = null,
//                                    isCorrect = null
//                                )
//                            )
//                        }
//                        onTestComplete(newResults)
//                    },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color.Gray,
//                        contentColor = Color.White
//                    ),
//                    shape = RoundedCornerShape(4.dp),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(top = 8.dp)
//                ) {
//                    Text("Завершить тест досрочно")
//                }
//            }
//    }
}