package com.example.campusia

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.campusia.ui.theme.CampusiaTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    private var pendingRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        pendingRoute = extractTargetRoute(intent)

        enableEdgeToEdge()

        setContent {
            CampusiaTheme {
                Navigation(
                    auth = auth,
                    initialRoute = pendingRoute,
                    onInitialRouteConsumed = {
                        pendingRoute = null
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingRoute = extractTargetRoute(intent)
    }

    private fun extractTargetRoute(intent: Intent?): String? {
        return intent
            ?.getStringExtra("targetRoute")
            ?.takeIf { it.isNotBlank() }
    }
}