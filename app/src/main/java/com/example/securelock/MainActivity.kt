package com.example.securelock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.securelock.ui.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContent sostituisce setContentView — tutto Compose da qui
        setContent {
            MaterialTheme {
                Surface {
                    // AppNavigation gestisce tutte le schermate
                    AppNavigation()
                }
            }
        }
    }
}