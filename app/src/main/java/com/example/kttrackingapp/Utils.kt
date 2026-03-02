package com.example.kttrackingapp

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf

class Utils {
    companion object {

        lateinit var activity: MainActivity

        var sharedHelper = SharedHelper()

        var notification = mutableStateOf(false)

        var user_id_share = "user_id_share"
        var login_Completed = "login_Completed"
        var notiEnable_share = "notiEnable_share"

        var user_id = 0

        fun ToastMessage(message: String) {

            try {
                val toast = Toast.makeText(
                    activity, message, Toast.LENGTH_SHORT
                )

                toast.show()


                val handler = Handler(Looper.myLooper()!!)
                handler.postDelayed({ toast.cancel() }, 1200)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}