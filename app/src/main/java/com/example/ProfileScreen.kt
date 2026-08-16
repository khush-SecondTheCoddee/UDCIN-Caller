package com.example

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("voip_prefs", Context.MODE_PRIVATE)
    val myUdcin = prefs.getString("my_udcin", "2048") ?: "2048"
    
    var displayName by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(myUdcin) {
        try {
            val doc = db.collection("users").document(myUdcin).get().await()
            if (doc.exists()) {
                displayName = doc.getString("displayName") ?: ""
                photoUrl = doc.getString("photoUrl") ?: ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("My Profile (UDCIN: $myUdcin)", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display Name") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = photoUrl,
            onValueChange = { photoUrl = it },
            label = { Text("Profile Photo URL") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            coroutineScope.launch {
                try {
                    db.collection("users").document(myUdcin).update(
                        mapOf(
                            "displayName" to displayName,
                            "photoUrl" to photoUrl
                        )
                    ).await()
                } catch (e: Exception) {
                    // Handle failure, or create if not exists
                    db.collection("users").document(myUdcin).set(
                        mapOf(
                            "displayName" to displayName,
                            "photoUrl" to photoUrl
                        )
                    ).await()
                }
            }
        }) {
            Text("Save Profile")
        }
    }
}
