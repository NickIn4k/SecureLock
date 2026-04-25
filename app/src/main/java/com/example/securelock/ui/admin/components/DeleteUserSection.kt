package com.example.securelock.ui.admin.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class AdminUserItem(
    val id: Int,
    val username: String
)

@Composable
fun DeleteUserSection(
    users: List<AdminUserItem> = listOf(
        AdminUserItem(1, "test1"),
        AdminUserItem(2, "test2"),
        AdminUserItem(3, "test3")
    ),
    onDeleteClick: (AdminUserItem) -> Unit = {}
) {
    var selectedUser by remember { mutableStateOf<AdminUserItem?>(null) }

    val cardShape = RoundedCornerShape(18.dp)
    val buttonShape = RoundedCornerShape(18.dp)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Elimina utente",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF16324F)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Seleziona un utente da rimuovere.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF5E6B7A)
        )

        Spacer(modifier = Modifier.height(16.dp))

        users.forEach { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                shape = cardShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF9FBFF)
                ),
                border = BorderStroke(
                    1.dp,
                    if (selectedUser?.id == user.id) {
                        Brush.linearGradient(
                            listOf(Color(0xFFB7A6FF), Color(0xFF8FD3FF))
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(Color(0xFFE0E6EF), Color(0xFFE0E6EF))
                        )
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedUser?.id == user.id,
                        onClick = { selectedUser = user }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E3A4B)
                        )
                        Text(
                            text = "ID: ${user.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7A8696)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = { selectedUser?.let { onDeleteClick(it) } },
            enabled = selectedUser != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = buttonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEA5C5C),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFF0C7C7),
                disabledContentColor = Color.White
            )
        ) {
            Text("Elimina utente")
        }
    }
}