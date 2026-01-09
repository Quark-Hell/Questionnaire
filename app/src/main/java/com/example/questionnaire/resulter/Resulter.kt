package com.example.questionnaire.resulter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.collectAsState

import com.example.questionnaire.docxParser.QuestionItem
import com.example.questionnaire.main.LocalAppColors

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class TestResult (
    val question: QuestionItem,
    val originalIndex: Int,
    val selectedAnswerIndex: Int?,
    val isCorrect: Boolean?
)

data class ResulterModel (
    val testResults: List<TestResult> = emptyList()
)

open class ResultsViewModel : ViewModel() {
    private val _resulterState = MutableStateFlow(ResulterModel())
    val resulterState: StateFlow<ResulterModel> = _resulterState
}

@Composable
fun ResultsScreen(
    resultModel: ResultsViewModel,
    modifier: Modifier = Modifier
) {
    val testResults = resultModel.resulterState.collectAsState().value.testResults

    val correctAnswers = testResults.count { it.isCorrect == true }
    val incorrectAnswers = testResults.count { it.isCorrect == false }
    val unanswered = testResults.count { it.isCorrect == null }

    val appColors = LocalAppColors.current

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