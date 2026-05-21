package com.example.securelock.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.securelock.ui.admin.AdminNewUserScreen
import com.example.securelock.ui.admin.AdminVehicleScreen

// Oggetto (container di costanti)
// Routes definite => parentesi graffe per passaggio dati
object Routes {
    const val HOME = "home"
    const val FACE_AUTH = "face_auth"
    const val LOGIN = "login"
    const val CREDITS = "credits"
    const val CREATE_USER = "admin_new_user/{adminId}"
    const val WELCOME = "welcome/{userId}/{isAdmin}"
    const val SETUP = "setup/{superAdminId}"
    const val SLOT_DETAIL = "slot_detail/{userId}/{deviceId}/{slotId}"
    const val ADMIN_VEHICLES = "admin_vehicles/{userId}"
}

// UI di compose (prima creata)
@Composable
fun AppNavigation() {
    // Crea NavController per stack delle schermate
    // Permette di navigare tra schermate
    val navController = rememberNavController()
    val startDestination = Routes.HOME

    // Container che collega il navController con le schermate composable
    // Si passa navController ad ogni pagina per i redirect
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

        // Passaggio dati tramite route
        composable(Routes.SETUP) { backStackEntry ->
            // backStackEntry contiene dati e stati (cerca ID superadmin)
            val superAdminId = backStackEntry.arguments
                ?.getString("superAdminId")
                ?.toIntOrNull() ?: 0

            // Apri la schermata e passa l'ID con una callback (event)
            // redirect a loginScreen
            SetupScreen(
                superAdminId = superAdminId,
                onSetupCompleted = {
                    // Apri homescreen ed elimina dati dal backstack
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        // Passaggio dati tramite route => adminID
        composable(Routes.CREATE_USER) { backStackEntry ->
            val adminId = backStackEntry.arguments
                ?.getString("adminId")
                ?.toIntOrNull() ?: -1

            // Schermata per la gestione utenti
            AdminNewUserScreen(
                navController = navController,
                userId = adminId
            )
        }

        // Dati route:
        // * userID
        // * isAdmin
        composable(Routes.WELCOME) { backStackEntry ->
            val userId = backStackEntry.arguments
                ?.getString("userId")
                ?.toIntOrNull() ?: 0

            val isAdmin = backStackEntry.arguments
                ?.getString("isAdmin")
                ?.toBoolean() ?: false

            // Schermata principale app
            WelcomeScreen(
                userId = userId,
                isAdmin = isAdmin,
                navController = navController
            )
        }

        // Dati route:
        // * userID
        // * deviceID => id univoco dell'ESP32
        // * slotID => id del drawer da aprire
        composable(Routes.SLOT_DETAIL) { backStackEntry ->
            val userId = backStackEntry.arguments
                ?.getString("userId")
                ?.toIntOrNull() ?: 0

            val deviceId = backStackEntry.arguments
                ?.getString("deviceId")
                ?.toIntOrNull() ?: 0

            val slotId = backStackEntry.arguments
                ?.getString("slotId")
                ?.toIntOrNull() ?: 0

            // Schermata con informazioni drawer e aggiornamenti stati
            SlotDetailScreen(
                userId = userId,
                deviceId = deviceId,
                slotId = slotId,
                navController = navController
            )
        }

        // Dati route: userID
        composable(Routes.ADMIN_VEHICLES) { backStackEntry ->
            val userId = backStackEntry.arguments
                ?.getString("userId")
                ?.toIntOrNull() ?: 0

            // Schermata gestione veicoli
            AdminVehicleScreen(
                navController = navController,
                userId = userId
            )
        }
    }
}