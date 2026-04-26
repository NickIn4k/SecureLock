package com.example.securelock.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.securelock.ui.admin.AdminNewUserScreen

// Routes
object Routes {
    const val HOME = "home"
    const val FACE_AUTH = "face_auth"
    const val LOGIN = "login"

    const val CREDITS = "credits"
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

        composable("admin_new_user/{adminId}") { backStackEntry ->

            val adminId = backStackEntry.arguments
                ?.getString("adminId")
                ?.toIntOrNull() ?: -1

            AdminNewUserScreen(
                navController = navController,
                userId = adminId
            )
        }

        composable(Routes.CREDITS) {
            CreditsScreen(navController = navController)
        }

        composable("welcome/{userId}/{isAdmin}") { backStackEntry ->
            val userId = backStackEntry.arguments
                ?.getString("userId")
                ?.toIntOrNull() ?: 0

            val isAdmin = backStackEntry.arguments
                ?.getString("isAdmin")
                ?.toBoolean() ?: false

            WelcomeScreen(
                userId = userId,
                isAdmin = isAdmin,
                navController = navController
            )
        }
    }
}