package com.example.smartcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.navigation.compose.rememberNavController
import com.example.smartcard.navigation.AppNavGraph
import com.example.smartcard.navigation.Screen
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runCatching {
            val app = FirebaseApp.getInstance()
            val options = app.options
            QrFlowPhoneLog.d(
                event = "firebase_init",
                "projectId" to options.projectId,
                "applicationId" to packageName,
                "appName" to app.name,
                "collections" to "users,carts,products"
            )
        }.onFailure { t ->
            QrFlowPhoneLog.e(
                event = "exception",
                throwable = t,
                "where" to "firebase_init"
            )
        }

        val start = if (FirebaseAuth.getInstance().currentUser != null) {
            Screen.Home.route
        } else {
            Screen.Login.route
        }

        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                val navController = rememberNavController()
                AppNavGraph(navController = navController, startDestination = start)
            }
        }
    }
}