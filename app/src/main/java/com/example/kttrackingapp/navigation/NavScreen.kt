package com.example.kttrackingapp.navigation

sealed class NavScreens ( val route : String )
{
    object SplashScreen : NavScreens("SplashScreen")
    object SignUp_Screen : NavScreens("SignUp_Screen")
    object HomeScreen : NavScreens("HomeScreen")
}