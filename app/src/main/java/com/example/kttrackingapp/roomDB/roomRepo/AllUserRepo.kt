package com.example.kttrackingapp.roomDB.roomRepo

import com.example.kttrackingapp.roomDB.roomDao.AllUserDao
import com.example.kttrackingapp.roomDB.roomTable.AllUserTable
import kotlinx.coroutines.flow.Flow

class AllUserRepo(private val dao: AllUserDao) {

    suspend fun saveItem(item: AllUserTable) = dao.insert(item)

    suspend fun removeItem(item: AllUserTable) = dao.delete(item)

    suspend fun checkUser(phoneNumber: String): Boolean {
        return dao.checkUser(phoneNumber)
    }

    suspend fun checkLogin(mobile: String, password: String): Boolean {
        return dao.checkLogin(mobile, password)
    }


    suspend fun getUserId(phoneNumber: String): Int {
        return dao.getUserId(phoneNumber)
    }

    suspend fun getUserData(user_id: Int): AllUserTable {
        return dao.getUserData(user_id)
    }


}