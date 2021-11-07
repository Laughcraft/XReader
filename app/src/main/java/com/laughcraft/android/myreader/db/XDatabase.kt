package com.laughcraft.android.myreader.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.laughcraft.android.myreader.db.dao.FavoritesDao
import com.laughcraft.android.myreader.db.dao.RecentsDao
import com.laughcraft.android.myreader.db.entity.BookmarkEntity
import com.laughcraft.android.myreader.db.entity.RecentFileEntity

@Database(entities = [
    RecentFileEntity::class,
    BookmarkEntity::class],
          exportSchema = true,
          version = 1)
abstract class XDatabase: RoomDatabase() {
    companion object {
        private const val DB_NAME = "files_db"
    
        @Volatile private var instance: XDatabase? = null
    
        fun getInstance(context: Context): XDatabase = instance ?: synchronized(this) {
            instance ?: buildDatabase(context.applicationContext).also { instance = it }
        }
    
        private fun buildDatabase(context: Context) = Room.databaseBuilder(context, XDatabase::class.java, DB_NAME).build()
    }
    
    abstract fun getRecentsDao(): RecentsDao
    abstract fun getFavoritesDao(): FavoritesDao
}