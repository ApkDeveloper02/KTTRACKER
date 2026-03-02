package com.example.kttrackingapp.roomDB.roomRepo

import com.example.kttrackingapp.roomDB.roomDao.AllUserDao
import com.example.kttrackingapp.roomDB.roomDao.UserDetailDao
import com.example.kttrackingapp.roomDB.roomTable.AllUserTable
import com.example.kttrackingapp.roomDB.roomTable.UserDetailTable

class UserDetailRepo(private val dao: UserDetailDao) {

    suspend fun saveData(item: UserDetailTable) = dao.insertData(item)

    fun getData(user_id: Int) = dao.getData(user_id)

}