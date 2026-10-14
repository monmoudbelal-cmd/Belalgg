package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.EduDao
import com.example.data.model.CategoryEntity
import com.example.data.model.FileItemEntity
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.QuizResultEntity
import com.example.data.model.UserAccountEntity

@Database(
    entities = [
        CategoryEntity::class,
        FileItemEntity::class,
        UserAccountEntity::class,
        QuizEntity::class,
        QuizQuestionEntity::class,
        QuizResultEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EduDatabase : RoomDatabase() {

    abstract fun eduDao(): EduDao

    companion object {
        @Volatile
        private var INSTANCE: EduDatabase? = null

        fun getInstance(context: Context): EduDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EduDatabase::class.java,
                    "eduvault_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
