package com.example.kttrackingapp

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.kttrackingapp.roomDB.roomDao.UserDetailDao
import com.example.kttrackingapp.roomDB.roomTable.UserDetailTable
import com.example.kttrackingapp.screen.startLocationTrackingIfNeeded
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/*
class LocationWorker(
    private val context: Context,
    params: WorkerParameters,
    private val userDetailDao: UserDetailDao
) : CoroutineWorker(context, params)
{

    override suspend fun doWork(): Result {
        return try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("LocationWorker", "❌ Permission not granted")
                return Result.failure()
            }

            val fusedClient = LocationServices.getFusedLocationProviderClient(context)

            // ✅ Try getCurrentLocation with a timeout
            val location: Location? = withTimeoutOrNull(10_000L) {
                suspendCancellableCoroutine { cont ->
                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { loc -> cont.resume(loc) }
                        .addOnFailureListener { e -> cont.resumeWithException(e) }
                }
            } ?: run {
                // ✅ Fallback to lastLocation if getCurrentLocation times out or returns null
                Log.w("LocationWorker", "⚠️ getCurrentLocation timed out, using lastLocation")
                suspendCancellableCoroutine { cont ->
                    fusedClient.lastLocation
                        .addOnSuccessListener { loc -> cont.resume(loc) }
                        .addOnFailureListener { e -> cont.resumeWithException(e) }
                }
            }

            if (location != null) {
                val sharedHelper = SharedHelper()
                val userId = sharedHelper.getInt(applicationContext, "user_id_share")

                val data = UserDetailTable(
                    data_id = 0,
                    user_id = userId,
                    user_latitude = location.latitude.toString(),
                    user_longitude = location.longitude.toString(),
                    user_time = System.currentTimeMillis().toString()
                )

                userDetailDao.insertData(data)
                Log.d("LocationWorker", "✅ Location saved: ${location.latitude}," +
                        " ${location.longitude}, ${System.currentTimeMillis()}")
            } else {
                // ⚠️ Don't fail — retry next cycle
                Log.w("LocationWorker", "⚠️ Both location sources returned null")
            }

            Result.success()

        } catch (e: Exception) {
            Log.e("LocationWorker", "❌ Error: ${e.message}")
            Result.retry()
        }
    }
}*/


//For one min
/*
class LocationWorker(
    private val context: Context,
    params: WorkerParameters,
    private val userDetailDao: UserDetailDao
) : CoroutineWorker(context, params)
{

    override suspend fun doWork(): Result {

        // ✅ Set as foreground to prevent OS from killing it
        setForeground(createForegroundInfo())

        return try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("LocationWorker", "❌ Permission not granted")
                return Result.failure()
            }

            val fusedClient = LocationServices.getFusedLocationProviderClient(context)

            val location: Location? = withTimeoutOrNull(10_000L) {
                suspendCancellableCoroutine { cont ->
                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { loc -> cont.resume(loc) }
                        .addOnFailureListener { e -> cont.resumeWithException(e) }
                }
            } ?: run {
                Log.w("LocationWorker", "⚠️ Falling back to lastLocation")
                suspendCancellableCoroutine { cont ->
                    fusedClient.lastLocation
                        .addOnSuccessListener { loc -> cont.resume(loc) }
                        .addOnFailureListener { e -> cont.resumeWithException(e) }
                }
            }

            if (location != null) {
                val sharedHelper = SharedHelper()
                val userId = sharedHelper.getInt(applicationContext, "user_id_share")

                val data = UserDetailTable(
                    data_id = 0,
                    user_id = userId,
                    user_latitude = location.latitude.toString(),
                    user_longitude = location.longitude.toString(),
                    user_time = System.currentTimeMillis().toString()
                )
                userDetailDao.insertData(data)
                Log.d("LocationWorker", "✅ Location saved: ${location.latitude}, ${location.longitude}")
            }

            Result.success()

        } catch (e: Exception) {
            Log.e("LocationWorker", "❌ Error: ${e.message}")
            Result.retry()
        }.also {
                // ✅ Re-enqueue itself after completion for 1-min repeat
                val next = OneTimeWorkRequestBuilder<LocationWorker>()
                    .setInitialDelay(1, TimeUnit.MINUTES)
                    .addTag("location_tracking")
                    .build()

                WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                    "location_tracking",
                    ExistingWorkPolicy.REPLACE,
                    next
                )
            }
    }

    // ✅ Required for foreground worker
    private fun createForegroundInfo(): ForegroundInfo {
        val channelId = "location_tracking_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW // LOW = no sound
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Tracking Location")
            .setContentText("Saving your location every 15 minutes")
            .setSmallIcon(R.drawable.location) // use any icon from your drawable
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(1001, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            ForegroundInfo(1001, notification)
        }
    }
}*/


class LocationForegroundService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(1003, buildNotification())
        startLocationTrackingIfNeeded(this)

        return START_STICKY // ✅ Restarts if killed
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        // ✅ App was swiped away — restart the service
        val restartIntent = Intent(applicationContext, LocationForegroundService::class.java)
        val pendingIntent = PendingIntent.getService(
            applicationContext, 1, restartIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 1000,
            pendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }

    private fun buildNotification(): Notification {
        val channelId = "location_foreground_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Tracking Service",
                NotificationManager.IMPORTANCE_LOW // ✅ LOW = no sound, stays silent
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Tracking Active")
            .setContentText("Tracking your location in background")
            .setSmallIcon(R.drawable.location)
            .setOngoing(true) // ✅ Can't be dismissed by user
            .build()
    }
}


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == "android.intent.action.QUICKBOOT_POWERON") { // ✅ Xiaomi uses this

            val serviceIntent = Intent(context, LocationForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}


class LocationWorker(
    private val context: Context,
    params: WorkerParameters,
    private val userDetailDao: UserDetailDao
) : CoroutineWorker(context, params)
{

    override suspend fun doWork(): Result {
        return try {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.failure()
            }

            val fusedClient = LocationServices.getFusedLocationProviderClient(context)

            val location: Location? = withTimeoutOrNull(10_000L) {
                suspendCancellableCoroutine { cont ->
                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { loc -> cont.resume(loc) }
                        .addOnFailureListener { e -> cont.resumeWithException(e) }
                }
            } ?: run {
                suspendCancellableCoroutine { cont ->
                    fusedClient.lastLocation
                        .addOnSuccessListener { loc -> cont.resume(loc) }
                        .addOnFailureListener { e -> cont.resumeWithException(e) }
                }
            }

            if (location != null) {
                val userId = SharedHelper().getInt(applicationContext, "user_id_share")

                userDetailDao.insertData(
                    UserDetailTable(
                        data_id = 0,
                        user_id = userId,
                        user_latitude = location.latitude.toString(),
                        user_longitude = location.longitude.toString(),
                        user_time = System.currentTimeMillis().toString()
                    )
                )
                Log.d("LocationWorker", "✅ Saved: ${location.latitude}, ${location.longitude}")


                // ✅ Send notification after saving
                sendLocationNotification(location.latitude, location.longitude)
            }

            Result.success()

        } catch (e: Exception) {
            Log.e("LocationWorker", "❌ Error: ${e.message}")
            Result.retry()
        }
    }

    private fun sendLocationNotification(lat: Double, lng: Double) {
        val channelId = "location_tracking_channel"

        // ✅ Create channel (safe to call multiple times)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Location Updated 📍")
            .setContentText("Lat: $lat, Lng: $lng")
            .setSmallIcon(R.drawable.location)
            .setAutoCancel(true) // ✅ dismisses when tapped
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)

        // ✅ Check permission before sending (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                manager.notify(1002, notification)
            }
        } else {
            manager.notify(1002, notification)
        }
    }
}