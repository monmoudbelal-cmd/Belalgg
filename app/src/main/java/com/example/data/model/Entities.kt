package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val colorHex: String = "#2563EB",
    val iconName: String = "folder",
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "file_items")
data class FileItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val title: String,
    val description: String = "",
    val fileType: String, // "PDF", "VIDEO", "IMAGE"
    val uriString: String, // content://, file://, or bundled/sample identifier
    val fileSize: String = "",
    val extraContent: String = "", // text notes / syllabus / preview text
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String, // unique login handle
    val password: String, // secret passcode set by the teacher/admin
    val displayName: String, // student name e.g. "أحمد محمد"
    val allowedCategoryIds: String = "*", // "*" means all, or "1,2,5"
    val isActive: Boolean = true,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val title: String,
    val description: String = "",
    val durationMinutes: Int = 10,
    val passScorePercent: Int = 60,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizId: Long,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOptionIndex: Int, // 0 for A, 1 for B, 2 for C, 3 for D
    val explanation: String = "",
    val orderIndex: Int = 0
)

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizId: Long,
    val quizTitle: String,
    val username: String,
    val studentName: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Int,
    val isPassed: Boolean,
    val completedAt: Long = System.currentTimeMillis()
)
