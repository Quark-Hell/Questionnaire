package com.example.questionnaire.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material3.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp

import com.example.questionnaire.docxParser.QuestionItem
import com.example.questionnaire.tester.TesterViewModel
import com.example.questionnaire.ui.theme.QuestionnaireTheme
import com.example.questionnaire.resulter.ResultsScreen
import com.example.questionnaire.resulter.ResultsViewModel
import com.example.questionnaire.tester.TestSettingsScreen
import com.example.questionnaire.viewer.DocumentPickerScreen
import com.example.questionnaire.viewer.ViewerViewModel

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

    val standardText: Color,
    val secondaryText: Color,
    val thirdText: Color,

    val fill: Color,
    val highlights: Color,
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

        standardText = Color(0xFF000000),
        secondaryText = Color(0xFFF5F0EA),
        thirdText = Color(0xFFD4C2B6),

        fill = Color(0xFFFFFFFF),
        highlights = Color(0xFFE9D6B1)
    )

    CompositionLocalProvider(LocalAppColors provides appColors) {
        content()
    }
}

data class ThemeColors (
    val whiteTheme: AppColorScheme,
    val blackTheme: AppColorScheme,
    val isWhiteTheme: Boolean = true
)

sealed class Screen {
    object StartScreen : Screen()
    object TestScreen : Screen()
    object ResultsScreen : Screen()
}

data class MainModel (
    var currentScreen: Screen = Screen.StartScreen,
    val questionsList: List<QuestionItem> = emptyList(),
    val questionCount: Int = 0,
    val fileName: String? = null,
)

open class MainViewModel : ViewModel() {
    private val _mainState = MutableStateFlow(MainModel())
    val mainState: StateFlow<MainModel> = _mainState

    fun navigateTo(screen: Screen) {
        _mainState.value = _mainState.value.copy(currentScreen = screen)
    }

    fun setQuestions(questions: List<QuestionItem>, fileName: String?) {
        _mainState.update { current ->
            current.copy(
                questionsList = questions,
                questionCount = questions.size,
                fileName = fileName
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private  val viewerViewModel: ViewerViewModel by viewModels()
    private val testerViewModel: TesterViewModel by viewModels()
    private val resultsViewModel: ResultsViewModel by viewModels()

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

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val fakeMainViewModel = object : MainViewModel() {
    }
    val fakeViewerViewModel = object : ViewerViewModel() {
    }
    val fakeTesterViewModel = object : TesterViewModel() {
    }

    val fakeResultsViewModel = object : ResultsViewModel() {
    }


    MainScreen(
        mainViewModel = fakeMainViewModel,
        fakeViewerViewModel,
        fakeTesterViewModel,
        fakeResultsViewModel
    )
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

                Box(modifier = Modifier.weight(1f)) {
                    when (mainState.currentScreen) {
                        Screen.StartScreen -> DocumentPickerScreen(
                            mainViewModel = mainViewModel,
                            viewerViewModel = viewerViewModel
                        )

                        Screen.TestScreen -> TestSettingsScreen(
                            mainViewModel = mainViewModel,
                            testerViewModel = testerViewModel
                        )

                        Screen.ResultsScreen -> ResultsScreen(resultModel = resultsViewModel)
                    }
                }
            }
        }
    }
}

private fun jumpToQuestion(
    value: String,
    questionsCount: Int,
    viewerViewModel: ViewerViewModel
) {
    val index = value.toIntOrNull()?.minus(1)

    if (index != null && index in 0 until questionsCount) {
        viewerViewModel.selectQuestion(index)
    } else {
        viewerViewModel.selectQuestion(null)
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
    val viewerState by viewerViewModel.viewerState.collectAsState()

    val appColors = LocalAppColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
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
                    tint = appColors.fill,
                    modifier = Modifier.size(60.dp)

                )
            }

            var searchValue by remember { mutableStateOf("") }
            val keyboardController = LocalSoftwareKeyboardController.current
            val focusManager = LocalFocusManager.current

            OutlinedTextField(
                value = searchValue,
                onValueChange = { searchValue = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp)
                    .height(56.dp),
                trailingIcon = {
                    IconButton(onClick = {
                        jumpToQuestion(searchValue, mainState.questionsList.size, viewerViewModel)
                        focusManager.clearFocus()
                        searchValue = ""
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search icon")
                    }
                },
                placeholder = { Text("Search", fontSize = 14.sp) },
                singleLine = true,
                shape = RoundedCornerShape(100.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        jumpToQuestion(
                            searchValue,
                            mainState.questionsList.size,
                            viewerViewModel
                        )
                        searchValue = ""
                        focusManager.clearFocus()

                        keyboardController?.hide()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = appColors.fill,
                    unfocusedContainerColor = appColors.fill,

                    unfocusedBorderColor = appColors.fill,

                    focusedTextColor = appColors.standardText,
                    unfocusedTextColor = appColors.highlights,

                    focusedTrailingIconColor = appColors.standardText,
                    unfocusedTrailingIconColor = appColors.highlights,
                )
            )
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
            mainState.questionCount.toString()


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
                        onClick = { /* действие */ },
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