package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.EduDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.FileItemEntity
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.QuizResultEntity
import com.example.data.model.UserAccountEntity
import com.example.data.repository.EduRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    data object Login : Screen()
    data object StudentDashboard : Screen()
    data object AdminDashboard : Screen()
    data class PdfViewer(val fileItem: FileItemEntity) : Screen()
    data class VideoPlayer(val fileItem: FileItemEntity) : Screen()
    data class ImageViewer(val fileItem: FileItemEntity) : Screen()
    data class QuizTaker(val quiz: QuizEntity) : Screen()
    data class QuizResult(
        val result: QuizResultEntity,
        val questions: List<QuizQuestionEntity>,
        val userAnswers: Map<Int, Int>
    ) : Screen()
}

sealed class AuthState {
    data object LoggedOut : AuthState()
    data class Student(val user: UserAccountEntity) : AuthState()
    data object Admin : AuthState()
}

class EduViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EduRepository
    private val prefs = application.getSharedPreferences("eduvault_prefs", Context.MODE_PRIVATE)

    // Master Admin PIN (stored safely in private shared preferences)
    private val _adminPin = MutableStateFlow(prefs.getString("admin_pin", "1234") ?: "1234")
    val adminPin: StateFlow<String> = _adminPin.asStateFlow()

    // Navigation Stack
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Login))
    val currentScreen: StateFlow<Screen> = MutableStateFlow(Screen.Login)
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Login)
    val screen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Auth State
    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Category and Filter
    val selectedCategoryId = MutableStateFlow<Long?>(null)
    val searchQuery = MutableStateFlow("")

    // Raw Flows from DB
    val categories: StateFlow<List<CategoryEntity>>
    val files: StateFlow<List<FileItemEntity>>
    val users: StateFlow<List<UserAccountEntity>>
    val quizzes: StateFlow<List<QuizEntity>>
    val results: StateFlow<List<QuizResultEntity>>

    // Feedback message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Active Quiz State
    val activeQuizQuestions = MutableStateFlow<List<QuizQuestionEntity>>(emptyList())
    val currentQuestionIndex = MutableStateFlow(0)
    val userAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap()) // questionIndex -> selectedOptionIndex (0..3)
    val timeRemainingSeconds = MutableStateFlow(0)
    private var quizTimerJob: Job? = null

    init {
        val db = EduDatabase.getInstance(application)
        repository = EduRepository(db.eduDao(), application)

        categories = repository.allCategories.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        files = repository.allFiles.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        users = repository.allUsers.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        quizzes = repository.allQuizzes.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        results = repository.allResults.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        viewModelScope.launch {
            repository.initializeSampleDataIfNeeded()
        }
    }

    // Filtered Files based on student permissions, category selection, and search query
    val visibleFiles: StateFlow<List<FileItemEntity>> = combine(
        files,
        categories,
        selectedCategoryId,
        searchQuery,
        _authState
    ) { allFiles, allCats, catId, query, auth ->
        var list = allFiles
        if (auth is AuthState.Student) {
            val allowed = auth.user.allowedCategoryIds
            if (allowed != "*") {
                val allowedIds = allowed.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()
                list = list.filter { it.categoryId in allowedIds }
            }
        }
        if (catId != null) {
            list = list.filter { it.categoryId == catId }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { it.title.lowercase().contains(q) || it.description.lowercase().contains(q) }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Categories based on student access
    val visibleCategories: StateFlow<List<CategoryEntity>> = combine(
        categories,
        _authState
    ) { allCats, auth ->
        if (auth is AuthState.Student) {
            val allowed = auth.user.allowedCategoryIds
            if (allowed != "*") {
                val allowedIds = allowed.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()
                return@combine allCats.filter { it.id in allowedIds }
            }
        }
        allCats
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Quizzes
    val visibleQuizzes: StateFlow<List<QuizEntity>> = combine(
        quizzes,
        selectedCategoryId,
        _authState
    ) { allQuizzes, catId, auth ->
        var list = allQuizzes
        if (auth is AuthState.Student) {
            val allowed = auth.user.allowedCategoryIds
            if (allowed != "*") {
                val allowedIds = allowed.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()
                list = list.filter { it.categoryId in allowedIds }
            }
        }
        if (catId != null) {
            list = list.filter { it.categoryId == catId }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation
    fun navigateTo(screen: Screen) {
        val current = _screenStack.value
        _screenStack.value = current + screen
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        val current = _screenStack.value
        if (current.size > 1) {
            val newStack = current.dropLast(1)
            _screenStack.value = newStack
            _currentScreen.value = newStack.last()
            return true
        }
        return false
    }

    fun showSnackbar(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    // Authentication
    fun loginAsStudent(username: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (username.isBlank() || pass.isBlank()) {
                onResult(false, "يرجى إدخال اسم المستخدم وكلمة السر")
                return@launch
            }
            val user = repository.authenticateUser(username, pass)
            if (user != null) {
                _authState.value = AuthState.Student(user)
                _screenStack.value = listOf(Screen.StudentDashboard)
                _currentScreen.value = Screen.StudentDashboard
                onResult(true, "مرحباً بك يا ${user.displayName}")
            } else {
                onResult(false, "بيانات الدخول غير صحيحة أو الحساب غير مفعّل")
            }
        }
    }

    fun loginAsAdmin(enteredPin: String, onResult: (Boolean, String) -> Unit) {
        if (enteredPin.trim() == _adminPin.value.trim()) {
            _authState.value = AuthState.Admin
            _screenStack.value = listOf(Screen.AdminDashboard)
            _currentScreen.value = Screen.AdminDashboard
            onResult(true, "تم تسجيل دخول المسؤول بنجاح")
        } else {
            onResult(false, "كلمة سر المسؤول غير صحيحة")
        }
    }

    fun updateAdminPin(newPin: String) {
        if (newPin.isNotBlank()) {
            prefs.edit().putString("admin_pin", newPin.trim()).apply()
            _adminPin.value = newPin.trim()
            showSnackbar("تم تغيير كلمة سر المسؤول بنجاح")
        }
    }

    fun logout() {
        _authState.value = AuthState.LoggedOut
        _screenStack.value = listOf(Screen.Login)
        _currentScreen.value = Screen.Login
        selectedCategoryId.value = null
        searchQuery.value = null ?: ""
    }

    // Category CRUD
    fun addCategory(name: String, desc: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            val cat = CategoryEntity(
                name = name.trim(),
                description = desc.trim(),
                colorHex = colorHex,
                iconName = iconName,
                orderIndex = (categories.value.maxOfOrNull { it.orderIndex } ?: 0) + 1
            )
            repository.insertCategory(cat)
            showSnackbar("تمت إضافة القسم: $name")
        }
    }

    fun updateCategory(cat: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(cat)
            showSnackbar("تم تحديث القسم بنجاح")
        }
    }

    fun deleteCategory(cat: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(cat)
            showSnackbar("تم حذف القسم وكافة ملفاته")
        }
    }

    // File CRUD
    fun addFile(catId: Long, title: String, desc: String, type: String, uri: String, size: String, extra: String) {
        viewModelScope.launch {
            if (title.isBlank() || uri.isBlank()) return@launch
            val item = FileItemEntity(
                categoryId = catId,
                title = title.trim(),
                description = desc.trim(),
                fileType = type,
                uriString = uri.trim(),
                fileSize = size.ifBlank { "1.5 MB" },
                extraContent = extra
            )
            repository.insertFile(item)
            showSnackbar("تمت إضافة الملف بنجاح: $title")
        }
    }

    fun deleteFile(fileItem: FileItemEntity) {
        viewModelScope.launch {
            repository.deleteFile(fileItem)
            showSnackbar("تم حذف الملف")
        }
    }

    // User Account CRUD
    fun addUserAccount(username: String, pass: String, name: String, allowedCatIds: String, notes: String) {
        viewModelScope.launch {
            if (username.isBlank() || pass.isBlank() || name.isBlank()) return@launch
            val user = UserAccountEntity(
                username = username.trim().lowercase(),
                password = pass.trim(),
                displayName = name.trim(),
                allowedCategoryIds = allowedCatIds.ifBlank { "*" },
                isActive = true,
                notes = notes.trim()
            )
            repository.insertUser(user)
            showSnackbar("تم إنشاء حساب الطالب: $name ($username)")
        }
    }

    fun updateUserAccount(user: UserAccountEntity) {
        viewModelScope.launch {
            repository.updateUser(user)
            showSnackbar("تم تحديث بيانات الحساب")
        }
    }

    fun toggleUserActive(user: UserAccountEntity) {
        viewModelScope.launch {
            repository.updateUser(user.copy(isActive = !user.isActive))
            showSnackbar(if (!user.isActive) "تم تفعيل الحساب" else "تم إيقاف الحساب")
        }
    }

    fun deleteUserAccount(user: UserAccountEntity) {
        viewModelScope.launch {
            repository.deleteUser(user)
            showSnackbar("تم حذف الحساب")
        }
    }

    // Quiz Management
    fun createQuiz(quiz: QuizEntity, questions: List<QuizQuestionEntity>) {
        viewModelScope.launch {
            repository.insertQuizWithQuestions(quiz, questions)
            showSnackbar("تم إنشاء الكويز بنجاح مع ${questions.size} أسئلة")
        }
    }

    fun deleteQuiz(quiz: QuizEntity) {
        viewModelScope.launch {
            repository.deleteQuiz(quiz)
            showSnackbar("تم حذف الكويز")
        }
    }

    // Interactive Quiz Taking Flow
    fun startQuiz(quiz: QuizEntity) {
        viewModelScope.launch {
            val qs = repository.getQuestionsForQuizOnce(quiz.id)
            if (qs.isEmpty()) {
                showSnackbar("هذا الكويز لا يحتوي على أسئلة بعد!")
                return@launch
            }
            activeQuizQuestions.value = qs
            currentQuestionIndex.value = 0
            userAnswers.value = emptyMap()
            timeRemainingSeconds.value = quiz.durationMinutes * 60

            startTimer(quiz)
            navigateTo(Screen.QuizTaker(quiz))
        }
    }

    private fun startTimer(quiz: QuizEntity) {
        quizTimerJob?.cancel()
        quizTimerJob = viewModelScope.launch {
            while (timeRemainingSeconds.value > 0) {
                delay(1000)
                timeRemainingSeconds.value -= 1
            }
            // Auto submit when time expires
            submitQuiz(quiz)
        }
    }

    fun selectQuizAnswer(questionIndex: Int, optionIndex: Int) {
        userAnswers.value = userAnswers.value.toMutableMap().apply {
            put(questionIndex, optionIndex)
        }
    }

    fun submitQuiz(quiz: QuizEntity) {
        quizTimerJob?.cancel()
        val questions = activeQuizQuestions.value
        val answers = userAnswers.value

        var correctCount = 0
        questions.forEachIndexed { index, q ->
            val userPick = answers[index]
            if (userPick != null && userPick == q.correctOptionIndex) {
                correctCount++
            }
        }

        val total = questions.size
        val percentage = if (total > 0) (correctCount * 100) / total else 0
        val isPassed = percentage >= quiz.passScorePercent

        val currentUser = _authState.value
        val username = if (currentUser is AuthState.Student) currentUser.user.username else "admin_tester"
        val studentName = if (currentUser is AuthState.Student) currentUser.user.displayName else "تجربة المسؤول"

        val resultEntity = QuizResultEntity(
            quizId = quiz.id,
            quizTitle = quiz.title,
            username = username,
            studentName = studentName,
            score = correctCount,
            totalQuestions = total,
            percentage = percentage,
            isPassed = isPassed
        )

        viewModelScope.launch {
            repository.recordQuizResult(resultEntity)
            navigateTo(Screen.QuizResult(resultEntity, questions, answers))
        }
    }
}
