package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.QuizResultEntity
import com.example.ui.viewmodel.EduViewModel
import com.example.ui.viewmodel.Screen
import java.util.Locale

// ----------------- Quiz Taker Screen -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTakerScreen(
    quiz: QuizEntity,
    viewModel: EduViewModel,
    modifier: Modifier = Modifier
) {
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitConfirmDialog = true
    }

    val questions by viewModel.activeQuizQuestions.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentQuestionIndex.collectAsStateWithLifecycle()
    val userAnswers by viewModel.userAnswers.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.timeRemainingSeconds.collectAsStateWithLifecycle()

    val total = questions.size
    val currentQuestion = questions.getOrNull(currentIndex)

    fun formatSeconds(s: Int): String {
        val m = s / 60
        val sec = s % 60
        return String.format(Locale.getDefault(), "%02d:%02d", m, sec)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(quiz.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(
                            text = if (total > 0) "السؤال ${currentIndex + 1} من إجمالي $total" else "كويز",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showExitConfirmDialog = true },
                        modifier = Modifier.testTag("quiz_cancel_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "خروج")
                    }
                },
                actions = {
                    // Live Countdown Timer Badge
                    val timerColor = if (remainingSeconds < 60) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
                    Surface(
                        color = timerColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = timerColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatSeconds(remainingSeconds),
                                color = timerColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentIndex > 0) viewModel.currentQuestionIndex.value = currentIndex - 1
                        },
                        enabled = currentIndex > 0,
                        modifier = Modifier.testTag("quiz_prev_question")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("السابق")
                    }

                    if (currentIndex == total - 1) {
                        Button(
                            onClick = { showSubmitConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            modifier = Modifier.testTag("quiz_finish_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تسليم الاختبار", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (currentIndex < total - 1) viewModel.currentQuestionIndex.value = currentIndex + 1
                            },
                            modifier = Modifier.testTag("quiz_next_question")
                        ) {
                            Text("التالي")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Linear Progress Indicator
            LinearProgressIndicator(
                progress = { if (total > 0) (currentIndex + 1).toFloat() / total else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (currentQuestion != null) {
                // Question Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "السؤال ${currentIndex + 1}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = currentQuestion.questionText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Options list
                val options = listOf(
                    Pair(0, currentQuestion.optionA),
                    Pair(1, currentQuestion.optionB),
                    Pair(2, currentQuestion.optionC),
                    Pair(3, currentQuestion.optionD)
                )

                val selectedAnswer = userAnswers[currentIndex]

                Text(
                    text = "اختر الإجابة الصحيحة:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                options.forEach { (optionIdx, optionText) ->
                    val isSelected = selectedAnswer == optionIdx
                    val optionLabel = when (optionIdx) {
                        0 -> "أ"
                        1 -> "ب"
                        2 -> "ج"
                        else -> "د"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { viewModel.selectQuizAnswer(currentIndex, optionIdx) }
                            .testTag("quiz_option_${currentIndex}_$optionIdx"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = optionLabel,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = optionText,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Exit confirmation dialog
    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("هل تريد مغادرة الاختبار؟", fontWeight = FontWeight.Bold) },
            text = { Text("في حال المغادرة الآن سيتم إلغاء تقدمك في هذا الكويز.") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        viewModel.navigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("نعم، مغادرة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) { Text("متابعة الاختبار") }
            }
        )
    }

    // Submit confirmation dialog
    if (showSubmitConfirmDialog) {
        val answeredCount = userAnswers.size
        AlertDialog(
            onDismissRequest = { showSubmitConfirmDialog = false },
            title = { Text("تسليم الاختبار", fontWeight = FontWeight.Bold) },
            text = {
                Text("لقد أجبت على $answeredCount من أصل $total أسئلة. هل أنت متأكد من تسليم الإجابات والحصول على النتيجة؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmDialog = false
                        viewModel.submitQuiz(quiz)
                    },
                    modifier = Modifier.testTag("confirm_submit_quiz")
                ) {
                    Text("تسليم الآن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirmDialog = false }) { Text("مراجعة الأسئلة") }
            }
        )
    }
}

// ----------------- Quiz Result Screen -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    result: QuizResultEntity,
    questions: List<QuizQuestionEntity>,
    userAnswers: Map<Int, Int>,
    viewModel: EduViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.StudentDashboard)
    }

    val isPassed = result.isPassed
    val statusColor = if (isPassed) Color(0xFF16A34A) else Color(0xFFDC2626)
    val statusBg = if (isPassed) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("نتيجة الاختبار والمراجعة", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.StudentDashboard) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الرئيسية")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Result Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(statusBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${result.percentage}%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                                Text(
                                    text = if (isPassed) "ناجح" else "راسب",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (isPassed) "مبارك! لقد اجتزت الكويز بنجاح 🎉" else "للأسف لم تجتز الكويز، يمكنك مراجعة المذكرة وإعادة المحاولة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "أحرزت ${result.score} إجابات صحيحة من إجمالي ${result.totalQuestions} أسئلة",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { viewModel.navigateTo(Screen.StudentDashboard) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("العودة إلى لوحة المواد")
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "مراجعة تفصيلية للأسئلة والإجابات والتفسير:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Question Review List
            itemsIndexed(questions) { index, q ->
                val userPick = userAnswers[index]
                val isCorrect = userPick != null && userPick == q.correctOptionIndex

                val options = listOf(q.optionA, q.optionB, q.optionC, q.optionD)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isCorrect) Color(0xFF16A34A).copy(alpha = 0.5f) else Color(0xFFDC2626).copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (isCorrect) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isCorrect) "إجابة صحيحة ✓" else "إجابة خاطئة ✗",
                                    color = if (isCorrect) Color(0xFF15803D) else Color(0xFFDC2626),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("السؤال ${index + 1}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(q.questionText, fontWeight = FontWeight.Bold, fontSize = 15.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Review Options
                        options.forEachIndexed { optIdx, optText ->
                            val isThisCorrect = optIdx == q.correctOptionIndex
                            val isThisSelectedByUser = userPick == optIdx

                            val bg = when {
                                isThisCorrect -> Color(0xFFDCFCE7)
                                isThisSelectedByUser && !isThisCorrect -> Color(0xFFFEE2E2)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }

                            val borderCol = when {
                                isThisCorrect -> Color(0xFF16A34A)
                                isThisSelectedByUser && !isThisCorrect -> Color(0xFFDC2626)
                                else -> Color.Transparent
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bg)
                                    .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val label = when (optIdx) {
                                    0 -> "أ"
                                    1 -> "ب"
                                    2 -> "ج"
                                    else -> "د"
                                }
                                Text(
                                    text = "$label) $optText",
                                    fontSize = 13.sp,
                                    fontWeight = if (isThisCorrect || isThisSelectedByUser) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )

                                if (isThisCorrect) {
                                    Text("الإجابة الصحيحة", fontSize = 11.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                                } else if (isThisSelectedByUser) {
                                    Text("إجابتك", fontSize = 11.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (q.explanation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "الشرح: ${q.explanation}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
