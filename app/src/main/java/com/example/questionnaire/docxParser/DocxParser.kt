package com.example.questionnaire.docxParser

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.apache.poi.xwpf.usermodel.XWPFDocument

@Parcelize
data class QuestionItem(
    val question: String,
    val answers: List<String> = emptyList(),
    val correctAnswerIndex: Int // -1 если правильный ответ не найден
) : Parcelable

fun String.normalizeDocx(): String =
    this
        .replace("\u00A0", " ")   // NBSP
        .replace("\u200B", "")    // zero-width space
        .replace("\uFEFF", "")    // BOM
        .trim()


class DocxParser {

    companion object {

        /**
         * Парсит .docx с фиксированной структурой:
         * - пустые строки
         * - строка с вопросом
         * - 3-4 строки с вариантами (правильный вариант заканчивается "(+)" или "+" )
         * - пустая строка = конец вопроса
         */
        fun parse(uri: Uri, context: Context): List<QuestionItem> {
            val questions = mutableListOf<QuestionItem>()

            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val doc = XWPFDocument(stream)
                    val paragraphs = doc.paragraphs.map { it.text.normalizeDocx() }

                    var i = 0
                    while (i < paragraphs.size) {
                        // Пропускаем пустые строки
                        if (paragraphs[i].isEmpty()) {
                            i++
                            continue
                        }

                        // Берём первую непустую строку как вопрос
                        val questionText = paragraphs[i]
                        i++

                        val answers = mutableListOf<String>()
                        var correctIndex = -1

                        // Берём следующие строки до пустой как ответы
                        while (i < paragraphs.size && paragraphs[i].isNotEmpty()) {
                            var answerText = paragraphs[i]
                            var isCorrect = false

                            // Проверяем на "(+)" или "+"
                            if (answerText.endsWith("(+)")) {
                                isCorrect = true
                                answerText = answerText.removeSuffix("(+)").trim()
                            } else if (answerText.endsWith("+")) {
                                isCorrect = true
                                answerText = answerText.removeSuffix("+").trim()
                            }
                            else if (answerText.startsWith("+")){
                                isCorrect = true
                                answerText = answerText.removePrefix("+").trim()
                            }

                            if (isCorrect) correctIndex = answers.size

                            answers.add(answerText)
                            i++
                        }

                        // Сохраняем вопрос даже если правильный ответ не найден
                        if (answers.isNotEmpty()) {
                            questions.add(
                                QuestionItem(
                                    question = questionText,
                                    answers = answers.toList(),
                                    correctAnswerIndex = correctIndex // -1 если не найдено
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return questions
        }
    }
}
