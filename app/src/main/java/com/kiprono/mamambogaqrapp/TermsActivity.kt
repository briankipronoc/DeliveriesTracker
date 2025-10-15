package com.kiprono.mamambogaqrapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiprono.mamambogaqrapp.ui.theme.AppThemeState
// REMOVED: import com.kiprono.mamambogaqrapp.ui.theme.AppTheme // Unused/Conflicting
import com.kiprono.mamambogaqrapp.ui.theme.MamaMbogaQRAppTheme

class TermsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // FIX: Correctly apply MamaMbogaQRAppTheme and pass the darkTheme state as an argument.
            MamaMbogaQRAppTheme(darkTheme = AppThemeState.isDark.value) {
                TermsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Conditions", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Welcome to the Mama Mboga QR App!",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                """
                By using this app, you agree to the following terms:
                
                • Use this app responsibly to record deliveries and track performance.  
                • Your data is stored locally unless explicitly shared.  
                • The app is meant for productivity, not resale.  
                • Updates or changes to the app may affect stored data.  
                • Contact support if you experience issues.
                
                Thank you for supporting your local Mama Mboga network! ❤️
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}