package com.example.questionnaire

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.questionnaire.ui.theme.QuestionnaireTheme
import kotlinx.coroutines.launch

data class TestResult(
    val question: QuestionItem,
    val originalIndex: Int,
    val selectedAnswerIndex: Int?,
    val isCorrect: Boolean?
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QuestionnaireTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun NavigationBar(
    currentScreen: Int,
    onScreenSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonHeight = 56.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val buttons = listOf("Просмотр", "Тестирование", "Результаты")

        buttons.forEachIndexed { index, title ->
            val isSelected = index == currentScreen
            Button(
                onClick = { onScreenSelected(index) },
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                    contentColor = if (isSelected)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSecondary
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    softWrap = true,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    fontSize = if (title.length > 12) 12.sp else 14.sp // уменьшаем текст, если слишком длинный
                )
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf(0) } // 0 - просмотр, 1 - тестирование, 2 - результаты
    val questionsList = remember { mutableStateListOf<QuestionItem>() }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var testResults by remember { mutableStateOf<List<TestResult>>(emptyList()) }

    // Состояние тестирования
    var isTestStarted by remember { mutableStateOf(false) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var displayedQuestions by remember { mutableStateOf(listOf<QuestionItem>()) }
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var currentTestResults by remember { mutableStateOf(mutableListOf<TestResult>()) }

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (currentScreen) {
                0 -> DocumentPickerScreen(
                    questionsList = questionsList,
                    selectedFileName = selectedFileName,
                    onFileSelected = { name -> selectedFileName = name }
                )
                1 -> TestingScreen(
                    questionsList = questionsList,
                    isTestStarted = isTestStarted,
                    currentQuestionIndex = currentQuestionIndex,
                    displayedQuestions = displayedQuestions,
                    selectedAnswerIndex = selectedAnswerIndex,
                    currentTestResults = currentTestResults,
                    onTestStateChanged = { started, index, questions, answer, results ->
                        isTestStarted = started
                        currentQuestionIndex = index
                        displayedQuestions = questions
                        selectedAnswerIndex = answer
                        currentTestResults = results
                    },
                    onTestComplete = { results ->
                        testResults = results
                        isTestStarted = false
                        currentScreen = 2
                    }
                )
                2 -> ResultsScreen(testResults = testResults)
            }
        }

        NavigationBar(
            currentScreen = currentScreen,
            onScreenSelected = { currentScreen = it },
            modifier = Modifier.padding(bottom = 8.dp)
        )

    }
}

@Composable
fun DocumentPickerScreen(
    modifier: Modifier = Modifier,
    questionsList: MutableList<QuestionItem>,
    selectedFileName: String?,
    onFileSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var jumpToNumber by remember { mutableStateOf("") }
    var currentQuestionIndex by remember { mutableStateOf(-1) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = getFileName(context, uri)
            if (name?.endsWith(".docx", ignoreCase = true) == true) {
                onFileSelected(name)
                val parsed = DocxParser.parse(uri, context)
                questionsList.clear()
                questionsList.addAll(parsed)
                currentQuestionIndex = -1
            } else {
                Toast.makeText(context, "Выберите файл .docx", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = { launcher.launch(arrayOf("application/*")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выбрать .docx файл")
        }

        selectedFileName?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Выбран файл: $it", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Количество вопросов: ${questionsList.size}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (questionsList.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = jumpToNumber,
                    onValueChange = { jumpToNumber = it },
                    label = { Text("Номер вопроса") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val questionIndex = jumpToNumber.toIntOrNull()?.minus(1)
                    if (questionIndex != null && questionIndex in questionsList.indices) {
                        coroutineScope.launch {
                            listState.animateScrollToItem(questionIndex)
                            currentQuestionIndex = questionIndex
                        }
                    }
                }) {
                    Text("Перейти")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(questionsList) { questionIndex, item ->
                val isCurrent = questionIndex == currentQuestionIndex
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(
                            if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.background
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${questionIndex + 1}. ${item.question}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    item.answers.forEachIndexed { index, answer ->
                        val isCorrect = index == item.correctAnswerIndex
                        Text(
                            text = "${index + 1}. $answer ${if (isCorrect) "✅" else ""}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TestingScreen(
    questionsList: List<QuestionItem>,
    isTestStarted: Boolean,
    currentQuestionIndex: Int,
    displayedQuestions: List<QuestionItem>,
    selectedAnswerIndex: Int?,
    currentTestResults: MutableList<TestResult>,
    onTestStateChanged: (Boolean, Int, List<QuestionItem>, Int?, MutableList<TestResult>) -> Unit,
    onTestComplete: (List<TestResult>) -> Unit,
    modifier: Modifier = Modifier
) {
    var rangeStart by remember { mutableStateOf("") }
    var rangeEnd by remember { mutableStateOf("") }
    var shuffleQuestions by remember { mutableStateOf(false) }

    val questionsCount = questionsList.size

    val startNumber = rangeStart.toIntOrNull()
    val endNumber = rangeEnd.toIntOrNull()

    val isRangeValid = remember(rangeStart, rangeEnd, questionsCount) {
        when {
            startNumber == null || endNumber == null -> false
            startNumber < 1 || endNumber < 1 -> false
            startNumber > questionsCount || endNumber > questionsCount -> false
            startNumber >= endNumber -> false
            else -> true
        }
    }

    val validationError = when {
        rangeStart.isBlank() || rangeEnd.isBlank() ->
            "Укажите диапазон вопросов"
        startNumber == null || endNumber == null ->
            "Введите корректные номера вопросов"
        startNumber < 1 || endNumber < 1 ->
            "Номера вопросов начинаются с 1"
        startNumber > questionsCount || endNumber > questionsCount ->
            "В тесте всего $questionsCount вопросов"
        startNumber >= endNumber ->
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
            if (!isTestStarted) {
                Text("Настройки теста", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
                        val startIndex = startNumber!! - 1
                        val endIndex = endNumber!! - 1

                        val newDisplayedQuestions = questionsList.subList(
                            startIndex,
                            endIndex + 1
                        )

                        val finalQuestions = if (shuffleQuestions) {
                            newDisplayedQuestions.shuffled()
                        } else {
                            newDisplayedQuestions
                        }

                        onTestStateChanged(
                            true,
                            0,
                            finalQuestions,
                            null,
                            mutableListOf()
                        )
                    },
                    enabled = isRangeValid,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Начать тестирование")
                }
            } else {
                val question = displayedQuestions[currentQuestionIndex]

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "${questionsList.indexOf(question) + 1}. ${question.question}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        question.answers.forEachIndexed { index, answer ->
                            val backgroundColor = when {
                                selectedAnswerIndex == null -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                index == question.correctAnswerIndex -> Color(0xFF4CAF50)
                                index == selectedAnswerIndex -> Color(0xFFF44336)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }

                            val contentColor = if (selectedAnswerIndex == null) {
                                MaterialTheme.colorScheme.onPrimary
                            } else if (index == question.correctAnswerIndex || index == selectedAnswerIndex) {
                                Color.White
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Button(
                                onClick = {
                                    if (selectedAnswerIndex == null) {
                                        onTestStateChanged(
                                            isTestStarted,
                                            currentQuestionIndex,
                                            displayedQuestions,
                                            index,
                                            currentTestResults
                                        )
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

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedAnswerIndex != null) {
                        Button(
                            onClick = {
                                val newResults = currentTestResults.toMutableList()
                                newResults.add(
                                    TestResult(
                                        question = question,
                                        originalIndex = questionsList.indexOf(question),
                                        selectedAnswerIndex = selectedAnswerIndex,
                                        isCorrect = selectedAnswerIndex == question.correctAnswerIndex
                                    )
                                )
                                if (currentQuestionIndex < displayedQuestions.lastIndex) {
                                    onTestStateChanged(
                                        isTestStarted,
                                        currentQuestionIndex + 1,
                                        displayedQuestions,
                                        null,
                                        newResults
                                    )
                                } else {
                                    onTestComplete(newResults)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (currentQuestionIndex < displayedQuestions.lastIndex) "Далее" else "Завершить тест")
                        }
                    }
                }

                Button(
                    onClick = {
                        val newResults = currentTestResults.toMutableList()
                        // Добавляем текущий вопрос если он был отвечен
                        if (selectedAnswerIndex != null) {
                            newResults.add(
                                TestResult(
                                    question = question,
                                    originalIndex = questionsList.indexOf(question),
                                    selectedAnswerIndex = selectedAnswerIndex,
                                    isCorrect = selectedAnswerIndex == question.correctAnswerIndex
                                )
                            )
                        }
                        // Добавляем неотвеченные вопросы
                        for (i in (currentQuestionIndex + if (selectedAnswerIndex != null) 1 else 0) until displayedQuestions.size) {
                            newResults.add(
                                TestResult(
                                    question = displayedQuestions[i],
                                    originalIndex = questionsList.indexOf(displayedQuestions[i]),
                                    selectedAnswerIndex = null,
                                    isCorrect = null
                                )
                            )
                        }
                        onTestComplete(newResults)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Завершить тест досрочно")
                }
            }
        }
    }
}

@Composable
fun ResultsScreen(
    testResults: List<TestResult>,
    modifier: Modifier = Modifier
) {
    val correctAnswers = testResults.count { it.isCorrect == true }
    val incorrectAnswers = testResults.count { it.isCorrect == false }
    val unanswered = testResults.count { it.isCorrect == null }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Результаты тестирования", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Правильных ответов: $correctAnswers", style = MaterialTheme.typography.bodyLarge)
                Text("Неправильных ответов: $incorrectAnswers", style = MaterialTheme.typography.bodyLarge)
                if (unanswered > 0) {
                    Text("Неотвеченных вопросов: $unanswered", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            if (correctAnswers > 0) {
                item {
                    Text(
                        "✅ Правильные ответы",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                itemsIndexed(testResults.filter { it.isCorrect == true }) { _, result ->
                    ResultCard(result = result, color = Color(0xFF4CAF50))
                }
            }

            if (incorrectAnswers > 0) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "❌ Неправильные ответы",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFF44336)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                itemsIndexed(testResults.filter { it.isCorrect == false }) { _, result ->
                    ResultCard(result = result, color = Color(0xFFF44336))
                }
            }

            if (unanswered > 0) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "⏭️ Неотвеченные вопросы",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                itemsIndexed(testResults.filter { it.isCorrect == null }) { _, result ->
                    ResultCard(result = result, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ResultCard(result: TestResult, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${result.originalIndex + 1}. ${result.question.question}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            result.selectedAnswerIndex?.let { selected ->
                Text(
                    text = "Ваш ответ: ${result.question.answers[selected]}",
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
            } ?: Text(
                text = "Не отвечено",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            if (result.isCorrect == false) {
                Text(
                    text = "Правильный ответ: ${result.question.answers[result.question.correctAnswerIndex]}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

fun getFileName(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst()) return it.getString(nameIndex)
    }
    return uri.lastPathSegment
}