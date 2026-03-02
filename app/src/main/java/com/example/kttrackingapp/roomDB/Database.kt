package com.example.kttrackingapp.roomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kttrackingapp.roomDB.roomDao.AllUserDao
import com.example.kttrackingapp.roomDB.roomDao.UserDetailDao
import com.example.kttrackingapp.roomDB.roomTable.AllUserTable
import com.example.kttrackingapp.roomDB.roomTable.UserDetailTable

@Database(
    entities = [AllUserTable::class , UserDetailTable::class],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun allUserDao(): AllUserDao
    abstract fun userDetailDao() : UserDetailDao
}