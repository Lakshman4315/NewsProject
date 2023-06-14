package com.example.materialnewsapp.data.db


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.materialnewsapp.data.api.model.Article

@Database(entities = [Article::class], version = 4)
@TypeConverters(Converters::class)
abstract class ArticleDatabase : RoomDatabase() {
    abstract fun getArticleDao(): ArticleDao

    companion object {
        @Volatile
        private var INSTANCE: ArticleDatabase? = null

        fun getDatabase(context: Context): ArticleDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: createDatabase(context).also { INSTANCE = it }
            }
        }

        private fun createDatabase(context: Context): ArticleDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ArticleDatabase::class.java,
                "article_database"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
//    val migration_2_3 = object : Migration(2, 3) {
//        override fun migrate(database: SupportSQLiteDatabase) {
//            // Perform the necessary database schema changes here
//            // For example, if you need to add a new column:
//            database.execSQL("ALTER TABLE articles ADD COLUMN  TEXT")
//        }
//    }

}


