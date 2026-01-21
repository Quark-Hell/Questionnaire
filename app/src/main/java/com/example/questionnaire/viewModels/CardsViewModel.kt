package com.example.questionnaire.viewModels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.questionnaire.docxParser.DocxParser
import com.example.questionnaire.viewers.getFileName

class CardsViewModel : ViewModel() {
    fun handleDocument(
        uri: Uri,
        context: Context,
        mainViewModel: MainViewModel
    ): String? {
        val name = getFileName(context, uri)
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document") {
            return null
        }
        return "Выберите файл .docx" // show error
    }
}