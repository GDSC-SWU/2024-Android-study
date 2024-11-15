package com.example.todolist.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity
data class TodoEntity (


    @PrimaryKey(autoGenerate = true) var id : Int? = null,
    @ColumnInfo(name="title")var title : String,
    @ColumnInfo(name="importance")var importance : Int
    )