package com.example.kttrackingapp.roomDB.roomTable

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserDetailTable")
data class UserDetailTable(
    @PrimaryKey(autoGenerate = true)
    var data_id : Int,

    var user_id : Int,
    var user_latitude : String,
    var user_longitude : String,
    var user_time : String,
)