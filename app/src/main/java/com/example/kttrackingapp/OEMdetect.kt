package com.example.kttrackingapp

import android.Manifest
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat



fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

//fun isAutostartEnabled(context: Context): Boolean {
//    return try {
//        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
//        val op = AppOpsManager.permissionToOp("android.permission.START_ACTIVITIES_FROM_BACKGROUND")
//            ?: return false
//        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            appOpsManager.unsafeCheckOpNoThrow(op, android.os.Process.myUid(), context.packageName)
//        } else {
//            @Suppress("DEPRECATION")
//            appOpsManager.checkOpNoThrow(op, android.os.Process.myUid(), context.packageName)
//        }
//        mode == AppOpsManager.MODE_ALLOWED
//    } catch (e: Exception) {
//        Log.e("Autostart", "Could not check: ${e.message}")
//        false
//    }
//}

enum class RomType { MIUI, OPPO, VIVO, HUAWEI, SAMSUNG, OTHER }

fun detectRomType(): RomType {
    val brand = Build.BRAND.lowercase()
    val manufacturer = Build.MANUFACTURER.lowercase()
    return when {
        !getSystemProperty("ro.miui.ui.version.name").isNullOrEmpty() -> RomType.MIUI
        brand.contains("oppo") || manufacturer.contains("oppo") ||
                brand.contains("realme") || manufacturer.contains("realme") ||
                brand.contains("oneplus") || manufacturer.contains("oneplus") -> RomType.OPPO
        brand.contains("vivo") || manufacturer.contains("vivo") -> RomType.VIVO
        brand.contains("huawei") || manufacturer.contains("huawei") -> RomType.HUAWEI
        brand.contains("samsung") || manufacturer.contains("samsung") -> RomType.SAMSUNG
        else -> RomType.OTHER
    }
}


// Replace isAutostartEnabled() with this
fun isAutostartEnabled(context: Context): Boolean {
    // ❌ AppOps START_ACTIVITIES_FROM_BACKGROUND does NOT map to OEM autostart toggle
    // ✅ We cannot programmatically read MIUI/OPPO/Vivo autostart state
    // ✅ So we trust the user — if they went to settings and came back, assume enabled
    return true
}
fun requiresAutostartPermission(): Boolean {
    return detectRomType() != RomType.SAMSUNG && detectRomType() != RomType.OTHER
}

fun openAutostartSettings(context: Context) {
    // ✅ OPPO needs special multi-candidate handling — handle separately
    if (detectRomType() == RomType.OPPO) {
        openOppoAutostartSettings(context)
        return
    }

    val intent = when (detectRomType()) {
        RomType.MIUI -> Intent().apply {
            component = ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
        }
        RomType.VIVO -> Intent().apply {
            component = ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
            )
        }
        RomType.HUAWEI -> Intent().apply {
            component = ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            )
        }
        else -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        )
    }
}

fun openOppoAutostartSettings(context: Context) {
    val candidates = listOf(
        // ColorOS 12+ / newer OPPO
        "com.oplus.safecenter" to "com.oplus.safecenter.internaljump.AppJumpSettingActivity",
        // ColorOS 11
        "com.coloros.safecenter" to "com.coloros.safecenter.startupapp.StartupAppListActivity",
        // Older ColorOS
        "com.coloros.safecenter" to "com.coloros.privacypermissionsentry.PermissionTopActivity",
        // Realme UI
        "com.realme.safecenter" to "com.realme.safecenter.startupapp.StartupAppListActivity",
        // OnePlus OxygenOS
        "com.oneplus.security" to "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity",
    )

    for ((pkg, cls) in candidates) {
        try {
            val intent = Intent().apply {
                component = ComponentName(pkg, cls)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            // ✅ Check if this activity actually exists on this device before launching
            val resolved = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolved != null) {
                context.startActivity(intent)
                Log.d("Autostart", "✅ Opened OPPO autostart via $pkg / $cls")
                return // ✅ Stop at first successful one
            }
        } catch (e: Exception) {
            Log.e("Autostart", "❌ Failed $pkg: ${e.message}")
        }
    }

    // Final fallback — App Info page
    Log.d("Autostart", "⚠️ No OPPO autostart screen found, opening App Info")
    context.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
}

fun getSystemProperty(key: String): String? {
    return try {
        val clazz = Class.forName("android.os.SystemProperties")
        val method = clazz.getMethod("get", String::class.java)
        method.invoke(null, key) as? String
    } catch (e: Exception) { null }
}

object AutostartPrefs {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_AUTOSTART_CONFIRMED = "autostart_confirmed"
    private const val KEY_DISMISS_COUNT = "autostart_dismiss_count"
    private const val KEY_APP_OPEN_COUNT = "app_open_count"

    fun shouldShowDialog(context: Context): Boolean {
        if (!requiresAutostartPermission()) return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_AUTOSTART_CONFIRMED, false)) return false
        val dismissCount = prefs.getInt(KEY_DISMISS_COUNT, 0)
        val appOpenCount = prefs.getInt(KEY_APP_OPEN_COUNT, 0)
        return dismissCount == 0 || (appOpenCount % 3 == 0)
    }

    fun incrementAppOpen(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_APP_OPEN_COUNT, prefs.getInt(KEY_APP_OPEN_COUNT, 0) + 1).apply()
    }

    fun onUserDismissed(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_DISMISS_COUNT, prefs.getInt(KEY_DISMISS_COUNT, 0) + 1).apply()
    }

    fun onUserConfirmed(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_AUTOSTART_CONFIRMED, true).apply()
    }

    fun isConfirmed(context: Context): Boolean {
        return !requiresAutostartPermission() ||
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .getBoolean(KEY_AUTOSTART_CONFIRMED, false)
    }
}

