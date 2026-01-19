package com.example.questionnaire.main

import androidx.room.Room
import androidx.room.RoomDatabase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels

import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background

import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.ui.Alignment

import androidx.compose.ui.draw.drawBehind

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import com.example.questionnaire.dataBase.AppDatabase
import com.example.questionnaire.dataBase.QuestionRepository

import com.example.questionnaire.models.QuestionModel
import com.example.questionnaire.models.QuestionEntity

import com.example.questionnaire.tester.TesterViewModel
import com.example.questionnaire.ui.theme.QuestionnaireTheme
import com.example.questionnaire.resulter.ResultsScreen
import com.example.questionnaire.resulter.ResultsViewModel
import com.example.questionnaire.tester.TesterScreen
import com.example.questionnaire.viewer.QuestionsScreen
import com.example.questionnaire.viewer.ViewerViewModel
import com.example.questionnaire.viewer.SearchBar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.getValue

data class AppColorScheme (
    val buttonDefault: Color,
    val buttonSelected: Color,
    val buttonUnavailable: Color,

    val standardBackground: Color,
    val secondaryBackground: Color,
    val thirdBackground: Color,

    val black: Color,
    val whiteText: Color,
    val thirdText: Color,

    val gray: Color,
    val red: Color,
    val green: Color,

    val highlights: Color,
    val focused: Color,
    val error: Color,
)

val LocalAppColors = staticCompositionLocalOf<AppColorScheme> {
    error("No AppColorScheme provided")
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val appColors = AppColorScheme(
        buttonDefault = Color(0xFF3D2B27),
        buttonSelected = Color(0xFF5B3F39),
        buttonUnavailable = Color(0xFFBDBDBD),

        standardBackground = Color(0xFFDBCAC0),
        secondaryBackground = Color(0xFF3D2B27),
        thirdBackground = Color(0xFFFFFFFF),

        whiteText = Color(0xFFF5F0EA),
        thirdText = Color(0xFFD4C2B6),

        black = Color(0xFF000000),
        gray = Color(0xFF49454F),
        red = Color(0xFFAA4861),
        green = Color(0xFF839958),

        highlights = Color(0xFFE9D6B1),
        focused = Color(0xFF9E8D83),
        error = Color(0xFF900000)
    )

    CompositionLocalProvider(LocalAppColors provides appColors) {
        content()
    }
}

sealed class TopBarItem {
    object Search : TopBarItem()
}

sealed class Screen {
    object StartScreen : Screen()
    object TestScreen : Screen()
    object ResultsScreen : Screen()
}

data class MainModel (
    var currentScreen: Screen = Screen.StartScreen,
    val questionsList: List<QuestionModel> = emptyList(),
    val topBarItems: List<TopBarItem> = emptyList(),
    val fileName: String? = null,
)

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

class AppViewModelFactory(
    private val repository: QuestionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(MainViewModel::class.java) ->
                MainViewModel(repository) as T

            modelClass.isAssignableFrom(ResultsViewModel::class.java) ->
                ResultsViewModel(repository) as T

            else -> throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
}


class MainActivity : ComponentActivity() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "questionnaire_db" // имя базы
        )
            .fallbackToDestructiveMigration() // ⬅ позволяет Room удалять старую БД при изменении схемы
            .build()
    }

    private val repository by lazy {
        QuestionRepository(
            questionDao = database.questionDao(),
            testResultDao = database.testResultDao(),
        )
    }

    private val factory by lazy {
        AppViewModelFactory(repository)
    }

    private val mainViewModel: MainViewModel by viewModels { factory }

    private val testerViewModel: TesterViewModel by viewModels()

    private val viewerViewModel: ViewerViewModel by viewModels()
    private val resultsViewModel: ResultsViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QuestionnaireTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        mainViewModel = mainViewModel,
                        viewerViewModel = viewerViewModel,
                        testerViewModel = testerViewModel,
                        resultsViewModel = resultsViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    viewerViewModel: ViewerViewModel,
    testerViewModel: TesterViewModel,
    resultsViewModel: ResultsViewModel,
    modifier: Modifier = Modifier
) {
    AppTheme {
        val mainState by mainViewModel.mainState.collectAsState()
        val appColors = LocalAppColors.current

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        SidePanel(
            mainViewModel = mainViewModel,
            drawerState = drawerState
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(appColors.standardBackground)
            ) {
                TopPanel(
                    mainViewModel = mainViewModel,
                    viewerViewModel = viewerViewModel,
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )

                //Switch screen
                Box(modifier = Modifier.weight(1f)) {
                    when (mainState.currentScreen) {
                        Screen.StartScreen -> QuestionsScreen(
                            mainViewModel = mainViewModel,
                            viewerViewModel = viewerViewModel
                        )

                        Screen.TestScreen -> TesterScreen(
                            mainViewModel = mainViewModel,
                            testerViewModel = testerViewModel,
                            resulterViewModel = resultsViewModel
                        )

                        Screen.ResultsScreen -> ResultsScreen(
                            mainViewModel = mainViewModel,
                            resultViewModel = resultsViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopPanel(
    mainViewModel: MainViewModel,
    viewerViewModel: ViewerViewModel,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
){
    val mainState by mainViewModel.mainState.collectAsState()
    val appColors = LocalAppColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(appColors.secondaryBackground)
    ){
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = appColors.thirdBackground,
                    modifier = Modifier.size(60.dp)
                )
            }

            mainState.topBarItems.forEach { item ->
                when (item) {
                    TopBarItem.Search -> {
                        SearchBar(
                            mainViewModel = mainViewModel,
                            viewerViewModel = viewerViewModel
                        )
                    }
                }
            }
        }
    }
}

fun Modifier.bottomShadow(
    color: Color = Color.Black.copy(alpha = 0.1f),
    height: Dp = 4.dp
) = this.drawBehind {
    val shadowHeight = height.toPx()
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(color, Color.Transparent),
            startY = size.height,
            endY = size.height + shadowHeight
        ),
        topLeft = Offset(0f, size.height),
        size = Size(size.width, shadowHeight)
    )
}

fun Modifier.topShadow(
    color: Color = Color.Black.copy(alpha = 0.1f),
    height: Dp = 4.dp
) = this.drawBehind {
    val shadowHeight = height.toPx()
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, color),
            startY = -shadowHeight,
            endY = 0f
        ),
        topLeft = Offset(0f, -shadowHeight),
        size = Size(size.width, shadowHeight)
    )
}

@Composable
fun SidePanel(
    mainViewModel: MainViewModel,
    drawerState: DrawerState,
    content: @Composable () -> Unit
) {
    val mainState by mainViewModel.mainState.collectAsState()
    val appColors = LocalAppColors.current

    val fileName =
        if(mainState.fileName == null)
            "" 
        else
            mainState.fileName

    val questionCount =
        if(mainState.fileName == null)
            ""

        else
            mainState.questionsList.size.toString()


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(250.dp),
                drawerContainerColor = appColors.secondaryBackground
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(
                        modifier = Modifier
                            .height(16.dp)
                    )

                    Text(
                        text = "Меню",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.thirdText,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .bottomShadow(
                                Color.Black.copy(alpha = 0.4f),
                                2.dp
                            )
                    )

                    val scope = rememberCoroutineScope()

                    SidePanelButton(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Главная",
                        label = "Главная",
                        selected = false,
                        onClick =
                            {
                                mainViewModel.navigateTo(Screen.StartScreen)
                                scope.launch {
                                    drawerState.close()
                                }
                            },
                        modifier = Modifier.height(55.dp)
                    )

                    SidePanelButton(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = "Тестирование",
                        label = "Тестирование",
                        selected = false,
                        onClick =
                            {
                                mainViewModel.navigateTo(Screen.TestScreen)
                                scope.launch {
                                    drawerState.close()
                                }
                            },
                        modifier = Modifier.height(55.dp)
                    )

                    SidePanelButton(
                        imageVector = Icons.Default.StackedLineChart,
                        contentDescription = "Результаты",
                        label = "Результаты",
                        selected = false,
                        onClick =
                            {
                                mainViewModel.navigateTo(Screen.ResultsScreen)
                                scope.launch {
                                    drawerState.close()
                                }
                            },
                        modifier = Modifier.height(55.dp)
                    )

                    SidePanelButton(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Настройки",
                        label = "Настройки",
                        selected = false,
                        onClick = { /* действие */ },
                        modifier = Modifier.height(55.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .topShadow(
                                Color.Black.copy(alpha = 0.4f),
                                2.dp
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                                .padding(start = 10.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Имя файла: $fileName",
                                color = appColors.thirdText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                            )

                            Text(
                                text = "Количество вопросов: $questionCount",
                                color = appColors.thirdText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                            )
                        }
                    }
                }
            }
        },
        content = content
    )
}

@Composable
fun SidePanelButton(
    imageVector: ImageVector,
    contentDescription: String? = null,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current

    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription
            )
        },
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
        shape = RectangleShape,
        colors = NavigationDrawerItemDefaults.colors(
            selectedTextColor = appColors.thirdText,
            unselectedTextColor = appColors.thirdText,
            selectedIconColor = appColors.thirdText,
            unselectedIconColor = appColors.thirdText,
            selectedContainerColor = appColors.thirdBackground,
            unselectedContainerColor = Color.Transparent
        ),
        modifier = modifier.bottomShadow(
            Color.Black.copy(alpha = 0.2f)
        )
    )
}