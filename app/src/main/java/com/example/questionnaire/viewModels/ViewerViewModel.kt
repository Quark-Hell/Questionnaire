package com.example.questionnaire.viewModels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.questionnaire.docxParser.DocxParser
import com.example.questionnaire.models.ViewerModel
import com.example.questionnaire.viewers.getFileName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ViewerViewModel : ViewModel() {
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
            mainViewModel.saveQuestionsToDatabase(mainViewModel.mainState.value.questionsList);
            return null
        }
        return "Выберите файл .docx" // show error
    }
}