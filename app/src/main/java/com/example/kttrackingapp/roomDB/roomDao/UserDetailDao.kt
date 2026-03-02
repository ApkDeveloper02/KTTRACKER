package com.example.kttrackingapp.roomDB.roomDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kttrackingapp.roomDB.roomTable.AllUserTable
import com.example.kttrackingapp.roomDB.roomTable.UserDetailTable
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDetailDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertData(data: UserDetailTable)


    @Query( "Select * from UserDetailTable where user_id =:user_id")
    fun getData(user_id : Int) : Flow<List<UserDetailTable>>

}