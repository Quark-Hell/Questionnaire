package com.example.questionnaire.viewer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import com.example.questionnaire.docxParser.DocxParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.questionnaire.components.QuestionCard


import com.example.questionnaire.main.LocalAppColors
import com.example.questionnaire.main.MainViewModel
import com.example.questionnaire.main.TopBarItem
import com.example.questionnaire.models.ViewerModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update

open class ViewerViewModel : ViewModel() {
    private val _viewerState = MutableStateFlow(ViewerModel())
    val viewerState: StateFlow<ViewerModel> = _viewerState

    fun selectQuestion(index: Int?) {
        _viewerState.update { current ->
            current.copy(selectedQuestion = index)
        }
    }

    fun handleDocument(
        uri: Uri,
        context: Context,
        mainViewModel: MainViewModel
    ): String? {
        val name = getFileName(context, uri)
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document") {
            val parsed = DocxParser.parse(uri, context)
            mainViewModel.setQuestions(parsed, name)
            return null
        }
        return "Выберите файл .docx" // show error
    }
}

fun getFileName(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst()) return it.getString(nameIndex)
    }
    return uri.lastPathSegment
}

@Composable
fun QuestionsScreen(
    mainViewModel: MainViewModel,
    viewerViewModel: ViewerViewModel
) {
    mainViewModel.setTopBar(listOf(TopBarItem.Search))

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val errorMessage = viewerViewModel.handleDocument(it, context, mainViewModel)
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.95f)
        ) {
            QuestionNavigator(
                mainViewModel,
                viewerViewModel
            )

        }
    }

    FilePickerButton(launcher)
}

@Composable
fun FilePickerButton(launcher: ManagedActivityResultLauncher<Array<String>, Uri?>) {
    val appColors = LocalAppColors.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        FloatingActionButton(
            onClick = { launcher.launch(arrayOf("application/*")) },
            shape = RoundedCornerShape(20.dp),
            containerColor = appColors.secondaryBackground,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(26.dp)
                .size(60.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Add",
                tint = appColors.thirdBackground,
                modifier = Modifier
                    .fillMaxSize(0.7f)
            )
        }
    }
}

@Composable
fun QuestionNavigator(
    mainViewModel: MainViewModel,
    viewerViewModel: ViewerViewModel
) {
    val mainState by mainViewModel.mainState.collectAsState()
    val viewerViewModel by viewerViewModel.viewerState.collectAsState()

    if (mainState.questionsList.isEmpty()) return

    val listState = rememberLazyListState()

    LaunchedEffect(viewerViewModel.selectedQuestion) {
        viewerViewModel.selectedQuestion?.let { index ->
            listState.animateScrollToItem(index)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        itemsIndexed(mainState.questionsList) { index, item ->
            val selectedModifier = if (index == viewerViewModel.selectedQuestion) {
                Modifier.dropShadow(
                    shape = RoundedCornerShape(4.dp),
                    shadow = Shadow(
                        radius = 4.dp,
                        spread = 0.dp,
                        color = Color(0x60000000),
                        offset = DpOffset(x = 4.dp, y = 4.dp)
                    )
                )
            } else {
                Modifier
            }

            QuestionCard(
                questionIndex = index,
                questionItem = item,
                selectedModifier
            )
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
fun SearchBar(
    mainViewModel: MainViewModel,
    viewerViewModel: ViewerViewModel
) {
    val mainState by mainViewModel.mainState.collectAsState()

    val appColors = LocalAppColors.current

    var searchValue by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var isSearchPressed by remember { mutableStateOf(false) }
    val searchIconScale by animateFloatAsState(
        targetValue = if (isSearchPressed) 0.85f else 1f,
        animationSpec = tween(250),
        label = "SearchIconScale"
    )

    var isSearchFocused by remember { mutableStateOf(false) }
    val searchState = rememberTextFieldState(searchValue)

    LaunchedEffect(isSearchPressed) {
        if (isSearchPressed) {
            delay(250)
            isSearchPressed = false
        }
    }

    OutlinedTextField(
        state = searchState,
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 10.dp),
        shape = RoundedCornerShape(100.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = appColors.thirdBackground,
            unfocusedContainerColor = appColors.thirdBackground,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = appColors.black,
            unfocusedTextColor = appColors.gray,
            focusedTrailingIconColor = appColors.black,
            unfocusedTrailingIconColor = appColors.gray,
        ),
        trailingIcon = {
            IconButton(
                onClick = {
                    isSearchPressed = true
                    jumpToQuestion(
                        searchState.text.toString(),
                        mainState.questionsList.size,
                        viewerViewModel
                    )
                    searchState.edit {
                        replace(0, length, "")
                    }
                    focusManager.clearFocus()
                },
                modifier = Modifier.graphicsLayer {
                    scaleX = searchIconScale
                    scaleY = searchIconScale
                }
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search icon")
            }
        },
        textStyle = TextStyle(
            fontSize = 18.sp,
            lineHeight = 14.sp,
            color = appColors.black
        ),
        placeholder = {
            if (!isSearchFocused) {
                Text(
                    "Search",
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    color = appColors.thirdText
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        onKeyboardAction = {
            jumpToQuestion(
                searchState.text.toString(),
                mainState.questionsList.size,
                viewerViewModel
            )
            searchState.edit {
                replace(0, length, "")
            }
            focusManager.clearFocus()
            keyboardController?.hide()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp)
            .height(45.dp)
            .onFocusChanged { focusState ->
                isSearchFocused = focusState.isFocused
            },
    )
}