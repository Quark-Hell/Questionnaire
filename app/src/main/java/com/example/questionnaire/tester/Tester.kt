package com.example.questionnaire.tester

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel

import com.example.questionnaire.main.LocalAppColors
import com.example.questionnaire.main.MainViewModel
import com.example.questionnaire.models.QuestionModel
import com.example.questionnaire.models.QuestionResult
import com.example.questionnaire.models.TestModel
import com.example.questionnaire.resulter.ResultsViewModel
import kotlinx.coroutines.delay

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
open class TesterViewModel : ViewModel()  {
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

@Composable
fun TesterScreen(
    mainViewModel: MainViewModel,
    testerViewModel: TesterViewModel,
    resulterViewModel: ResultsViewModel
) {
    mainViewModel.setTopBar(emptyList())

    val isStarted = testerViewModel.testerState.collectAsState().value.isTestStarted

    if (isStarted) {
        TestingScreen(
            mainViewModel = mainViewModel,
            testerViewModel = testerViewModel,
            resulterViewModel = resulterViewModel
        )
    }
    else {
        TestSettingsScreen(
            mainViewModel = mainViewModel,
            testerViewModel = testerViewModel
        )
    }
}

@Composable
fun TestSettingsScreen(
    mainViewModel: MainViewModel,
    testerViewModel: TesterViewModel,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current

    val mainState by mainViewModel.mainState.collectAsState()

    var rangeStart by remember { mutableStateOf("") }
    var rangeEnd by remember { mutableStateOf("") }
    var questionCount by remember { mutableStateOf("") }

    var shuffleQuestions by remember { mutableStateOf(false) }
    var shuffleAnswers by remember { mutableStateOf(false) }

    val fromStateTotal = mainState.questionsList.size

    val start = rangeStart.toIntOrNull()
    val end = rangeEnd.toIntOrNull()
    val questionsRange = questionCount.toIntOrNull()

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

        questionsRange != null && questionsRange - 1 > end - start ->
            "количество вопросов больше диапазона"

        questionsRange != null && questionsRange < 0 ->
            "количество вопросов не может быть отрицательным"

        else -> null
    }

    //Settings screen
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        //Categories column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TesterInputField("С вопроса №", rangeStart) { input ->
                rangeStart = input.filter { it.isDigit() } }
            Spacer(modifier = Modifier.height(12.dp))

            TesterInputField("По вопрос №", rangeEnd) { input ->
                rangeEnd = input.filter { it.isDigit() } }
            Spacer(modifier = Modifier.height(12.dp))

            TesterInputField("Количество вопросов №", questionCount,
                shuffleQuestions
            ) { input ->
                questionCount = input.filter { it.isDigit() } }
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier.height(16.dp)
            ) {
                if (validationError != null) {
                    Text(
                        text = validationError,
                        color = appColors.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Column(

            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsSwitch(
                        checked = shuffleQuestions,
                        onCheckedChange = { shuffleQuestions = it },
                    )

                    Text("Перемешать вопросы", color = appColors.black)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsSwitch(
                        checked = shuffleAnswers,
                        onCheckedChange = { shuffleAnswers = it },
                    )

                    Text("Перемешать ответы", color = appColors.black)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            StartTestButton(
                validationError == null,
                onClick = {
                    val startIndex = rangeStart.toInt() - 1
                    val endIndex = rangeEnd.toInt()
                    val questRange = questionsRange ?: 0

                    testerViewModel.startTest(
                        rangeStart = startIndex,
                        rangeEnd = endIndex,
                        questionCount = questRange,
                        isShuffleQuestions = shuffleQuestions,
                        isShuffleAnswers = shuffleAnswers,
                        mainViewModel = mainViewModel
                    )
                }
            )
        }
    }
}

@Composable
fun SettingsSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val appColors = LocalAppColors.current

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = appColors.thirdBackground,
            uncheckedThumbColor = appColors.focused,
            checkedTrackColor = appColors.thirdBackground.copy(alpha = 0.5f),
            uncheckedTrackColor = appColors.focused.copy(alpha = 0.25f),
            uncheckedBorderColor = appColors.focused
        ),
        modifier = Modifier
            .padding(horizontal = 20.dp)
    )
}

@Composable
fun StartTestButton(
    isEnabled: Boolean,
    onClick: () -> Unit
){
    val appColors = LocalAppColors.current

    val widthFraction by animateFloatAsState(
        targetValue = if (isEnabled) 0.8f else 0.6f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "ButtonWidthAnimation"
    )

    val animatedContainerColor by animateColorAsState(
        targetValue = if (isEnabled)
            appColors.secondaryBackground
        else
            appColors.focused,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "ButtonColorAnimation"
    )

    Button(
        onClick = onClick,
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = animatedContainerColor,
            contentColor = appColors.whiteText,
            disabledContainerColor = animatedContainerColor,
            disabledContentColor = appColors.whiteText
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth(widthFraction)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text(
                "Начать тест",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = appColors.whiteText
            )

            //Icon(
            //    imageVector = Icons.Default.PlayArrow,
            //    contentDescription = null,
            //    modifier = Modifier
            //        .align(Alignment.CenterStart)
            //        .padding(start = if (isEnabled) 12.dp else 0.dp) // условный отступ
            //        .size(40.dp)
            //)
        }
    }
}
enum class TrailingIconState {
    CHECK, CANCEL
}
@Composable
fun TesterInputField(
    textLabel: String,
    range: String,
    isEnabled: Boolean = true,
    onRangeChange: (String) -> Unit
){
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val appColors = LocalAppColors.current

    var isFocused by remember { mutableStateOf(false) }
    val trailingIconState = when {
        isFocused -> TrailingIconState.CHECK
        range.isNotEmpty() -> TrailingIconState.CANCEL
        else -> null
    }

    if(!isEnabled){
        onRangeChange ("")
    }

    val animatedContainerColor by animateColorAsState(
        targetValue = if (isEnabled)
            appColors.thirdBackground
        else
            appColors.focused,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )
    val animatedContentColor by animateColorAsState(
        targetValue = if (isEnabled)
            appColors.whiteText
        else
            appColors.black,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )

    TextField(
        value = range,
        onValueChange = { onRangeChange (it) },
        label = { Text(textLabel, color = appColors.thirdText) },
        shape = RoundedCornerShape(8.dp),
        trailingIcon = {
            trailingIconState?.let { state ->
                AnimatedContent(
                    targetState = state,
                    transitionSpec = {
                        (fadeIn(tween(250)) + scaleIn(tween(250))) togetherWith
                                (fadeOut(tween(250)) + scaleOut(tween(250)))
                    },
                    label = "TrailingIconAnimation"
                ) { currentState ->
                    when (currentState) {
                        TrailingIconState.CHECK -> {
                            IconButton(
                                onClick = {
                                    focusManager.clearFocus()
                                }
                            ) {
                                Icon(Icons.Default.CheckCircle, null)
                            }
                        }

                        TrailingIconState.CANCEL -> {
                            IconButton(
                                onClick = {
                                    onRangeChange("")
                                    focusManager.clearFocus()
                                }
                            ) {
                                Icon(Icons.Default.Cancel, null)
                            }
                        }
                    }
                }
            }
        },
        enabled = isEnabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = appColors.thirdBackground,
            unfocusedContainerColor = appColors.thirdBackground,

            focusedIndicatorColor = appColors.secondaryBackground,
            unfocusedIndicatorColor = Color.Transparent,

            focusedTextColor = appColors.black,
            unfocusedTextColor = appColors.gray,

            focusedTrailingIconColor = appColors.black,
            unfocusedTrailingIconColor = appColors.gray,

            disabledContainerColor = animatedContainerColor,
            disabledTextColor = animatedContentColor,
            disabledTrailingIconColor = animatedContentColor,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
    )
}

@Composable
fun TestingScreen(
    mainViewModel: MainViewModel,
    testerViewModel: TesterViewModel,
    resulterViewModel: ResultsViewModel
) {
    val mainState by mainViewModel.mainState.collectAsState()
    val testerState by testerViewModel.testerState.collectAsState()

    val appColors = LocalAppColors.current

    //All screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.standardBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val questionRes = testerState.displayedQuestions[testerState.currentQuestionIndex]
        val question = questionRes.questionItem

        //Questions + answers
        Column(
            modifier = Modifier
                .fillMaxHeight(0.6f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "${testerState.currentQuestionIndex + 1}. ${question.question}",
                style = MaterialTheme.typography.titleMedium,
                color = appColors.secondaryBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Answers
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                question.answers.forEachIndexed { index, answer ->
                    val buttonState = when {
                        testerState.selectedAnswerIndex == null -> {
                            ButtonState.ENABLED
                        }

                        index == testerState.selectedAnswerIndex &&
                                index == question.correctAnswerIndex -> {
                            ButtonState.RIGHT
                        }

                        index == testerState.selectedAnswerIndex &&
                                index != question.correctAnswerIndex -> {
                            ButtonState.WRONG
                        }

                        index == question.correctAnswerIndex -> {
                            ButtonState.RIGHT
                        }

                        else -> {
                            ButtonState.DISABLED
                        }
                    }

                    QuestionButton(
                        onClick = { testerViewModel.selectAnswer(index, testerState.currentQuestionIndex) },
                        buttonState = buttonState,
                        answer = answer,
                        modifier = Modifier.defaultMinSize(minHeight = 60.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        //Next + end test
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(appColors.standardBackground)
        ) {
            var isButtonActivated by remember { mutableStateOf(false) }

            QuestionButton(
                onClick =
                    {
                        testerViewModel.nextQuestion()
                        isButtonActivated = false
                    },
                buttonState = if (testerState.selectedAnswerIndex != null)
                    ButtonState.ENABLED
                else
                    ButtonState.DISABLED,
                answer = "Продолжить",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            QuestionButton(
                onClick = { testerViewModel.endTest(resulterViewModel) },
                buttonState = ButtonState.FAKE_DISABLED,
                isVisuallyActivated = isButtonActivated,
                onActivationChange = { isButtonActivated = it },
                answer = "Завершить тест",
                style = MaterialTheme.typography.titleLarge,
                FontWeight.SemiBold
            )
        }
    }
}

enum class ButtonState{
    ENABLED,
    DISABLED,
    FAKE_DISABLED,
    RIGHT,
    WRONG,
}

@Composable
fun QuestionButton(
    onClick: () -> Unit,
    buttonState: ButtonState,
    isVisuallyActivated: Boolean = false,
    onActivationChange: (Boolean) -> Unit = {},
    answer: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    fontWeight: FontWeight = FontWeight.SemiBold,
    modifier: Modifier = Modifier.height(60.dp)
){
    val appColors = LocalAppColors.current
    var clickCount by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    val defaultButtonColors = ButtonDefaults.buttonColors(
        containerColor = appColors.secondaryBackground,
        contentColor = appColors.whiteText
    )

    val disabledButtonColors = ButtonDefaults.buttonColors(
        disabledContainerColor = appColors.standardBackground,
        disabledContentColor = appColors.focused,
    )

    val fakeDisabledButtonColors = ButtonDefaults.buttonColors(
        containerColor = appColors.standardBackground,
        contentColor = appColors.focused,
    )

    val rightButtonColors = ButtonDefaults.buttonColors(
        containerColor = appColors.green,
        contentColor = appColors.whiteText
    )

    val wrongButtonColors = ButtonDefaults.buttonColors(
        containerColor = appColors.red,
        contentColor = appColors.whiteText
    )

    val doubleClickDelay: Duration = 500.milliseconds
    val disabledDelay: Duration = 5.seconds

    LaunchedEffect(clickCount, lastClickTime) {
        if (clickCount > 0) {
            delay(doubleClickDelay)
            clickCount = 0
        }
    }

    LaunchedEffect(isVisuallyActivated) {
        if (isVisuallyActivated && buttonState == ButtonState.FAKE_DISABLED) {
            delay(disabledDelay)
            onActivationChange(false)
        }
    }

    val effectiveButtonState = if (buttonState == ButtonState.FAKE_DISABLED && isVisuallyActivated) {
        ButtonState.ENABLED
    } else {
        buttonState
    }

    val buttonColors = when (effectiveButtonState) {
        ButtonState.ENABLED -> defaultButtonColors
        ButtonState.DISABLED -> disabledButtonColors
        ButtonState.FAKE_DISABLED -> fakeDisabledButtonColors
        ButtonState.RIGHT -> rightButtonColors
        ButtonState.WRONG -> wrongButtonColors
    }

    val borderModifier = if (
        buttonState == ButtonState.DISABLED ||
        (buttonState == ButtonState.FAKE_DISABLED && !isVisuallyActivated)
        ) {
        Modifier.border(
            width = 2.dp,
            color = appColors.focused,
            shape = RoundedCornerShape(8.dp)
        )
    } else {
        Modifier
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                if (buttonState == ButtonState.FAKE_DISABLED && !isVisuallyActivated) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastClickTime < 500) {
                        onActivationChange(true)
                        clickCount = 0
                    } else {
                        clickCount = 1
                        lastClickTime = currentTime
                    }
                } else {
                    onClick()
                }
            },
            colors = buttonColors,
            shape = RoundedCornerShape(10.dp),
            enabled = buttonState != ButtonState.DISABLED,

            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .dropShadow(
                    shape = RoundedCornerShape(10.dp),
                    shadow = Shadow(
                        radius = 4.dp,
                        spread = 0.dp,
                        color = Color.Black.copy(alpha = 0.25f),
                        offset = DpOffset(x = 0.dp, 4.dp)
                    )
                )
                .then(borderModifier)
        ) {
            Text(
                text = answer,
                textAlign = TextAlign.Center,
                style = style,
                fontWeight = fontWeight,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if(buttonState == ButtonState.FAKE_DISABLED && !isVisuallyActivated){
            Text(
                text = if (clickCount <= 2) "Нажмите дважды, чтобы кнопка стала активной" else "",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Thin,
                color = appColors.secondaryBackground,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}