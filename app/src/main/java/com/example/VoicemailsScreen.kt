package com.example

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class VoicemailMessage(val id: String, val senderUdcin: String, val audioUrl: String, val timestamp: Long)

@Composable
fun VoicemailsScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("voip_prefs", Context.MODE_PRIVATE)
    val myUdcin = prefs.getString("my_udcin", "2048") ?: "2048"
    
    var voicemails by remember { mutableStateOf<List<VoicemailMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val db = FirebaseFirestore.getInstance()
    val dateFormat = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }

    LaunchedEffect(myUdcin) {
        try {
            db.collection("users").document(myUdcin).collection("voicemails")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener
                    if (snapshot != null) {
                        voicemails = snapshot.documents.mapNotNull { doc ->
                            val sender = doc.getString("senderUdcin") ?: return@mapNotNull null
                            val url = doc.getString("audioUrl") ?: return@mapNotNull null
                            val time = doc.getLong("timestamp") ?: 0L
                            VoicemailMessage(doc.id, sender, url, time)
                        }
                        isLoading = false
                    }
                }
        } catch (e: Exception) {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    if (voicemails.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No voicemails")
        }
    } else {
        LazyColumn(Modifier.fillMaxSize()) {
            items(voicemails) { vm ->
                ListItem(
                    headlineContent = { Text("From: ${vm.senderUdcin}") },
                    supportingContent = { Text(dateFormat.format(Date(vm.timestamp))) },
                    leadingContent = { Icon(Icons.Filled.Voicemail, contentDescription = null) },
                    trailingContent = {
                        IconButton(onClick = { /* Play Audio from vm.audioUrl */ }) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
                        }
                    }
                )
            }
        }
    }
}
