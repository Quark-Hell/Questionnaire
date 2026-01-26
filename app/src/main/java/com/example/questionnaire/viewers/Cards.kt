package com.example.questionnaire.viewers

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.questionnaire.components.FilePickerButton
import com.example.questionnaire.viewModels.CardsViewModel
import com.example.questionnaire.viewModels.MainViewModel

@Composable
fun Cards(
    mainViewModel: MainViewModel,
    cardsViewModel: CardsViewModel
) {
    mainViewModel.setTopBar(listOf(TopBarItem.Search))

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val errorMessage = cardsViewModel.handleDocument(it, context, mainViewModel)
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    FilePickerButton(launcher)
}