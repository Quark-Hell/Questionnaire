package com.example.questionnaire.viewers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import com.example.questionnaire.components.ClassicButton
import com.example.questionnaire.components.IconButton

import com.example.questionnaire.components.QuestionCard
import com.example.questionnaire.components.ResultQuestionCard

import com.example.questionnaire.models.QuestionModel
import com.example.questionnaire.models.QuestionResult
import com.example.questionnaire.models.TestResult

import com.example.questionnaire.viewModels.MainViewModel
import com.example.questionnaire.viewModels.ResultsViewModel

import java.time.format.DateTimeFormatter
import kotlin.collections.emptyList

sealed class ResultsSubScreen {
    object ResultsListScreen : ResultsSubScreen()
    object WrongAnswerScreen : ResultsSubScreen()
}

@Composable
fun ResultsScreen(
    mainViewModel: MainViewModel,
    resultViewModel: ResultsViewModel
) {
    mainViewModel.setTopBar(emptyList())

    val appColors = LocalAppColors.current

    var currentScreen: ResultsSubScreen by remember { mutableStateOf(ResultsSubScreen.ResultsListScreen) }
    var viewedTestResult by remember { mutableStateOf(TestResult()) }

    Box() {
        when(currentScreen) {
            ResultsSubScreen.ResultsListScreen -> {
                ResultsList(
                    mainViewModel,
                    resultViewModel
                ) {
                    currentScreen = ResultsSubScreen.WrongAnswerScreen
                }
            }
            ResultsSubScreen.WrongAnswerScreen -> {
                ResultDescription(
                    mainViewModel,
                    resultViewModel
                ) {
                    currentScreen = ResultsSubScreen.ResultsListScreen
                }
            }
        }
    }
}

@Composable
fun ResultsList(
    mainViewModel: MainViewModel,
    resultViewModel: ResultsViewModel,
    onSwitchScreen:() -> Unit
) {
    val appColors = LocalAppColors.current

    val testResults by resultViewModel.resulterState.collectAsState()
    val resultItems = testResults.allTestResults

    val scrollState = rememberLazyListState()
    var expandedCardIndex by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxSize()
            .background(color = appColors.standardBackground)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        itemsIndexed(resultItems.asReversed()) { index, result ->
            val cardNumber = resultItems.size - index

            ResultCard(
                testResult = result,
                cardNumber = cardNumber,
                isExpanded = cardNumber == expandedCardIndex,
                onExpandToggle = {
                    expandedCardIndex = if (expandedCardIndex == cardNumber) null else cardNumber
                },
                onOpened = {
                    resultViewModel.setViewedTestResult(result)
                    onSwitchScreen()
                }
            )
        }
    }
}

@Composable
fun ResultDescription(
    mainViewModel: MainViewModel,
    resultViewModel: ResultsViewModel,
    onSwitchScreen:() -> Unit
) {
    val appColors = LocalAppColors.current

    val resultItems by resultViewModel.viewedResultState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = appColors.standardBackground)
    ) {
        val scrollState = rememberLazyListState()

        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(resultItems.testResults) { index, result ->
                ResultQuestionCard(
                    questionIndex = index,
                    questionItem = result
                )
            }
        }

        IconButton(

        ) {
            onSwitchScreen()
        }
    }

}

@Composable
fun ResultCard(
    testResult: TestResult,
    cardNumber: Int,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onOpened: () -> Unit
) {
    val appColors = LocalAppColors.current
    val animDuration: Int = 400

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
            onExpandToggle = onExpandToggle
        )

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = animDuration)
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

                ClassicButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = "Список ответов",
                    cornerRadius = 4.dp,
                    onClickAction = {
                        onOpened()
                    }
                )
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
    val formattedDate = remember(testResult.date) {
        testResult.date.format(formatter)
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