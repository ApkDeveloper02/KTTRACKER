package com.example.kttrackingapp.roomDB.roomVM

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kttrackingapp.Utils
import com.example.kttrackingapp.roomDB.roomRepo.UserDetailRepo
import com.example.kttrackingapp.roomDB.roomTable.UserDetailTable
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException



data class MapSelection(
    val latitude: Double,
    val longitude: Double,
    val date: String,
    val time: String
)


class UserDetailVM(private val repo : UserDetailRepo ,
                   application: Application) : AndroidViewModel(application)
{

    var selectedMapLocation = mutableStateOf<MapSelection?>(null)

    var logOutAlert = mutableStateOf(false)
    var screenChange = mutableStateOf(false)

    private val _userData = MutableStateFlow<List<UserDetailTable>>(emptyList())
    val userData = _userData.asStateFlow()


    fun GetUserData(user_id: Int) {
        viewModelScope.launch {
            repo.getData(user_id).collect { list ->
                _userData.value = list
            }
        }
    }

    fun SaveUserDetail(
        userId: Int,
        latitude: String,
        longitude: String,
        time: String
    ) {
        viewModelScope.launch {

            val data = UserDetailTable(
                data_id = 0, // MUST be 0 for autoGenerate
                user_id = userId,
                user_latitude = latitude,
                user_longitude = longitude,
                user_time = time
            )

            repo.saveData(data)

            Utils.ToastMessage("Location saved ✅")
        }
    }

    @SuppressLint("MissingPermission")
    fun SaveCurrentLocationToDB(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(getApplication<Application>())

                val location = suspendCancellableCoroutine<Location?> { cont ->
                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { loc -> cont.resume(loc) }
                        .addOnFailureListener { e -> cont.resumeWithException(e) }
                }

                location?.let {
                    val data = UserDetailTable(
                        data_id = 0,
                        user_id = userId,
                        user_latitude = it.latitude.toString(),
                        user_longitude = it.longitude.toString(),
                        user_time = System.currentTimeMillis().toString()
                    )
                    repo.saveData(data)
                    Log.d("ViewModel", "✅ Initial location saved: ${it.latitude}, ${it.longitude}")
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "❌ Failed to save location: ${e.message}")
            }
        }
    }
}