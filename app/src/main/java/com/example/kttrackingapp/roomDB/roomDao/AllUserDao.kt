package com.example.kttrackingapp.roomDB.roomDao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kttrackingapp.roomDB.roomTable.AllUserTable
import kotlinx.coroutines.flow.Flow

@Dao
interface AllUserDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(item: AllUserTable)

    @Delete
    suspend fun delete(item: AllUserTable)

    @Query("SELECT EXISTS(SELECT 1 FROM AllUserTable WHERE userMobile = :phoneNumber)")
    suspend fun checkUser(phoneNumber: String): Boolean


    @Query("SELECT EXISTS( SELECT 1 FROM AllUserTable WHERE userMobile = :mobile AND userPassword = :password)")
    suspend fun checkLogin(mobile: String, password: String): Boolean

    @Query("SELECT user_id FROM ALLUSERTABLE WHERE userMobile = :phoneNumber")
    suspend fun getUserId(phoneNumber: String): Int


    @Query("SELECT * FROM ALLUSERTABLE WHERE user_id = :user_id")
    suspend fun getUserData(user_id: Int): AllUserTable

}