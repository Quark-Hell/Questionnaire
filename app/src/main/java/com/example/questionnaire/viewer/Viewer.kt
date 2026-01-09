package com.example.questionnaire.viewer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.questionnaire.docxParser.QuestionItem
import com.example.questionnaire.main.LocalAppColors
import com.example.questionnaire.main.MainViewModel
import com.example.questionnaire.main.bottomShadow
import kotlinx.coroutines.flow.update

data class ViewerModel (
    val questionCount: Int = 0,
    val selectedQuestion: Int? = null
)

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

@Composable
fun DocumentPickerScreen(
    mainViewModel: MainViewModel,
    viewerViewModel: ViewerViewModel
) {
    val mainState = mainViewModel.mainState.collectAsState().value
    val viewerState = viewerViewModel.viewerState.collectAsState().value
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
            .padding(top = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.95f)
        ) {
            QuestionNavigator(
                mainViewModel,
                viewerViewModel,
                modifier = Modifier.fillMaxWidth()
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
                tint = appColors.fill,
                modifier = Modifier
                    .fillMaxSize(0.7f)
            )
        }
    }
}

@Composable
fun QuestionNavigator(
    mainViewModel: MainViewModel,
    viewerViewModel: ViewerViewModel,
    modifier: Modifier
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
            QuestionItemView(
                questionIndex = index,
                questionItem = item,
                isCurrent = index == viewerViewModel.selectedQuestion
            )
        }
    }
}

@Composable
fun QuestionItemView(
    questionIndex: Int,
    questionItem: QuestionItem,
    isCurrent: Boolean
) {
    val appColors = LocalAppColors.current

    Column(
        modifier = Modifier
            .then(
                if (isCurrent) Modifier.dropShadow(
                    shape = RoundedCornerShape(20.dp),
                    shadow = Shadow(
                        radius = 10.dp,
                        spread = 2.dp,
                        color = Color(0x40000000),
                        offset = DpOffset(x = 2.dp, 2.dp)
                    )
                )
                else Modifier
            )
            .clip(RoundedCornerShape(4.dp))
            .background(
                color = appColors.thirdBackground,
            )

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(appColors.secondaryBackground)
                .dropShadow(
                    shape = RoundedCornerShape(10.dp),
                    shadow = Shadow(
                        radius = 8.dp,
                        spread = 0.dp,
                        color = Color(0x30000000),
                        offset = DpOffset(x = 0.dp, 2.dp)
                    )
                )
                .padding(horizontal = 16.dp)
                .padding(vertical = 10.dp)
        ) {
            Text(
                text = "${questionIndex + 1}. ${questionItem.question}",
                style = MaterialTheme.typography.titleMedium,
                color = appColors.secondaryText
            )
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .padding(vertical = 10.dp)
        ){
            Spacer(modifier = Modifier.height(4.dp))

            questionItem.answers.forEachIndexed { index, answer ->
                val isCorrect = index == questionItem.correctAnswerIndex

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isCorrect) appColors.highlights else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = "${index + 1}. $answer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.standardText
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
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