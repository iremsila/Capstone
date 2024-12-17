package com.iremsilayildirim.capstone.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.iremsilayildirim.capstone.data.model.User

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: com.iremsilayildirim.capstone.data.model.User)

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun getUserByEmailAndPassword(email: String, password: String): User?
}
