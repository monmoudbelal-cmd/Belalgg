package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CategoryEntity
import com.example.data.model.FileItemEntity
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.QuizResultEntity
import com.example.data.model.UserAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EduDao {

    // --- Categories ---
    @Query("SELECT * FROM categories ORDER BY orderIndex ASC, id ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)

    // --- File Items ---
    @Query("SELECT * FROM file_items ORDER BY createdAt DESC")
    fun getAllFiles(): Flow<List<FileItemEntity>>

    @Query("SELECT * FROM file_items WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun getFilesByCategory(categoryId: Long): Flow<List<FileItemEntity>>

    @Query("SELECT * FROM file_items WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: Long): FileItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(fileItem: FileItemEntity): Long

    @Update
    suspend fun updateFile(fileItem: FileItemEntity)

    @Delete
    suspend fun deleteFile(fileItem: FileItemEntity)

    @Query("DELETE FROM file_items WHERE categoryId = :categoryId")
    suspend fun deleteFilesByCategory(categoryId: Long)

    // --- User Accounts ---
    @Query("SELECT * FROM user_accounts ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserAccountEntity?

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun countUsers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccountEntity): Long

    @Update
    suspend fun updateUser(user: UserAccountEntity)

    @Delete
    suspend fun deleteUser(user: UserAccountEntity)

    // --- Quizzes ---
    @Query("SELECT * FROM quizzes ORDER BY createdAt DESC")
    fun getAllQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun getQuizzesByCategory(categoryId: Long): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE id = :id LIMIT 1")
    suspend fun getQuizById(id: Long): QuizEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity): Long

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    @Delete
    suspend fun deleteQuiz(quiz: QuizEntity)

    // --- Quiz Questions ---
    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId ORDER BY orderIndex ASC, id ASC")
    fun getQuestionsForQuiz(quizId: Long): Flow<List<QuizQuestionEntity>>

    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId ORDER BY orderIndex ASC, id ASC")
    suspend fun getQuestionsForQuizOnce(quizId: Long): List<QuizQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuizQuestionEntity>)

    @Query("DELETE FROM quiz_questions WHERE quizId = :quizId")
    suspend fun deleteQuestionsForQuiz(quizId: Long)

    // --- Quiz Results ---
    @Query("SELECT * FROM quiz_results ORDER BY completedAt DESC")
    fun getAllResults(): Flow<List<QuizResultEntity>>

    @Query("SELECT * FROM quiz_results WHERE username = :username ORDER BY completedAt DESC")
    fun getResultsForUser(username: String): Flow<List<QuizResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: QuizResultEntity): Long
}
