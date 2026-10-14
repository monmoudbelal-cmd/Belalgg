package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.utils.SampleDataGenerator
import com.example.ui.components.getCategoryIcon
import com.example.ui.components.parseColorSafe

// ----------------- Add Category Dialog -----------------
@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, desc: String, colorHex: String, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#2563EB") }
    var selectedIcon by remember { mutableStateOf("folder") }

    val presetColors = listOf("#2563EB", "#059669", "#7C3AED", "#D97706", "#DC2626", "#0D9488")
    val presetIcons = listOf("folder", "calculate", "science", "menu_book", "movie", "quiz")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة قسم / مجلد جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم القسم (مثال: الفيزياء، الرياضيات)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_cat_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("وصف مختصر للقسم ومحتواه") },
                    modifier = Modifier.fillMaxWidth().testTag("add_cat_desc_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("اختر لون المجلد:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetColors.forEach { colorHex ->
                        val col = parseColorSafe(colorHex)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(col)
                                .clickable { selectedColor = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == colorHex) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("اختر أيقونة المجلد:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetIcons.forEach { iconName ->
                        val isSelected = selectedIcon == iconName
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedIcon = iconName },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(iconName),
                                contentDescription = iconName,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, desc, selectedColor, selectedIcon)
                    }
                },
                modifier = Modifier.testTag("submit_add_cat_button")
            ) {
                Text("إضافة القسم")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

// ----------------- Add File Dialog -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFileDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (catId: Long, title: String, desc: String, type: String, uri: String, size: String, extra: String) -> Unit
) {
    val context = LocalContext.current
    var selectedCatId by remember { mutableStateOf(categories.firstOrNull()?.id ?: 0L) }
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var fileType by remember { mutableStateOf("PDF") } // "PDF", "VIDEO", "IMAGE"
    var uriString by remember { mutableStateOf("") }
    var fileSize by remember { mutableStateOf("") }
    var extraNotes by remember { mutableStateOf("") }

    var expandedCatMenu by remember { mutableStateOf(false) }

    // Media Picker for Images and Videos
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            uriString = uri.toString()
            if (title.isBlank()) {
                title = if (fileType == "VIDEO") "فيديو تعليمي جديد" else "صورة توضيحية جديدة"
            }
            fileSize = "محدد من الجهاز"
        }
    }

    // Generic Document Picker for PDFs
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uriString = uri.toString()
            if (title.isBlank()) title = "مستند PDF جديد"
            fileSize = "ملف PDF محلي"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة ملف تعليمي (PDF / فيديو / صورة)", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Category Picker
                Text("اختر القسم / المجلد:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedCatMenu,
                    onExpandedChange = { expandedCatMenu = !expandedCatMenu }
                ) {
                    val currentCat = categories.find { it.id == selectedCatId }
                    OutlinedTextField(
                        value = currentCat?.name ?: "اختر القسم",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCatMenu) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCatMenu,
                        onDismissRequest = { expandedCatMenu = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCatId = cat.id
                                    expandedCatMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // File Type Selector
                Text("نوع الملف:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = fileType == "PDF",
                        onClick = { fileType = "PDF" },
                        label = { Text("PDF مستند") },
                        leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = fileType == "VIDEO",
                        onClick = { fileType = "VIDEO" },
                        label = { Text("فيديو") },
                        leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = fileType == "IMAGE",
                        onClick = { fileType = "IMAGE" },
                        label = { Text("صورة") },
                        leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الملف") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_file_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("وصف مختصر للمحتوى") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // File Picker Buttons & URI
                Text("مصدر الملف:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (fileType == "PDF") {
                                documentPickerLauncher.launch("application/pdf")
                            } else if (fileType == "VIDEO") {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            } else {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اختيار من الجهاز")
                    }

                    if (fileType == "PDF") {
                        Button(
                            onClick = {
                                val generatedPath = SampleDataGenerator.createSamplePdf(
                                    context,
                                    "lesson_${System.currentTimeMillis()}.pdf",
                                    title.ifBlank { "مذكرة دراسية" }
                                )
                                uriString = generatedPath
                                fileSize = "1.6 MB"
                                if (title.isBlank()) title = "مذكرة تفاعلية جديدة (PDF)"
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("توليد PDF حقيقي")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uriString,
                    onValueChange = { uriString = it },
                    label = { Text("مسار أو رابط الملف (URI / Path)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_file_uri_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = extraNotes,
                    onValueChange = { extraNotes = it },
                    label = { Text("ملاحظات إضافية / نص توضيحي للدرس") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && uriString.isNotBlank() && selectedCatId != 0L) {
                        onConfirm(selectedCatId, title, desc, fileType, uriString, fileSize, extraNotes)
                    }
                },
                modifier = Modifier.testTag("submit_add_file_button")
            ) {
                Text("حفظ الملف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

// ----------------- Add User Dialog ("أقدر اعمل كتير من ده") -----------------
@Composable
fun AddUserDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (username: String, pass: String, name: String, allowedCatIds: String, notes: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var allCategoriesAllowed by remember { mutableStateOf(true) }
    val selectedCatIds = remember { mutableStateListOf<Long>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إنشاء حساب طالب جديد", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "أنشئ اسم مستخدم وكلمة سر خاصة بالطالب ليتمكن من الدخول للملفات والكويزات.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("اسم الطالب (مثال: محمد خالد)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_user_display_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("اسم المستخدم للدخول (مثال: mohamed)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_user_username_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة السر الممنوحة له (مثال: 123456)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_user_password_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات (المجموعة، الصف، رقم الهاتف)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("صلاحية الوصول للأقسام:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { allCategoriesAllowed = !allCategoriesAllowed }
                ) {
                    Checkbox(
                        checked = allCategoriesAllowed,
                        onCheckedChange = { allCategoriesAllowed = it }
                    )
                    Text("السماح بالوصول لكافة المواد والأقسام", fontSize = 13.sp)
                }

                if (!allCategoriesAllowed) {
                    Text("حدد الأقسام المسموح بها فقط:", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    categories.forEach { cat ->
                        val isChecked = selectedCatIds.contains(cat.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (isChecked) selectedCatIds.remove(cat.id) else selectedCatIds.add(cat.id)
                            }
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    if (it) selectedCatIds.add(cat.id) else selectedCatIds.remove(cat.id)
                                }
                            )
                            Text(cat.name, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (username.isNotBlank() && password.isNotBlank() && displayName.isNotBlank()) {
                        val allowed = if (allCategoriesAllowed) "*" else selectedCatIds.joinToString(",")
                        onConfirm(username, password, displayName, allowed, notes)
                    }
                },
                modifier = Modifier.testTag("submit_add_user_button")
            ) {
                Text("حفظ الحساب")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

// ----------------- Add Quiz Dialog -----------------
data class DraftQuestion(
    var text: String = "",
    var opA: String = "",
    var opB: String = "",
    var opC: String = "",
    var opD: String = "",
    var correctIndex: Int = 0,
    var explanation: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuizDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (quiz: QuizEntity, questions: List<QuizQuestionEntity>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedCatId by remember { mutableStateOf(categories.firstOrNull()?.id ?: 0L) }
    var durationMinutes by remember { mutableStateOf("15") }
    var passScorePercent by remember { mutableStateOf("60") }
    var expandedCatMenu by remember { mutableStateOf(false) }

    val draftQuestions = remember {
        mutableStateListOf(
            DraftQuestion(
                text = "ما هي نتيجة المعادلة 5 × 4 ؟",
                opA = "20",
                opB = "25",
                opC = "15",
                opD = "10",
                correctIndex = 0,
                explanation = "5 مضروبة في 4 تساوي 20."
            )
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء كويز تفاعلي جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Category Picker
                Text("القسم / المادة:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedCatMenu,
                    onExpandedChange = { expandedCatMenu = !expandedCatMenu }
                ) {
                    val currentCat = categories.find { it.id == selectedCatId }
                    OutlinedTextField(
                        value = currentCat?.name ?: "اختر القسم",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCatMenu) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCatMenu,
                        onDismissRequest = { expandedCatMenu = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCatId = cat.id
                                    expandedCatMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الكويز") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_quiz_title_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("وصف الاختبار") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = durationMinutes,
                        onValueChange = { durationMinutes = it },
                        label = { Text("المدة بالدقائق") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = passScorePercent,
                        onValueChange = { passScorePercent = it },
                        label = { Text("نسبة النجاح %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("الأسئلة (${draftQuestions.size}):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    TextButton(onClick = {
                        draftQuestions.add(
                            DraftQuestion(
                                text = "",
                                opA = "",
                                opB = "",
                                opC = "",
                                opD = "",
                                correctIndex = 0,
                                explanation = ""
                            )
                        )
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة سؤال")
                    }
                }

                draftQuestions.forEachIndexed { qIndex, qDraft ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("السؤال ${qIndex + 1}", fontWeight = FontWeight.Bold)
                                if (draftQuestions.size > 1) {
                                    IconButton(
                                        onClick = { draftQuestions.removeAt(qIndex) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = qDraft.text,
                                onValueChange = { qDraft.text = it },
                                label = { Text("نص السؤال") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("الخيارات الأربعة (حدد الإجابة الصحيحة):", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)

                            // Option A
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = qDraft.correctIndex == 0, onClick = { qDraft.correctIndex = 0 })
                                OutlinedTextField(
                                    value = qDraft.opA,
                                    onValueChange = { qDraft.opA = it },
                                    label = { Text("الخيار أ") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Option B
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = qDraft.correctIndex == 1, onClick = { qDraft.correctIndex = 1 })
                                OutlinedTextField(
                                    value = qDraft.opB,
                                    onValueChange = { qDraft.opB = it },
                                    label = { Text("الخيار ب") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Option C
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = qDraft.correctIndex == 2, onClick = { qDraft.correctIndex = 2 })
                                OutlinedTextField(
                                    value = qDraft.opC,
                                    onValueChange = { qDraft.opC = it },
                                    label = { Text("الخيار ج") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Option D
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = qDraft.correctIndex == 3, onClick = { qDraft.correctIndex = 3 })
                                OutlinedTextField(
                                    value = qDraft.opD,
                                    onValueChange = { qDraft.opD = it },
                                    label = { Text("الخيار د") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = qDraft.explanation,
                                onValueChange = { qDraft.explanation = it },
                                label = { Text("تفسير وشرح الإجابة الصحيحة للطالب") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedCatId != 0L && draftQuestions.isNotEmpty()) {
                        val quiz = QuizEntity(
                            categoryId = selectedCatId,
                            title = title.trim(),
                            description = desc.trim(),
                            durationMinutes = durationMinutes.toIntOrNull() ?: 15,
                            passScorePercent = passScorePercent.toIntOrNull() ?: 60
                        )
                        val questionEntities = draftQuestions.mapIndexed { idx, dq ->
                            QuizQuestionEntity(
                                quizId = 0,
                                questionText = dq.text.ifBlank { "سؤال بدون عنوان" },
                                optionA = dq.opA.ifBlank { "الخيار 1" },
                                optionB = dq.opB.ifBlank { "الخيار 2" },
                                optionC = dq.opC.ifBlank { "الخيار 3" },
                                optionD = dq.opD.ifBlank { "الخيار 4" },
                                correctOptionIndex = dq.correctIndex,
                                explanation = dq.explanation,
                                orderIndex = idx
                            )
                        }
                        onConfirm(quiz, questionEntities)
                    }
                },
                modifier = Modifier.testTag("submit_add_quiz_button")
            ) {
                Text("حفظ الكويز")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

// ----------------- Change Admin PIN Dialog -----------------
@Composable
fun ChangeAdminPinDialog(
    currentPin: String,
    onDismiss: () -> Unit,
    onConfirm: (newPin: String) -> Unit
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تغيير كلمة سر المسؤول (PIN)", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "كلمة السر الحالية مطلوبة لتأكيد التغيير.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = oldPin,
                    onValueChange = {
                        oldPin = it
                        error = null
                    },
                    label = { Text("كلمة السر الحالية") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        newPin = it
                        error = null
                    },
                    label = { Text("كلمة السر الجديدة") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (oldPin.trim() != currentPin.trim()) {
                    error = "كلمة السر الحالية غير صحيحة"
                } else if (newPin.isBlank()) {
                    error = "يرجى إدخال كلمة سر جديدة"
                } else {
                    onConfirm(newPin)
                }
            }) {
                Text("تغيير")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
