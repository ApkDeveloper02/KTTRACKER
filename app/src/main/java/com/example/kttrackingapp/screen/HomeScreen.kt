package com.example.kttrackingapp.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.kttrackingapp.AutostartPrefs
import com.example.kttrackingapp.CommonLoader
import com.example.kttrackingapp.CommonText
import com.example.kttrackingapp.LocationForegroundService
import com.example.kttrackingapp.LocationWorker
import com.example.kttrackingapp.LottieAnimation_Json
import com.example.kttrackingapp.R
import com.example.kttrackingapp.RomType
import com.example.kttrackingapp.Utils
import com.example.kttrackingapp.detectRomType
import com.example.kttrackingapp.formatTimestamp
import com.example.kttrackingapp.hasLocationPermission
import com.example.kttrackingapp.navigation.NavScreens
import com.example.kttrackingapp.noRippleClickable
import com.example.kttrackingapp.openAutostartSettings
import com.example.kttrackingapp.openInGoogleMaps
import com.example.kttrackingapp.rememberNotchHeightDp
import com.example.kttrackingapp.requiresAutostartPermission
import com.example.kttrackingapp.roomDB.roomTable.UserDetailTable
import com.example.kttrackingapp.roomDB.roomVM.AllUserVM
import com.example.kttrackingapp.roomDB.roomVM.MapSelection
import com.example.kttrackingapp.roomDB.roomVM.UserDetailVM
import com.example.kttrackingapp.scaledSp
import com.example.kttrackingapp.shareLocation
import com.example.kttrackingapp.ui.theme.blueApp
import com.example.kttrackingapp.ui.theme.inActiveTextColor
import com.example.kttrackingapp.ui.theme.lightBlueApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navHostController: NavHostController,
    userDBVM: AllUserVM = koinViewModel(),
    dbVM: UserDetailVM = koinViewModel()
)
{
    val context = LocalContext.current
    val activity = Utils.Companion.activity

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var backPressedOnce by remember { mutableStateOf(false) }

    // Reset flag after 2 seconds
    LaunchedEffect(backPressedOnce) {
        if (backPressedOnce) {
            delay(2000)
            backPressedOnce = false
        }
    }

    var hasLocPermission by remember { mutableStateOf(hasLocationPermission(context)) }
    // Add this with your other state variables
    var isAutostartConfirmed by remember {
        mutableStateOf(AutostartPrefs.isConfirmed(context))
    }

    var permanentlyDenied by remember { mutableStateOf(false) }
    val topPadding = rememberNotchHeightDp()

    var showAutostartDialog by remember { mutableStateOf(false) }
    var waitingForAutostartResult by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {

                val granted = hasLocationPermission(context)
                hasLocPermission = granted
                if (granted) permanentlyDenied = false

                if (waitingForAutostartResult) {
                    waitingForAutostartResult = false
                    // ✅ Trust the user — they went to settings, assume they enabled it
                    AutostartPrefs.onUserConfirmed(context)
                    isAutostartConfirmed = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocPermission = granted
        permanentlyDenied = !granted &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.ACCESS_FINE_LOCATION
                )
    }


    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> Utils.Companion.notification.value = granted }


    val notificationLaunch = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val userDetail by userDBVM.userDetail.collectAsStateWithLifecycle()

    // ✅ Show dialog before Scaffold
    if (showAutostartDialog)
    {
        AutostartDialog(
            context = context,
            onOpenSettings = {
                showAutostartDialog = false
                waitingForAutostartResult = true
            },
            onDismiss = {
                showAutostartDialog = false
                AutostartPrefs.onUserDismissed(context)
            }
        )
    }

    LaunchedEffect(hasLocPermission, Utils.Companion.notification.value, isAutostartConfirmed) {

        if (hasLocPermission && Utils.Companion.notification.value && isAutostartConfirmed) {

            val pm = context.getSystemService(PowerManager::class.java)
            if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                context.startActivity(
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                )
            }

            val serviceIntent = Intent(context, LocationForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    LaunchedEffect(Unit) {
        dbVM.GetUserData(Utils.user_id)
        userDBVM.getUserData(Utils.user_id)
    }

    val userData by dbVM.userData.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(blueApp)
            .padding(top = topPadding.value)
    )
    {
        Scaffold(
            topBar = {
                TopAppBar(dbVM , hasLocPermission)
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(top = paddingValues.calculateTopPadding())
                        .fillMaxSize()
                        .background(Color.White)
                )
                {
                    when {
                        // Step 1: Notification first
                        !Utils.notification.value -> {
                            EnableNotificationUI(context, notificationLaunch)
                        }

                        // Step 2: Location second
                        !hasLocPermission -> {
                            EnableLocationUI(context, launcher, permanentlyDenied)
                        }

                        // Step 3: Autostart third (only on devices that need it)
                        requiresAutostartPermission() && !isAutostartConfirmed -> {
                            EnableAutostartUI(
                                context = context,
                                onOpenSettings = {
                                    waitingForAutostartResult = true
                                    openAutostartSettings(context)
                                }
                            )
                        }

                        else -> {
                            if (dbVM.screenChange.value)
                            {
                                MapScreen(context ,dbVM)
                            }
                            else
                            {
                                val list1 = listOf("Name", "Phone Number")
                                val list2 = listOf(userDetail?.userName, userDetail?.userMobile)

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(15.dp),
                                    horizontalAlignment = Alignment.Start
                                )
                                {
                                    list1.zip(list2) { item1, item2 ->
                                        Row(modifier = Modifier.fillMaxWidth())
                                        {
                                            CommonText(
                                                text = item1,
                                                fontSize = 14,
                                                color = inActiveTextColor,
                                                textAlign = TextAlign.Start,
                                                modifier = Modifier.weight(0.5f)
                                            )
                                            CommonText(
                                                text = ":",
                                                color = Color.Black,
                                                fontSize = 14,
                                                modifier = Modifier.weight(0.1f)
                                            )
                                            CommonText(
                                                text = item2 ?: "",
                                                fontSize = 16,
                                                color = Color.Black,
                                                textAlign = TextAlign.Start,
                                                modifier = Modifier.weight(0.5f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                }

                                UserDetail_Content(context, userData , dbVM)
                            }
                        }
                    }
                }
            }
        )

        // 🔥 Snackbar overlay at bottom
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)   // spacing from bottom, optional
        )

        // ✅ Pass context to LogOutAlert so it can stop the service too
        LogOutAlert(dbVM.logOutAlert, context , navHostController)
    }


    BackHandler {
        if(dbVM.screenChange.value)
            dbVM.screenChange.value = false
        else
            if (backPressedOnce) {
                Utils.Companion.activity.finishAffinity()
                Utils.Companion.activity.finish()
            } else {
                backPressedOnce = true
                // Show message on first back
                scope.launch {
                    snackbarHostState.showSnackbar("Swipe again to exit")
                }
            }
    }

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(dbVM: UserDetailVM, hasLocPermission: Boolean)
{
    CenterAlignedTopAppBar(
        title = {
            CommonText(
                "KT Tracker",
                fontSize = 16,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .shadow(2.dp, CircleShape)
                    .wrapContentSize()
                    .noRippleClickable { dbVM.logOutAlert.value = true }
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logout), "",
                    modifier = Modifier
                        .padding(5.dp)
                        .size(25.dp)
                )
            }
        },
        actions = {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .shadow(2.dp, CircleShape)
                    .wrapContentSize()
                    .noRippleClickable {
                        if (Utils.Companion.notification.value) {
                            if (hasLocPermission) {
                                dbVM.screenChange.value = !dbVM.screenChange.value
                            } else
                                Utils.Companion.ToastMessage("Location permission is required")
                        } else
                            Utils.Companion.ToastMessage("Notification permission is required")
                    }
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        if(dbVM.screenChange.value)
                            R.drawable.list
                        else
                            R.drawable.streetmap), "",
                    modifier = Modifier
                        .padding(5.dp)
                        .size(25.dp)
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = blueApp
        ),
        modifier = Modifier.fillMaxWidth()
    )
}


@Composable
fun UserDetail_Content(context: Context, userData: List<UserDetailTable>, dbVM: UserDetailVM)
{
    var loader by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1500)
        loader = true
    }


    if(!loader)
        CommonLoader()
    else
    {
        if(userData.isNullOrEmpty())
        {
            CommonText(
                "NoData"
            )
        }
        else
        {
            Column(modifier = Modifier.fillMaxSize())
            {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0))
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeaderText("S.No", Modifier.weight(0.7f))
                    HeaderText("Location (Lat, Long)", Modifier.weight(2f))
                    HeaderText("Date", Modifier.weight(1.5f))
                    HeaderText("Time", Modifier.weight(1.2f))
                    HeaderText("Share", Modifier.weight(0.8f))
//                HeaderText("Map", Modifier.weight(0.8f))
                }

                Divider()

                LazyColumn(modifier = Modifier.fillMaxSize())
                {

                    itemsIndexed(userData) { index, item ->

                        val dateTime = formatTimestamp(item.user_time)
                        val date = dateTime.first
                        val time = dateTime.second

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .noRippleClickable {
                                    val dateTime = formatTimestamp(item.user_time)

                                    dbVM.selectedMapLocation.value =
                                        MapSelection(
                                            item.user_latitude.toDouble(),
                                            item.user_longitude.toDouble(),
                                            dateTime.first,
                                            dateTime.second
                                        )

                                    dbVM.screenChange.value = true
                                }
                                .padding(vertical = 10.dp, horizontal = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            CommonText(
                                text = "${index + 1}",
                                modifier = Modifier.weight(0.7f)
                            )

                            CommonText(
                                text = "${item.user_latitude}, ${item.user_longitude}",
                                modifier = Modifier.weight(2f)
                            )

                            CommonText(
                                text = date,
                                modifier = Modifier.weight(1.5f)
                            )

                            CommonText(
                                text = time,
                                modifier = Modifier.weight(1.2f)
                            )

                            // Share icon
                            Image(
                                painter = painterResource(R.drawable.share),
                                contentDescription = "Share",
                                modifier = Modifier
                                    .weight(0.8f)
                                    .size(20.dp)
                                    .noRippleClickable {
                                        shareLocation(
                                            context,
                                            item.user_latitude.toDouble(),
                                            item.user_longitude.toDouble(),
                                            dateTime
                                        )
                                    }
                            )

//                        // Map icon
//                        Image(
//                            painter = painterResource(R.drawable.location),
//                            contentDescription = "Open Map",
//                            modifier = Modifier
//                                .weight(0.8f)
//                                .size(20.dp)
//                                .noRippleClickable() {
//                                    openInGoogleMaps(
//                                        context,
//                                        item.user_latitude.toDouble(),
//                                        item.user_longitude.toDouble()
//                                    )
//                                }
//                        )
                        }

                        Divider()
                    }
                }
            }
        }
    }
}


@Composable
fun HeaderText(text: String, modifier: Modifier) {
    CommonText(
        text = text,
        modifier = modifier,
        fontWeight = FontWeight.Bold,
        fontSize = 16,
        color = Color.Black
    )
}


fun startLocationTrackingIfNeeded(context: Context) {
    val workManager = WorkManager.getInstance(context)

    val existingWork = workManager.getWorkInfosForUniqueWork("location_tracking").get()
    val isAlreadyScheduled = existingWork?.any {
        it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
    } ?: false

    if (isAlreadyScheduled) {
        Log.d("WorkManager", "✅ Worker already running — skipping enqueue")
        return // ✅ Don't reset the 15-min clock!
    }

    val request = PeriodicWorkRequestBuilder<LocationWorker>(15, TimeUnit.MINUTES)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
        )
        .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
        .build()

    workManager.enqueueUniquePeriodicWork(
        "location_tracking",
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
    Log.d("WorkManager", "✅ LocationWorker freshly enqueued")
}


@Composable
fun AutostartDialog(
    context: Context,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    // ✅ Dynamic message based on device brand
    val deviceName = when (detectRomType()) {
        RomType.MIUI -> "Xiaomi/MIUI"
        RomType.OPPO -> "OPPO/Realme/OnePlus"
        RomType.VIVO -> "Vivo"
        RomType.HUAWEI -> "Huawei"
        else -> "this device"
    }

    AlertDialog(
        onDismissRequest = {
            AutostartPrefs.onUserDismissed(context)
            onDismiss()
        },
        title = { Text("Enable Autostart") },
        text = {
            Column {
                Text(
                    "Without Autostart, location tracking stops when the app is closed.\n\n" +
                            "Go to: Settings → Apps → Manage Apps → ${context.packageName} → Autostart → Enable"
                )
                Spacer(modifier = Modifier.height(8.dp))
                CommonText(
                    // ✅ No longer hardcoded to "Xiaomi"
                    "⚠️ Required for background tracking on $deviceName.",
                    color = Color.Red,
                    fontSize = 12
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onOpenSettings()
                openAutostartSettings(context)
            }) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                AutostartPrefs.onUserDismissed(context)
                onDismiss()
            }) {
                Text("Later")
            }
        }
    )
}



@Composable
fun EnableLocationUI(
    context: Context,
    launcher: ManagedActivityResultLauncher<String, Boolean>,
    permanentlyDenied: Boolean )
{
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {

        LottieAnimation_Json(
            animation = R.raw.location,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        CommonText(
            "Location permission is required",
            fontSize = 16,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(25.dp))

        Button(
            onClick = {
                if (permanentlyDenied)
                {
                    // Open app settings
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    ).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
                else
                {
                    launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        ) {
            Text(
                if (permanentlyDenied) "Open Settings"
                else "Enable Location Permission"
            )
        }
    }
}



@Composable
fun EnableNotificationUI(
    context: Context,
    launcher: ManagedActivityResultLauncher<String, Boolean>
)
{
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {

        LottieAnimation_Json(
            animation = R.raw.location,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        CommonText(
            "Notification permission is required",
            fontSize = 16,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(25.dp))

        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    when {
                        ContextCompat.checkSelfPermission(Utils.Companion.activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                            Utils.Companion.sharedHelper.putBoolean(
                                Utils.Companion.activity,
                                Utils.Companion.notiEnable_share,false)
                            Utils.Companion.notification.value = Utils.Companion.sharedHelper.getBoolean(
                                Utils.Companion.activity , Utils.Companion.notiEnable_share)
                        }
                        Utils.Companion.activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            ).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                        else -> {
                            //Permission  fesDeGrand
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }
                }
                else
                {
                    Utils.Companion.sharedHelper.putBoolean(
                        Utils.Companion.activity,
                        Utils.Companion.notiEnable_share,true)
                    Utils.Companion.notification.value = Utils.Companion.sharedHelper.getBoolean(
                        Utils.Companion.activity , Utils.Companion.notiEnable_share)
                }
            }
        )
        {
            Text(
                "Enable Notification"
            )
        }
    }
}



@Composable
fun EnableAutostartUI(
    context: Context,
    onOpenSettings: () -> Unit
) {
    val deviceName = when (detectRomType()) {
        RomType.MIUI -> "Xiaomi/MIUI"
        RomType.OPPO -> "OPPO/Realme/OnePlus"
        RomType.VIVO -> "Vivo"
        RomType.HUAWEI -> "Huawei"
        else -> "this device"
    }

    // ✅ Device-specific instructions
    val instructions = when (detectRomType()) {
        RomType.MIUI -> "Settings → Apps → Manage Apps → KT Tracker → Autostart → Enable"
        RomType.OPPO -> "Phone Manager → Privacy Permissions → Startup Manager → KT Tracker → Enable\n\nor\n\nSettings → Battery → App Management → KT Tracker → Allow Autostart"
        RomType.VIVO -> "iManager → App Manager → Autostart Manager → KT Tracker → Enable"
        RomType.HUAWEI -> "Phone Manager → App Launch → KT Tracker → Manage Manually → Enable All"
        else -> "App Settings → Battery → Allow Background Activity"
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation_Json(
            animation = R.raw.location,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        CommonText(
            "Autostart Permission Required",
            fontSize = 16,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        CommonText(
            "Required to keep location tracking running after reboot or when app is closed on $deviceName.",
            fontSize = 13,
            color = inActiveTextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Step-by-step instructions box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF0F4FF))
                .padding(16.dp)
        ) {
            Column {
                CommonText(
                    "How to enable:",
                    fontSize = 13,
                    color = blueApp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                CommonText(
                    instructions,
                    fontSize = 13,
                    color = Color.Black,
                    textAlign = TextAlign.Start
                )
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        Button(onClick = onOpenSettings) {
            Text("Open Settings")
        }
    }
}



@Composable
fun LogOutAlert(
    showDialog: MutableState<Boolean>,
    context: Context,
    navHostController: NavHostController
)
{
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Image(
                        painter = painterResource(R.drawable.logout), "",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    CommonText(
                        text = "Logout",
                        fontSize = 16,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.scaledSp,
                        color = blueApp
                    )
                }
            },
            text = {
                CommonText(
                    text = "Are you sure you want to logout?",
                    fontSize = 14,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.scaledSp,
                    color = Color.Black
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .fillMaxWidth()
                            .background(lightBlueApp)
                            .noRippleClickable { showDialog.value = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CommonText(text = "No", fontSize = 14, color = Color.Black)
                    }

                    Spacer(modifier = Modifier.width(15.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .fillMaxWidth()
                            .background(blueApp)
                            .noRippleClickable {
                                // ✅ Stop WorkManager
                                WorkManager.getInstance(context)
                                    .cancelUniqueWork("location_tracking")

                                // ✅ Stop ForegroundService too (was missing before!)
                                context.stopService(
                                    Intent(context, LocationForegroundService::class.java)
                                )

                                Log.d("WorkManager", "🛑 LocationWorker & ForegroundService stopped")
                                showDialog.value = false
                                Utils.Companion.sharedHelper.putBoolean(context, Utils.Companion.login_Completed, false)
                                navHostController.navigate(NavScreens.SignUp_Screen.route)
                                Utils.Companion.ToastMessage("Logged Out Successfully")
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CommonText(text = "Yes", fontSize = 14, color = Color.White)
                    }
                }
            },
            shape = RoundedCornerShape(15.dp),
            containerColor = Color.White,
            tonalElevation = 6.dp
        )
    }
}