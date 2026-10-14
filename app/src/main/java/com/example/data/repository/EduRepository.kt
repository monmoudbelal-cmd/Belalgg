package com.example.data.repository

import android.content.Context
import com.example.data.dao.EduDao
import com.example.data.model.CategoryEntity
import com.example.data.model.FileItemEntity
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.QuizResultEntity
import com.example.data.model.UserAccountEntity
import com.example.data.utils.SampleDataGenerator
import kotlinx.coroutines.flow.Flow

class EduRepository(
    private val dao: EduDao,
    private val context: Context
) {

    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategories()
    val allFiles: Flow<List<FileItemEntity>> = dao.getAllFiles()
    val allUsers: Flow<List<UserAccountEntity>> = dao.getAllUsers()
    val allQuizzes: Flow<List<QuizEntity>> = dao.getAllQuizzes()
    val allResults: Flow<List<QuizResultEntity>> = dao.getAllResults()

    suspend fun initializeSampleDataIfNeeded() {
        SampleDataGenerator.populateInitialDataIfEmpty(context, dao)
    }

    // Category operations
    suspend fun insertCategory(category: CategoryEntity): Long = dao.insertCategory(category)
    suspend fun updateCategory(category: CategoryEntity) = dao.updateCategory(category)
    suspend fun deleteCategory(category: CategoryEntity) {
        dao.deleteFilesByCategory(category.id)
        dao.deleteCategory(category)
    }

    // File operations
    fun getFilesByCategory(categoryId: Long): Flow<List<FileItemEntity>> = dao.getFilesByCategory(categoryId)
    suspend fun insertFile(fileItem: FileItemEntity): Long = dao.insertFile(fileItem)
    suspend fun updateFile(fileItem: FileItemEntity) = dao.updateFile(fileItem)
    suspend fun deleteFile(fileItem: FileItemEntity) = dao.deleteFile(fileItem)

    // User operations
    suspend fun authenticateUser(username: String, pass: String): UserAccountEntity? {
        val user = dao.getUserByUsername(username.trim().lowercase()) ?: return null
        if (!user.isActive) return null
        if (user.password.trim() == pass.trim()) {
            return user
        }
        return null
    }

    suspend fun insertUser(user: UserAccountEntity): Long {
        return dao.insertUser(user.copy(username = user.username.trim().lowercase()))
    }

    suspend fun updateUser(user: UserAccountEntity) {
        dao.updateUser(user.copy(username = user.username.trim().lowercase()))
    }

    suspend fun deleteUser(user: UserAccountEntity) = dao.deleteUser(user)

    // Quiz operations
    fun getQuizzesByCategory(categoryId: Long): Flow<List<QuizEntity>> = dao.getQuizzesByCategory(categoryId)
    suspend fun insertQuizWithQuestions(quiz: QuizEntity, questions: List<QuizQuestionEntity>): Long {
        val quizId = dao.insertQuiz(quiz)
        val linkedQuestions = questions.mapIndexed { index, q ->
            q.copy(quizId = quizId, orderIndex = index)
        }
        dao.insertQuestions(linkedQuestions)
        return quizId
    }

    suspend fun deleteQuiz(quiz: QuizEntity) {
        dao.deleteQuestionsForQuiz(quiz.id)
        dao.deleteQuiz(quiz)
    }

    fun getQuestionsForQuiz(quizId: Long): Flow<List<QuizQuestionEntity>> = dao.getQuestionsForQuiz(quizId)
    suspend fun getQuestionsForQuizOnce(quizId: Long): List<QuizQuestionEntity> = dao.getQuestionsForQuizOnce(quizId)

    // Quiz results
    fun getResultsForUser(username: String): Flow<List<QuizResultEntity>> = dao.getResultsForUser(username)
    suspend fun recordQuizResult(result: QuizResultEntity): Long = dao.insertResult(result)
}
