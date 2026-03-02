package com.example.kttrackingapp

import android.content.Context
import android.content.SharedPreferences

class SharedHelper {
    var sharedPreferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    fun putString(context: Context, Key: String?, Value: String?) {
        try {
            sharedPreferences = context.getSharedPreferences("Cache", Context.MODE_PRIVATE)
            editor = sharedPreferences!!.edit()
            editor?.putString(Key, Value)
            editor?.commit()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }


    fun getString(contextGetKey: Context, Key: String?): String? {
        sharedPreferences = contextGetKey.getSharedPreferences("Cache", Context.MODE_PRIVATE)
        return sharedPreferences!!.getString(Key, "")
    }

    fun putLong(context: Context, Key: String?, Value: Long?) {
        try {
            sharedPreferences = context.getSharedPreferences("Cache", Context.MODE_PRIVATE)
            editor = sharedPreferences!!.edit()
            Value?.let { editor?.putLong(Key, it) }
            editor?.commit()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun getLong(contextGetKey: Context, Key: String?): Long {
        sharedPreferences = contextGetKey.getSharedPreferences("Cache", Context.MODE_PRIVATE)
        return sharedPreferences!!.getLong(Key,0L )
    }

    fun putInt(context: Context, Key: String?, Value: Int?) {
        try {
            sharedPreferences = context.getSharedPreferences("Cache", Context.MODE_PRIVATE)
            editor = sharedPreferences!!.edit()
            Value?.let { editor?.putInt(Key, it) }
            editor?.commit()
        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun getInt(contextGetKey: Context, Key: String?): Int {
        sharedPreferences = contextGetKey.getSharedPreferences("Cache", Context.MODE_PRIVATE)
        return sharedPreferences!!.getInt(Key,0)
    }

    fun putBoolean(context: Context, Key: String?, Value: Boolean?) {
        try {
            sharedPreferences = context.getSharedPreferences("Cache", Context.MODE_PRIVATE)
            editor = sharedPreferences!!.edit()
            editor?.putBoolean(Key, Value!!)
            editor?.commit()
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBoolean(contextGetKey: Context, Key: String): Boolean {
        sharedPreferences = contextGetKey.getSharedPreferences("Cache", Context.MODE_PRIVATE)
        return sharedPreferences!!.getBoolean(Key, false)
    }

    fun clearSharedPreferences(context: Context) {
        sharedPreferences = context.getSharedPreferences("Cache", Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.clear()?.apply()
    }

}