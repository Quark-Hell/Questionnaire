package com.example.questionnaire.components

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.questionnaire.viewers.LocalAppColors

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
fun IconButton(
    onClickAction: () -> Unit
){
    val appColors = LocalAppColors.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        FloatingActionButton(
            onClick = onClickAction,
            shape = RoundedCornerShape(20.dp),
            containerColor = appColors.secondaryBackground,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(26.dp)
                .size(60.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = appColors.thirdBackground,
                modifier = Modifier
                    .fillMaxSize(0.7f)
            )
        }
    }
}