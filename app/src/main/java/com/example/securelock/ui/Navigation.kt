package com.example.securelock.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Routes
object Routes {
    const val HOME = "home"
    const val FACE_AUTH = "face_auth"
    const val LOGIN = "login"
}

@Composable
fun AppNavigation() {
    // Controller di navigazione
    val navController = rememberNavController()

    // NavHost: contenitore della schermata corrente
    // startDestination: prima schermata
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        // Ogni composable() => una schermata
        composable(Routes.HOME) {
            HomeScreen(navController = navController)
        }
        composable(Routes.FACE_AUTH) {
            FaceAuthScreen(navController = navController)
        }
        composable(Routes.LOGIN) {
            LoginScreen(navController = navController)
        }
    }
}