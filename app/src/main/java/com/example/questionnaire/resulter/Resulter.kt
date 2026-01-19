package com.example.questionnaire.resulter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import com.example.questionnaire.components.QuestionCard

import com.example.questionnaire.main.LocalAppColors
import com.example.questionnaire.main.MainViewModel
import com.example.questionnaire.models.AllTestResult

import com.example.questionnaire.models.QuestionModel
import com.example.questionnaire.models.QuestionResult
import com.example.questionnaire.models.TestResult

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.format.DateTimeFormatter
import kotlin.collections.emptyList

open class ResultsViewModel : ViewModel() {

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



    private val _resulterState = MutableStateFlow(AllTestResult())
    val resulterState: StateFlow<AllTestResult> = _resulterState

    init {
        addTestResults(testData)
        addTestResults(testData)
        addTestResults(testData)
        addTestResults(testData)
    }

    fun addTestResults(testResults: List<QuestionResult>){
        val rightAnswered = testResults.count { it.selectedAnswer == it.questionItem.correctAnswerIndex }
        val wrongAnswered = testResults.count { it.selectedAnswer != it.questionItem.correctAnswerIndex && it.selectedAnswer != null }
        val unanswered = testResults.count() { it.selectedAnswer == null }

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
    }

    private fun addTestResults(testResults: TestResult){
        _resulterState.update { current ->
            current.copy(
                allTestResults = current.allTestResults + testResults
            )
        }
    }
}

@Composable
fun ResultsScreen(
    mainViewModel: MainViewModel,
    resultViewModel: ResultsViewModel
) {
    mainViewModel.setTopBar(emptyList())

    val testResults by resultViewModel.resulterState.collectAsState()
    val resultItems = testResults.allTestResults

    val appColors = LocalAppColors.current

    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = appColors.standardBackground,
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        resultItems.asReversed().forEachIndexed { index, testResult ->
            val cardNumber: Int = resultItems.size - index

            item {
                ResultCard(
                    testResult = testResult,
                    cardNumber = cardNumber
                ) {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(appColors.highlights)
                            .padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        testResult.testResults.forEachIndexed { questionIndex, questionResult ->
                            WrongAnswer(
                                questionIndex = questionIndex,
                                questionItem = questionResult.questionItem
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultCard(
    testResult: TestResult,
    cardNumber: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    val appColors = LocalAppColors.current

    var isExpanded by remember { mutableStateOf(false) }
    val animDuration: Int = 800


    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = appColors.thirdBackground
        )
    ) {
        CardTitle(
            testResult,
            cardNumber,
            isExpanded,
            onExpandToggle = { isExpanded = !isExpanded }
        )

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = animDuration) // длительность раскрытия
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = animDuration)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val itemModifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(appColors.highlights)
                    .padding(4.dp)

                TestInfo(
                    testResult,
                    modifier = itemModifier
                )

                TestInfoTitle(modifier = itemModifier)

                content()
            }
        }

        AnimatedVisibility(
            visible = !isExpanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = animDuration)
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = animDuration)
            )
        ){
            //bottom
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)
                    .background(appColors.thirdBackground)
            )
        }
    }
}

@Composable
fun WrongAnswer(
    questionIndex: Int,
    questionItem: QuestionModel
){
    QuestionCard(
        questionIndex = questionIndex,
        questionItem = questionItem
    )
}

@Composable
fun TestInfoTitle(
    modifier: Modifier
){
    val appColors = LocalAppColors.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Список неправильных ответов",
            style = MaterialTheme.typography.titleMedium,
            color = appColors.black
        )
    }
}

@Composable
fun TestInfo(
    testResult: TestResult,
    modifier: Modifier
){
    val appColors = LocalAppColors.current

    val answerRow: @Composable (label: String, value: Int) -> Unit = { label, value ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = appColors.black
            )

            Text(
                text = "$value",
                style = MaterialTheme.typography.bodySmall,
                color = appColors.black
            )
        }
    }

    Column(
        modifier = modifier
    ) {
        val right = if (testResult.rightAnswerCount == -1) 0 else testResult.rightAnswerCount
        val wrong = if (testResult.wrongAnswerCount == -1) 0 else testResult.wrongAnswerCount
        val unanswered = if (testResult.wrongAnswerCount == -1) 0 else testResult.unansweredCount

        answerRow("Правильных ответов: ", right)
        answerRow("Неправильных ответов: ", wrong)
        answerRow("Неотвеченных ответов: ", unanswered)
    }
}

@Composable
fun CardTitle(
    testResult: TestResult,
    cardNumber: Int,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit
){
    val appColors = LocalAppColors.current

    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val formattedDate = remember(testResult.data) {
        testResult.data.format(formatter)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(appColors.secondaryBackground)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column() {
            Text(
                "Результаты теста №$cardNumber",
                style = MaterialTheme.typography.titleMedium,
                color = appColors.whiteText
            )
            Text(
                "Дата: $formattedDate",
                style = MaterialTheme.typography.titleMedium,
                color = appColors.whiteText
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                modifier = Modifier
                    .size(60.dp),
                onClick = { onExpandToggle() }
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = appColors.thirdBackground,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}