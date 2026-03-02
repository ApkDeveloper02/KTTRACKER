package com.example.kttrackingapp.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.kttrackingapp.CommonText
import com.example.kttrackingapp.R
import com.example.kttrackingapp.Utils
import com.example.kttrackingapp.navigation.NavScreens
import com.example.kttrackingapp.noRippleClickable
import com.example.kttrackingapp.rememberNotchHeightDp
import com.example.kttrackingapp.roomDB.roomVM.AllUserVM
import com.example.kttrackingapp.scaledSp
import com.example.kttrackingapp.ui.theme.blueApp
import com.example.kttrackingapp.ui.theme.inActiveTextColor
import com.example.kttrackingapp.ui.theme.lightBlueApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignUp_Screen(
    navHostController: NavHostController ,
    dbViewModel : AllUserVM = koinViewModel()
)
{

    val focusManager = LocalFocusManager.current
    val topPadding = rememberNotchHeightDp()

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

    Box(modifier = Modifier.fillMaxSize()
        .noRippleClickable {
            focusManager.clearFocus()
        })
    {
        Image(painter = painterResource(R.drawable.loginbg) , "" ,
            modifier = Modifier.fillMaxSize())

        AnimatedContent(
            targetState = dbViewModel.screenOpt,
            transitionSpec = {
                (fadeIn(
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                ) + scaleIn(
                    initialScale = 0.92f, // small scale difference = smoother
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                )) togetherWith
                        (fadeOut(
                            animationSpec = tween(
                                durationMillis = 90, // faster exit prevents overlap lag
                                easing = LinearOutSlowInEasing
                            )
                        ) + scaleOut(
                            targetScale = 0.92f,
                            animationSpec = tween(
                                durationMillis = 90,
                                easing = LinearOutSlowInEasing
                            )
                        ))
            },
            label = "SmoothFadeScaleAnimation"
        )
        { state ->
            if(!state)
            {
                InitialScreen(modifier = Modifier.fillMaxSize()
                    .padding(top = topPadding.value) ,
                    onClick = { it ->
                        dbViewModel.screenOpt = true
                        dbViewModel.loginSignCheck = it
                    })
            }
            else
            {
                LoginSignUp_Content(
                    modifier = Modifier.fillMaxSize()
                        .padding(top = topPadding.value) ,
                    dbViewModel , navHostController
                )
            }
        }



        // 🔥 Snackbar overlay at bottom
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)   // spacing from bottom, optional
        )
    }

    BackHandler {
        if(dbViewModel.screenOpt)
            dbViewModel.screenOpt = false
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


@Composable
fun LoginSignUp_Content(modifier: Modifier, dbVM: AllUserVM, navHostController: NavHostController)
{
    AnimatedContent(
        targetState = dbVM.loginSignCheck,
        transitionSpec = {
            (fadeIn(
                animationSpec = tween(
                    durationMillis = 250,
                    easing = FastOutSlowInEasing
                )
            ) + scaleIn(
                initialScale = 0.92f, // small scale difference = smoother
                animationSpec = tween(
                    durationMillis = 250,
                    easing = FastOutSlowInEasing
                )
            )) togetherWith
                    (fadeOut(
                        animationSpec = tween(
                            durationMillis = 90, // faster exit prevents overlap lag
                            easing = LinearOutSlowInEasing
                        )
                    ) + scaleOut(
                        targetScale = 0.92f,
                        animationSpec = tween(
                            durationMillis = 90,
                            easing = LinearOutSlowInEasing
                        )
                    ))
        },
        label = "SmoothFadeScaleAnimation"
    )
    { screen ->

        Column(modifier = modifier,
            verticalArrangement = Arrangement.Center ,
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            CommonText(
                if (screen == 0) "Login Here" else "Create Account",
                fontSize = 22,
                color = blueApp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(15.dp))

            CommonText(
                if (screen == 0) "Welcome back you’ve\n" +
                        "been missed!" else "Create an account so you can explore\n all the paths you have covered",
                fontSize = 16,
                lineHeight = 24.scaledSp,
                color = Color.Black,
            )

            Spacer(modifier = Modifier.height(25.dp))


            Column(
                modifier = Modifier.wrapContentSize()
                    .padding(25.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                if (screen == 1) {
                    CommonTF(
                        value = dbVM.username,
                        onValueChange = { it ->

                            val filterText =
                                it.filter { it.isLetter() || it.isWhitespace() }

                            if (filterText.length <= 25) {
                                dbVM.username = filterText
                            } else
                                Utils.Companion.ToastMessage("Limit Exceeds")
                        },
                        placeholder = "User Name",
                        keyboardType = KeyboardType.Text
                    )

                    Spacer(modifier = Modifier.height(15.dp))
                }

                CommonTF(
                    value = dbVM.phoneNumber,
                    onValueChange = { it ->

                        val filterText = it.filter { it.isDigit() }

                        if (filterText.length <= 10) {
                            dbVM.phoneNumber = filterText
                        } else
                            Utils.Companion.ToastMessage("Limit Exceeds")
                    },
                    placeholder = "Phone Number",
                    keyboardType = KeyboardType.NumberPassword
                )

                Spacer(modifier = Modifier.height(15.dp))

                CommonTF(
                    value = dbVM.password,
                    onValueChange = { it ->
                        dbVM.password = it.filter { it.isLetterOrDigit() || it == '@' || it == '.'}
                    },
                    placeholder = "Password",
                    keyboardType = KeyboardType.Password,
                    password_eye = true
                )

                Spacer(modifier = Modifier.height(15.dp))

                if (screen == 1) {

                    CommonTF(
                        value = dbVM.confirmPassword,
                        onValueChange = { it ->
                            dbVM.confirmPassword = it
                        },
                        placeholder = "Confirm Password",
                        keyboardType = KeyboardType.Password,
                        password_eye = true
                    )

//                    Spacer(modifier = Modifier.height(15.dp))
                }


//                if (screen == 0) {
//                    Box(
//                        modifier = Modifier.fillMaxWidth(),
//                        contentAlignment = Alignment.CenterEnd
//                    )
//                    {
//                        CommonText(
//                            "Forgot Password ?",
//                            color = blueApp,
//                            fontSize = 16,
//                        )
//                    }
//                }

                Spacer(modifier = Modifier.height(20.dp))


                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .shadow(2.dp, RoundedCornerShape(12.dp))
                        .fillMaxWidth()
                        .background(blueApp)
                        .noRippleClickable {
                            println("CHECK---1")
                            dbVM.BtnCheck(screen , onSuccess = {
                                dbVM.ClearData()
                                navHostController.navigate(NavScreens.HomeScreen.route)
                            })
                        }
                        .padding(vertical = 15.dp),
                    contentAlignment = Alignment.Center
                )
                {
                    CommonText(
                        if (screen == 0) "Log In" else "Create Account",
                        color = Color.White,
                        fontSize = 16,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                )
                {
                    CommonText(
                        if (screen == 0)
                            "Don't have an account ?" else "You already have an account ?",
                        fontSize = 16,
                        color = Color.Black,
                    )

                    CommonText(
                        if (screen == 0) " Sign Up" else " Log In",
                        fontSize = 16,
                        color = blueApp,
                        modifier = Modifier.noRippleClickable {
                            dbVM.ClearData()
                            if (dbVM.loginSignCheck == 0)
                                dbVM.loginSignCheck = 1
                            else
                                dbVM.loginSignCheck = 0
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun InitialScreen( modifier : Modifier , onClick : (Int) -> Unit )
{
    Column(modifier = modifier)
    {
        Box(modifier = Modifier.weight(0.5f)
            .fillMaxSize()
            .padding(50.dp),
            contentAlignment = Alignment.Center)
        {
            Image(painter = painterResource(R.drawable.work_from_home) ,
                " ",
                modifier = Modifier
                    .fillMaxSize())
        }

        Column(modifier = Modifier.weight(0.5f)
            .fillMaxSize()
            .padding(25.dp),
            verticalArrangement = Arrangement.SpaceEvenly ,
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            CommonText(
                "Discover Your Path",
                fontSize = 22,
                color = blueApp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 15.dp)
            )


            CommonText(
                "Explore all the visited paths and track your day to day progress",
                fontSize = 16,
                color = Color.Black,
                lineHeight = 24.scaledSp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 15.dp)
            )


            Row(modifier = Modifier.fillMaxWidth() ,
                verticalAlignment = Alignment.CenterVertically)
            {
                repeat(2) { index ->
                    Box(modifier = Modifier.weight(1f)
                        .fillMaxWidth() ,
                        contentAlignment = Alignment.Center)
                    {
                        if(index == 0)
                        {
                            Box(modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .wrapContentHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .shadow(2.dp , RoundedCornerShape(12.dp))
                                .background(blueApp)
                                .noRippleClickable {
                                    onClick(0)
                                }
                                .padding(15.dp),
                                contentAlignment = Alignment.Center)
                            {
                                CommonText(
                                    "Login",
                                    fontSize = 16,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        else
                            CommonText(
                                "Register",
                                fontSize = 16,
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.noRippleClickable {
                                    onClick(1)
                                }
                            )
                    }
                }
            }
        }
    }
}


@Composable
fun CommonTF(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType ,
    password_eye : Boolean = false
)
{
    val focusManager = LocalFocusManager.current
    var eyecheck by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value ,
        onValueChange = onValueChange,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = blueApp ,
            unfocusedBorderColor = Color.Transparent ,
            focusedContainerColor = lightBlueApp ,
            unfocusedContainerColor = lightBlueApp
        ),
        placeholder = {
            CommonText(
                placeholder,
                fontSize = 14,
                color = inActiveTextColor
            )
        },
        maxLines = 1,
        singleLine = true,
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        trailingIcon = {
            if (password_eye) {
                Image(
                    painter = painterResource(id = if (eyecheck) R.drawable.eye_open else R.drawable.eye_close),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(inActiveTextColor),
                    modifier = Modifier
                        .size(20.dp)
                        .noRippleClickable(onClick = { eyecheck = !eyecheck })
                )
            }
        },
        visualTransformation = if (password_eye)
        {
            if (eyecheck) VisualTransformation.None else PasswordVisualTransformation(
                mask = '\u002A'
            )
        } else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType ,
            imeAction = ImeAction.Done ),
        modifier = Modifier
            .fillMaxWidth()
    )
}