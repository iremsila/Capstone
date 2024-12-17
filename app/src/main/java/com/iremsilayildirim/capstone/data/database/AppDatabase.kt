package com.iremsilayildirim.capstone.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iremsilayildirim.capstone.data.dao.UserDao
import com.iremsilayildirim.capstone.data.model.User

@Database(entities = [User::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

}
