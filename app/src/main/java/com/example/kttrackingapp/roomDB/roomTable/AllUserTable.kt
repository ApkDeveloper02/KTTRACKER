package com.example.kttrackingapp.roomDB.roomTable

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AllUserTable")
data class AllUserTable(
    @PrimaryKey(autoGenerate = true)
    var user_id : Int,

    var userName : String,
    var userMobile : String,
    var userPassword : String,
)