package com.example.securelock.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.securelock.ui.admin.AdminNewUserScreen

object Routes {
    const val HOME = "home"
    const val FACE_AUTH = "face_auth"
    const val LOGIN = "login"
    const val CREDITS = "credits"
    const val SETUP = "setup/{superAdminId}"
    const val SLOT_DETAIL = "slot_detail/{userId}/{deviceId}/{slotId}"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val startDestination = Routes.HOME

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(navController = navController)
        }

        composable(Routes.FACE_AUTH) {
            FaceAuthScreen(navController = navController)
        }

        composable(Routes.HOME) {
            HomeScreen(navController = navController)
        }

        composable(Routes.CREDITS) {
            CreditsScreen(navController = navController)
        }

        composable("setup/{superAdminId}") { backStackEntry ->
            val superAdminId = backStackEntry.arguments
                ?.getString("superAdminId")
                ?.toIntOrNull() ?: 0

            SetupScreen(
                superAdminId = superAdminId,
                onSetupCompleted = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
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

        composable("slot_detail/{userId}/{deviceId}/{slotId}") { backStackEntry ->
            val userId = backStackEntry.arguments
                ?.getString("userId")
                ?.toIntOrNull() ?: 0

            val deviceId = backStackEntry.arguments
                ?.getString("deviceId")
                ?.toIntOrNull() ?: 0

            val slotId = backStackEntry.arguments
                ?.getString("slotId")
                ?.toIntOrNull() ?: 0

            SlotDetailScreen(
                userId = userId,
                deviceId = deviceId,
                slotId = slotId,
                navController = navController
            )
        }
    }
}