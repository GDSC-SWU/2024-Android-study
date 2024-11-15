package com.example.todolist.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(TodoEntity::class), version = 1)   // 조건1
abstract class AppDatabase : RoomDatabase() {   // 조건2

    abstract fun getTodoDao() : TodoDao // 조건3

    companion object{
        val databaseName = "db_todo"    // 데이터베이스 이름
        var appDatabase : AppDatabase? = null

        fun getInstance(context : Context) : AppDatabase? {
            if(appDatabase == null){
                appDatabase = Room.databaseBuilder(context,
                    AppDatabase::class.java,
                    databaseName).
                fallbackToDestructiveMigration()
                    .build()
            }
            return appDatabase
        }
    }
}