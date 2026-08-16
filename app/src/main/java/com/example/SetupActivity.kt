package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class SetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SetupScreen(onComplete = {
                        startActivity(Intent(this, DialerActivity::class.java))
                        finish()
                    })
                }
            }
        }
    }
}

@Composable
fun SetupScreen(onComplete: () -> Unit) {
    var tapCount by remember { mutableStateOf(0) }
    var showPinDialog by remember { mutableStateOf(false) }

    LaunchedEffect(tapCount) {
        if (tapCount > 0 && tapCount < 15) {
            delay(2000)
            tapCount = 0
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Enter Master/Dev PIN") },
            text = { OutlinedTextField(value = "", onValueChange = {}) },
            confirmButton = { Button(onClick = { showPinDialog = false }) { Text("Unlock") } }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "App Logo",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.clickable {
                tapCount++
                if (tapCount >= 15) {
                    showPinDialog = true
                    tapCount = 0
                }
            }
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { 
            // Generate UDCIN, Register Face, Complete Setup
            onComplete() 
        }) {
            Text("Generate Standard UDCIN")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { 
            // Open Account Transfer Activity
        }) {
            Text("Transfer Existing Account")
        }
    }
}
