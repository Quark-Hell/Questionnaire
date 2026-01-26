package com.example.questionnaire.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.questionnaire.viewers.LocalAppColors

@Composable
fun ClassicButton (
    modifier: Modifier = Modifier,
    label: String = "",
    cornerRadius: Dp = 0.dp,
    onClickAction: () -> Unit
) {

    val appColors = LocalAppColors.current

    Button(
        onClick = onClickAction,
        colors = ButtonDefaults.buttonColors(
            containerColor = appColors.secondaryBackground,
            contentColor = appColors.whiteText,
            disabledContainerColor = appColors.focused,
            disabledContentColor = appColors.whiteText
        ),
        shape = RoundedCornerShape(cornerRadius),
        modifier = Modifier
            .then(modifier)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            color = appColors.whiteText
        )
    }
}