package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.BookRepository
import com.example.data.SettingsManager

class MyApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "ebook_database").fallbackToDestructiveMigration().build()
    }
    val repository by lazy {
        BookRepository(database.bookDao())
    }
    val settingsManager by lazy {
        SettingsManager(this)
    }
}
