package com.example.securelock.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.securelock.storage.SetupPrefs
import com.example.securelock.ui.admin.AdminNewUserScreen

object Routes {
    const val HOME = "home"
    const val FACE_AUTH = "face_auth"
    const val LOGIN = "login"
    const val CREDITS = "credits"
    const val SETUP = "setup"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val startDestination = if (SetupPrefs.isSetupCompleted(context)) {
        Routes.HOME
    } else {
        Routes.SETUP
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.SETUP) {
            SetupScreen(
                onSetupCompleted = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SETUP) { inclusive = true }
                    }
                }
            )
        }

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

        composable("slot_detail/{userId}/{slotId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            val slotId = backStackEntry.arguments?.getString("slotId")?.toIntOrNull() ?: 0

            SlotDetailScreen(
                userId = userId,
                slotId = slotId,
                navController = navController
            )
        }
    }
}