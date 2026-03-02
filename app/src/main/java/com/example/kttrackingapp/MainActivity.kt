package com.example.kttrackingapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.example.kttrackingapp.navigation.NavigationBuild
import com.example.kttrackingapp.ui.theme.KTtrackingAppTheme

class MainActivity : ComponentActivity() {

    private val dataEnforce =  mutableStateOf(false)
    private val notiRequestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            dataEnforce.value = true   // 🔥 triggers recomposition
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setSystemUIVisibility(true, this)

        Utils.activity = this

        Utils.user_id = Utils.sharedHelper.getInt(this , Utils.user_id_share)


        setContent {
            val navHost = rememberNavController()


            // ✅ Navigation + Permission logic (UI)
            if (!dataEnforce.value)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    when {
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            println("NOTI-CHECK----1")
                            NavigationBuild(
                                navHostController = navHost ,
                                mainActivity = this
                            )
                        }
                        shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                            println("NOTI-CHECK----2")
                            NavigationBuild(
                                navHostController = navHost ,
                                mainActivity = this
                            )
                        }
                        else -> {
                            notiRequestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                } else {
                    println("NOTI-CHECK----3")
                    NavigationBuild(
                        navHostController = navHost ,
                        mainActivity = this
                    )
                }
            } else {
                println("NOTI-CHECK----4")
                NavigationBuild(
                    navHostController = navHost ,
                    mainActivity = this
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        println("ONRESUME")

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
        {
            Utils.sharedHelper.putBoolean(this, Utils.notiEnable_share, true)
            Utils.notification.value = Utils.sharedHelper.getBoolean(this, Utils.notiEnable_share)
        }
        else
        {
            Utils.sharedHelper.putBoolean(this, Utils.notiEnable_share, false)
            Utils.notification.value = Utils.sharedHelper.getBoolean(this, Utils.notiEnable_share)
        }
    }
}


fun setSystemUIVisibility(hide: Boolean, mainActivity: MainActivity) {
    val window = mainActivity.window
    val controller = WindowCompat.getInsetsController(window, window.decorView)

    if (hide) {
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
        controller.show(WindowInsetsCompat.Type.systemBars())
    }
}


@Composable
fun rememberNotchHeightDp(): State<Dp> {
    val context = LocalContext.current
    val density = LocalDensity.current
    val notchHeight = remember { mutableStateOf(0.dp) }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && context is Activity) {
            val insets = context.window.decorView.rootWindowInsets
            val cutout = insets?.displayCutout
            val notchPx = cutout?.safeInsetTop ?: 0

            notchHeight.value = with(density) { notchPx.toDp() } // ✅ convert px to dp
        }
    }

    return notchHeight
}


@SuppressLint("SuspiciousModifierThen")
fun Modifier.noRippleClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    this.then( // Use the Modifier instance
        clickable(
            enabled = enabled,
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
            onClick()
        }
    )
}