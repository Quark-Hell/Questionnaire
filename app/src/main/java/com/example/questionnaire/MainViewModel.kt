import androidx.lifecycle.ViewModel
import com.example.questionnaire.QuestionItem
import com.example.questionnaire.TestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    var currentScreen = MutableStateFlow(0)

    val questionsList = MutableStateFlow<List<QuestionItem>>(emptyList())

    // тест
    val isTestStarted = MutableStateFlow(false)
    val currentQuestionIndex = MutableStateFlow(0)
    val displayedQuestions = MutableStateFlow<List<QuestionItem>>(emptyList())
    val selectedAnswerIndex = MutableStateFlow<Int?>(null)
    val testResults = MutableStateFlow<List<TestResult>>(emptyList())
}
