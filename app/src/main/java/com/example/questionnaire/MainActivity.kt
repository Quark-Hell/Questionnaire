package com.example.questionnaire


//
//@Composable
//fun MainScreen(
//    viewModel: MainViewModel,
//    modifier: Modifier = Modifier
//) {
//    val questionsList = remember { mutableStateListOf<QuestionItem>() }
//    var selectedFileName by remember { mutableStateOf<String?>(null) }
//    var testResults by remember { mutableStateOf<List<TestResult>>(emptyList()) }
//
//    // Состояние тестирования
//    var isTestStarted by remember { mutableStateOf(false) }
//    var currentQuestionIndex by remember { mutableStateOf(0) }
//    var displayedQuestions by remember { mutableStateOf(listOf<QuestionItem>()) }
//    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
//    var currentTestResults by remember { mutableStateOf(mutableListOf<TestResult>()) }
//
//    val state by viewModel.mainState.collectAsState()
//
//    Column(modifier = modifier.fillMaxSize()) {
//        Box(modifier = Modifier.weight(1f)) {
//            when (state.currentScreen) {
//                Screen.StartScreen -> DocumentPickerScreen(
//                    questionsList = questionsList,
//                    selectedFileName = selectedFileName,
//                    onFileSelected = { name -> selectedFileName = name }
//                )
//                Screen.TestScreen -> TestingScreen(
//                    questionsList = questionsList,
//                    isTestStarted = isTestStarted,
//                    currentQuestionIndex = currentQuestionIndex,
//                    displayedQuestions = displayedQuestions,
//                    selectedAnswerIndex = selectedAnswerIndex,
//                    currentTestResults = currentTestResults,
//                    onTestStateChanged = { started, index, questions, answer, results ->
//                        isTestStarted = started
//                        currentQuestionIndex = index
//                        displayedQuestions = questions
//                        selectedAnswerIndex = answer
//                        currentTestResults = results
//                    },
//                    onTestComplete = { results ->
//                        testResults = results
//                        isTestStarted = false
//                        state.currentScreen = Screen.ResultsScreen
//                    }
//                )
//                Screen.ResultsScreen -> ResultsScreen(viewModel = viewModel, testResults = testResults)
//            }
//        }
//
//        NavigationBar(
//            viewModel = viewModel,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//    }
//}
//