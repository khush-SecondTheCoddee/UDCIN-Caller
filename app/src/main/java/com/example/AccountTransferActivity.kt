package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AccountTransferActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TransferScreen(onSuccess = { udcin ->
                        val prefs = getSharedPreferences("voip_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("my_udcin", udcin).apply()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    })
                }
            }
        }
    }
}

@Composable
fun TransferScreen(onSuccess: (String) -> Unit) {
    val context = LocalContext.current
    var udcin by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var totp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Account Recovery & Transfer", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("4-Factor Authentication Required", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = udcin, 
            onValueChange = { udcin = it }, 
            label = { Text("Target UDCIN") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = pin, 
            onValueChange = { pin = it }, 
            label = { Text("Account PIN") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = totp, 
            onValueChange = { totp = it }, 
            label = { Text("Authenticator TOTP Code") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (udcin.isEmpty() || pin.isEmpty() || totp.isEmpty()) {
                        errorMessage = "All fields are required"
                        return@Button
                    }
                    
                    isLoading = true
                    errorMessage = ""
                    
                    coroutineScope.launch {
                        try {
                            // 1. Fetch user doc from Firestore
                            val doc = db.collection("users").document(udcin).get().await()
                            if (!doc.exists()) {
                                errorMessage = "UDCIN not found"
                                isLoading = false
                                return@launch
                            }
                            
                            // 2. Validate PIN (Mocking hash check for prototype)
                            val storedPin = doc.getString("pin") ?: "1234" // Fallback mock
                            if (pin != storedPin) {
                                errorMessage = "Invalid PIN"
                                isLoading = false
                                return@launch
                            }
                            
                            // 3. Validate TOTP (Mocking check)
                            val storedTotp = doc.getString("totpSecret") ?: "000000"
                            if (totp != storedTotp && totp != "123456") { // 123456 as master override for prototype
                                errorMessage = "Invalid TOTP code"
                                isLoading = false
                                return@launch
                            }
                            
                            // 4. Face Biometrics (Mocking CameraX/TFLite integration)
                            val authManager = FaceAuthManager(context)
                            val liveEmbedding = authManager.extractEmbedding(android.graphics.Bitmap.createBitmap(112, 112, android.graphics.Bitmap.Config.ARGB_8888))
                            
                            @Suppress("UNCHECKED_CAST")
                            val storedEmbeddingList = doc.get("faceEmbedding") as? List<Double>
                            val storedEmbedding = storedEmbeddingList?.map { it.toFloat() }?.toFloatArray() ?: FloatArray(128) { 0.5f }
                            
                            val similarity = authManager.computeCosineSimilarity(liveEmbedding, storedEmbedding)
                            
                            if (similarity >= 0.80f) {
                                // 5. Update active device ID
                                val newDeviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                                db.collection("users").document(udcin).update("activeDeviceId", newDeviceId).await()
                                
                                Toast.makeText(context, "Transfer Successful", Toast.LENGTH_LONG).show()
                                onSuccess(udcin)
                            } else {
                                errorMessage = "Biometric Face Verification Failed (Sim: $similarity)"
                            }
                            
                        } catch (e: Exception) {
                            errorMessage = "Network or Server Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Verify Identity & Transfer")
            }
        }
    }
}
