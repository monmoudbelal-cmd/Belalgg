package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CategoryEntity
import com.example.data.model.FileItemEntity
import com.example.data.model.UserAccountEntity
import com.example.ui.components.CategorySelectorBar
import com.example.ui.components.FileCardItem
import com.example.ui.components.QuizCardItem
import com.example.ui.components.QuizResultCardItem
import com.example.ui.components.getCategoryIcon
import com.example.ui.components.parseColorSafe
import com.example.ui.viewmodel.EduViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: EduViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler {
        viewModel.logout()
    }

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val files by viewModel.files.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val quizzes by viewModel.quizzes.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val adminPin by viewModel.adminPin.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Dialog flags
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddFileDialog by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showAddQuizDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("لوحة تحكم المعلم", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("إدارة المحتوى وحسابات الطلاب", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.StudentDashboard) },
                        modifier = Modifier.testTag("admin_preview_student_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "معاينة كطالب",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { showChangePinDialog = true },
                        modifier = Modifier.testTag("admin_change_pin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "تغيير PIN",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("admin_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "تسجيل الخروج",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            when (selectedTabIndex) {
                0 -> {
                    FloatingActionButton(
                        onClick = { showAddCategoryDialog = true },
                        modifier = Modifier.testTag("fab_add_category")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة قسم")
                    }
                }
                1 -> {
                    FloatingActionButton(
                        onClick = { showAddFileDialog = true },
                        modifier = Modifier.testTag("fab_add_file")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة ملف")
                    }
                }
                2 -> {
                    FloatingActionButton(
                        onClick = { showAddUserDialog = true },
                        modifier = Modifier.testTag("fab_add_user")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إنشاء حساب طالب")
                    }
                }
                3 -> {
                    FloatingActionButton(
                        onClick = { showAddQuizDialog = true },
                        modifier = Modifier.testTag("fab_add_quiz")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إنشاء كويز")
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
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("الأقسام (${categories.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_categories")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("الملفات (${files.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_files")
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("حسابات الطلاب (${users.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_users")
                )
                Tab(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    text = { Text("الكويزات (${quizzes.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_quizzes")
                )
                Tab(
                    selected = selectedTabIndex == 4,
                    onClick = { selectedTabIndex = 4 },
                    text = { Text("نتائج الاختبارات (${results.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_tab_results")
                )
            }

            when (selectedTabIndex) {
                0 -> {
                    // Categories Tab
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(categories, key = { it.id }) { cat ->
                            val catColor = parseColorSafe(cat.colorHex)
                            val catFilesCount = files.count { it.categoryId == cat.id }
                            val catQuizzesCount = quizzes.count { it.categoryId == cat.id }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth().testTag("admin_category_card_${cat.id}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(catColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getCategoryIcon(cat.iconName),
                                            contentDescription = cat.name,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cat.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (cat.description.isNotBlank()) {
                                            Text(
                                                text = cat.description,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$catFilesCount ملفات • $catQuizzesCount كويزات",
                                            fontSize = 11.sp,
                                            color = catColor,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteCategory(cat) },
                                        modifier = Modifier.testTag("admin_delete_cat_${cat.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف القسم",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Files Tab
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(files, key = { it.id }) { fileItem ->
                            val cat = categories.find { it.id == fileItem.categoryId }
                            val catName = cat?.name ?: "عام"
                            val catColor = parseColorSafe(cat?.colorHex ?: "#2563EB")

                            FileCardItem(
                                fileItem = fileItem,
                                categoryName = catName,
                                categoryColor = catColor,
                                onClick = {
                                    when (fileItem.fileType.uppercase()) {
                                        "PDF" -> viewModel.navigateTo(Screen.PdfViewer(fileItem))
                                        "VIDEO" -> viewModel.navigateTo(Screen.VideoPlayer(fileItem))
                                        else -> viewModel.navigateTo(Screen.ImageViewer(fileItem))
                                    }
                                },
                                onDelete = { viewModel.deleteFile(fileItem) }
                            )
                        }
                    }
                }

                2 -> {
                    // Users Tab ("أقدر اعمل كتير من ده")
                    Column(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "إدارة حسابات وصول الطلاب (${users.size} حساب)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "شارك اسم المستخدم وكلمة السر مع الطلاب للوصول للأقسام المحددة لهم",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(users, key = { it.id }) { user ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("admin_user_card_${user.id}")
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(if (user.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = if (user.isActive) MaterialTheme.colorScheme.primary else Color.Gray,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = user.displayName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Surface(
                                                        color = if (user.isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = if (user.isActive) "مفعّل" else "معطّل",
                                                            color = if (user.isActive) Color(0xFF15803D) else Color(0xFFDC2626),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }

                                                if (user.notes.isNotBlank()) {
                                                    Text(
                                                        text = user.notes,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Switch(
                                                checked = user.isActive,
                                                onCheckedChange = { viewModel.toggleUserActive(user) },
                                                modifier = Modifier.testTag("toggle_user_active_${user.id}")
                                            )

                                            IconButton(
                                                onClick = { viewModel.deleteUserAccount(user) },
                                                modifier = Modifier.testTag("delete_user_${user.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "حذف",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Credential Pills
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "المستخدم: ",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = user.username,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }

                                            Surface(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "كلمة السر: ",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = user.password,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    val clip = ClipData.newPlainText("بيانات دخول الطالب", "اسم المستخدم: ${user.username}\nكلمة المرور: ${user.password}")
                                                    clipboard.setPrimaryClip(clip)
                                                    viewModel.showSnackbar("تم نسخ بيانات الدخول إلى الحافظة")
                                                },
                                                modifier = Modifier.size(36.dp).testTag("copy_credentials_${user.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "نسخ",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Allowed Categories Info
                                        val accessText = if (user.allowedCategoryIds == "*") {
                                            "صلاحية كاملة لجميع المواد والأقسام"
                                        } else {
                                            val catNames = user.allowedCategoryIds.split(",").mapNotNull { idStr ->
                                                val id = idStr.trim().toLongOrNull()
                                                categories.find { it.id == id }?.name
                                            }
                                            "المواد المصرحة: ${catNames.joinToString("، ")}"
                                        }

                                        Text(
                                            text = accessText,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Quizzes Tab
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(quizzes, key = { it.id }) { quiz ->
                            val cat = categories.find { it.id == quiz.categoryId }
                            val catName = cat?.name ?: "عام"
                            val catColor = parseColorSafe(cat?.colorHex ?: "#2563EB")

                            QuizCardItem(
                                quiz = quiz,
                                categoryName = catName,
                                categoryColor = catColor,
                                onStartQuiz = { viewModel.startQuiz(quiz) },
                                onDelete = { viewModel.deleteQuiz(quiz) }
                            )
                        }
                    }
                }

                4 -> {
                    // Results Tab
                    if (results.isEmpty()) {
                        EmptyStateCard(
                            title = "لا توجد نتائج اختبارات حتى الآن",
                            subtitle = "عندما يقوم الطلاب بإنهاء الكويزات ستظهر درجاتهم وتاريخ إنجازهم هنا"
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(results, key = { it.id }) { res ->
                                QuizResultCardItem(result = res)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Instances
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, desc, colorHex, iconName ->
                viewModel.addCategory(name, desc, colorHex, iconName)
                showAddCategoryDialog = false
            }
        )
    }

    if (showAddFileDialog) {
        AddFileDialog(
            categories = categories,
            onDismiss = { showAddFileDialog = false },
            onConfirm = { catId, title, desc, type, uri, size, extra ->
                viewModel.addFile(catId, title, desc, type, uri, size, extra)
                showAddFileDialog = false
            }
        )
    }

    if (showAddUserDialog) {
        AddUserDialog(
            categories = categories,
            onDismiss = { showAddUserDialog = false },
            onConfirm = { username, pass, name, allowedCatIds, notes ->
                viewModel.addUserAccount(username, pass, name, allowedCatIds, notes)
                showAddUserDialog = false
            }
        )
    }

    if (showAddQuizDialog) {
        AddQuizDialog(
            categories = categories,
            onDismiss = { showAddQuizDialog = false },
            onConfirm = { quiz, questions ->
                viewModel.createQuiz(quiz, questions)
                showAddQuizDialog = false
            }
        )
    }

    if (showChangePinDialog) {
        ChangeAdminPinDialog(
            currentPin = adminPin,
            onDismiss = { showChangePinDialog = false },
            onConfirm = { newPin ->
                viewModel.updateAdminPin(newPin)
                showChangePinDialog = false
            }
        )
    }
}
