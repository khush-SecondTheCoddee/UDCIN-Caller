package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class AccountTransferActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("4-Factor Transfer", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Target UDCIN") })
                        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Account PIN") })
                        OutlinedTextField(value = "", onValueChange = {}, label = { Text("2FA TOTP") })
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { /* Start CameraX Face Auth */ }) {
                            Text("Verify Face Biometrics")
                        }
                    }
                }
            }
        }
    }
}
