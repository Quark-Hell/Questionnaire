package com.example.questionnaire.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.questionnaire.viewers.LocalAppColors
import com.example.questionnaire.models.QuestionModel

@Composable
fun QuestionCard(
    questionIndex: Int,
    questionItem: QuestionModel,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                color = appColors.thirdBackground,
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .dropShadow(
                    shape = RoundedCornerShape(10.dp),
                    shadow = Shadow(
                        radius = 8.dp,
                        spread = 0.dp,
                        color = Color(0x30000000),
                        offset = DpOffset(x = 0.dp, 2.dp)
                    )
                )
                .background(appColors.secondaryBackground)
                .padding(horizontal = 16.dp)
                .padding(vertical = 10.dp)
        ) {
            Text(
                text = "${questionIndex + 1}. ${questionItem.question}",
                style = MaterialTheme.typography.titleMedium,
                color = appColors.whiteText
            )
        }
        Column(
            modifier = Modifier
                .padding(10.dp)
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
                        color = appColors.black
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}