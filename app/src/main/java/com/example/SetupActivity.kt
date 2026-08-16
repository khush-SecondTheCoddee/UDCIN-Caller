package com.example

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class SetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("voip_prefs", Context.MODE_PRIVATE)
        if (prefs.contains("my_udcin")) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SetupScreen(
                        onComplete = { udcin ->
                            prefs.edit().putString("my_udcin", udcin).apply()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        },
                        onTransfer = {
                            startActivity(Intent(this, AccountTransferActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SetupScreen(onComplete: (String) -> Unit, onTransfer: () -> Unit) {
    val context = LocalContext.current
    var tapCount by remember { mutableStateOf(0) }
    var showPinDialog by remember { mutableStateOf(false) }
    
    var showCustomUdcinDialog by remember { mutableStateOf(false) }
    var customUdcin by remember { mutableStateOf("") }
    var accessTier by remember { mutableStateOf(0) }
    var pinInput by remember { mutableStateOf("") }

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
            text = { OutlinedTextField(value = pinInput, onValueChange = { pinInput = it }) },
            confirmButton = { 
                Button(onClick = { 
                    if (pinInput == "DEV123") {
                        accessTier = 1
                        showPinDialog = false
                        showCustomUdcinDialog = true
                    } else if (pinInput == "MASTER456") {
                        accessTier = 2
                        showPinDialog = false
                        showCustomUdcinDialog = true
                    }
                }) { Text("Unlock") } 
            }
        )
    }

    if (showCustomUdcinDialog) {
        AlertDialog(
            onDismissRequest = { showCustomUdcinDialog = false },
            title = { Text("Enter Custom UDCIN") },
            text = { OutlinedTextField(value = customUdcin, onValueChange = { customUdcin = it }) },
            confirmButton = { 
                Button(onClick = { 
                    var isValid = false
                    if (accessTier == 1) {
                        isValid = customUdcin.startsWith("2") && customUdcin.length in listOf(3, 4, 5, 6, 10)
                    } else if (accessTier == 2) {
                        isValid = customUdcin.length in 1..10 && customUdcin.firstOrNull()?.isDigit() == true && customUdcin.first() != '0'
                    }
                    if (isValid) {
                        onComplete(customUdcin) 
                    }
                }) { Text("Claim") } 
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "VoIP App",
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
            val udcin = UdcinGenerator.generateBaseUdcin(context)
            onComplete(udcin)
        }) {
            Text("Generate Standard UDCIN")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { 
            onTransfer()
        }) {
            Text("Transfer Existing Account")
        }
    }
}
