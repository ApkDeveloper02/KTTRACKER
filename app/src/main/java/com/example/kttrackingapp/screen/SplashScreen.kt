package com.example.kttrackingapp.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.kttrackingapp.CommonText
import com.example.kttrackingapp.R
import com.example.kttrackingapp.Utils
import com.example.kttrackingapp.navigation.NavScreens
import com.example.kttrackingapp.ui.theme.blueApp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navHostController: NavHostController)
{
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 3000
        )
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(3000)

        if(Utils.Companion.sharedHelper.getBoolean(context , Utils.Companion.login_Completed))
            navHostController.navigate(NavScreens.HomeScreen.route)
        else
            navHostController.navigate(NavScreens.SignUp_Screen.route)
    }

    Splash(alpha = alphaAnim.value)
}


@Composable
fun Splash(alpha: Float)
{
    Box(modifier = Modifier.fillMaxSize()
        .background(Color.White))
    {
        Image(painter = painterResource(R.drawable.logo) ,
            contentDescription = "splashIcon",
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(alpha = alpha)
                .size(150.dp))

        CommonText(
            text = stringResource(R.string.app_name),
            fontSize = 18,
            color = blueApp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(bottom = 50.dp)
                .align(Alignment.BottomCenter)
        )
    }
}