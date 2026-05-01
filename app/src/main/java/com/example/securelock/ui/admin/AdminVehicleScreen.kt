package com.example.securelock.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.securelock.R
import com.example.securelock.ui.admin.components.AssignVehicleToSlotSection
import com.example.securelock.ui.admin.components.CreateVehicleSection
import com.example.securelock.ui.admin.components.DeleteVehicleSection
import com.example.securelock.ui.admin.components.VehicleActionSelector
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVehicleScreen(
    navController: NavController,
    userId: Int
) {
    var selectedAction by remember { mutableStateOf(VehicleAction.CREATE) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    suspend fun showMessage(msg: String) {
        snackbarHostState.showSnackbar(msg)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.shadows_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x55FFFFFF))
        )

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Gestione veicoli",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF16324F)
                )

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0x66FFFFFF))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        VehicleActionSelector(
                            selectedAction = selectedAction,
                            onActionSelected = { selectedAction = it }
                        )

                        when (selectedAction) {
                            VehicleAction.CREATE -> {
                                CreateVehicleSection(
                                    adminUserId = userId,
                                    onVehicleCreated = {
                                        scope.launch { showMessage("Veicolo creato") }
                                    },
                                    onMessage = { msg ->
                                        scope.launch { showMessage(msg) }
                                    }
                                )
                            }

                            VehicleAction.DELETE -> {
                                DeleteVehicleSection(
                                    adminUserId = userId,
                                    onVehicleDeleted = {
                                        scope.launch { showMessage("Veicolo eliminato") }
                                    },
                                    onMessage = { msg ->
                                        scope.launch { showMessage(msg) }
                                    }
                                )
                            }

                            VehicleAction.ASSIGN -> {
                                AssignVehicleToSlotSection(
                                    adminUserId = userId,
                                    onMessage = { msg ->
                                        scope.launch { showMessage(msg) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}