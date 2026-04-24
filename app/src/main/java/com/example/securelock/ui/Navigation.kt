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
    const val ADMIN_NEW_USER = "admin_new_user"
    const val CREDITS = "credits"
    const val WELCOME = "welcome/{userId}"
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

        composable(Routes.ADMIN_NEW_USER) {
            AdminNewUserScreen(navController = navController)
        }


        composable(Routes.CREDITS) {
            CreditsScreen(navController = navController)
        }

        composable("welcome/{userId}") { backStackEntry ->

            val userId = backStackEntry.arguments
                ?.getString("userId")
                ?.toIntOrNull() ?: 0

            WelcomeScreen(userId, navController = navController, isAdmin = false)
        }
    }
}