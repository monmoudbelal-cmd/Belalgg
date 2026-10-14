package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CategorySelectorBar
import com.example.ui.components.FileCardItem
import com.example.ui.components.QuizCardItem
import com.example.ui.components.QuizResultCardItem
import com.example.ui.components.parseColorSafe
import com.example.ui.viewmodel.AuthState
import com.example.ui.viewmodel.EduViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    viewModel: EduViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.logout()
    }

    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val studentName = if (authState is AuthState.Student) {
        (authState as AuthState.Student).user.displayName
    } else {
        "طالب منصة EduVault"
    }

    val categories by viewModel.visibleCategories.collectAsStateWithLifecycle()
    val files by viewModel.visibleFiles.collectAsStateWithLifecycle()
    val quizzes by viewModel.visibleQuizzes.collectAsStateWithLifecycle()
    val allResults by viewModel.results.collectAsStateWithLifecycle()

    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Student specific results
    val studentUsername = if (authState is AuthState.Student) (authState as AuthState.Student).user.username else ""
    val studentResults = allResults.filter { it.username == studentUsername }

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
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "الطالب",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = studentName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "بوابة المواد والملفات المصرحة لك",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("student_logout_button")
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("student_search_input"),
                placeholder = { Text("ابحث في ملفاتك، المذكرات، الكويزات...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "بحث")
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Category Chips Row
            CategorySelectorBar(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = { viewModel.selectedCategoryId.value = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Tabs Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الملفات (${files.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_files")
                )

                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الكويزات (${quizzes.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_quizzes")
                )

                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("نتائجي (${studentResults.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_results")
                )
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // Files List
                    if (files.isEmpty()) {
                        EmptyStateCard(
                            title = "لا توجد ملفات في هذا القسم",
                            subtitle = "لم يقم المعلم برفع ملفات في هذا القسم أو تم مسح البحث"
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                                    }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // Quizzes List
                    if (quizzes.isEmpty()) {
                        EmptyStateCard(
                            title = "لا توجد كويزات متاحة حالياً",
                            subtitle = "سيتم إتاحة اختبارات هذا القسم فور نشرها من قبل المعلم"
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                                    onStartQuiz = { viewModel.startQuiz(quiz) }
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // Results List
                    if (studentResults.isEmpty()) {
                        EmptyStateCard(
                            title = "لم تجتز أي كويزات بعد",
                            subtitle = "انتقل إلى تبويب الكويزات وابدأ اختبارك الأول لتسجيل درجتك هنا"
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(studentResults, key = { it.id }) { res ->
                                QuizResultCardItem(result = res)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
