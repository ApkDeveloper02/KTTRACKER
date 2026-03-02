package com.example.kttrackingapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.kttrackingapp.screen.HomeScreen
import com.example.kttrackingapp.MainActivity
import com.example.kttrackingapp.screen.SignUp_Screen
import com.example.kttrackingapp.screen.SplashScreen

@Composable
fun NavigationBuild(
    navHostController: NavHostController,
    mainActivity: MainActivity
) {
    NavHost(
        navController = navHostController, startDestination = NavScreens.SplashScreen.route,
        )
    {
        composable(NavScreens.SplashScreen.route)
        {
            SplashScreen(navHostController)
        }

        composable(NavScreens.SignUp_Screen.route)
        {
            SignUp_Screen(navHostController)
        }

        composable(NavScreens.HomeScreen.route)
        {
            HomeScreen(navHostController)
        }
    }
}