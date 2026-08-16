package com.example

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun DialerScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("voip_prefs", Context.MODE_PRIVATE)
    val myUdcin = prefs.getString("my_udcin", "2048") ?: "2048"
    
    var udcin by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    
    val db = FirebaseFirestore.getInstance()
    val dao = remember { AppDatabase.getDatabase(context).callHistoryDao() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = udcin.ifEmpty { "Enter UDCIN" },
            style = MaterialTheme.typography.displayMedium,
            color = if (udcin.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        )
        
        DialerKeypad(
            onDigitClick = { if (udcin.length < 10) udcin += it },
            onDeleteClick = { if (udcin.isNotEmpty()) udcin = udcin.dropLast(1) }
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = {
                    if (udcin.isNotEmpty()) {
                        coroutineScope.launch {
                            dao.insertCall(CallHistory(remoteUdcin = udcin, timestamp = System.currentTimeMillis(), type = "OUTGOING"))
                        }
                        val app = context.applicationContext as VoipApp
                        app.webRtcManager.startCall(udcin)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Call, contentDescription = "Call", modifier = Modifier.size(32.dp))
            }
            
            FloatingActionButton(
                onClick = {
                    if (udcin.isNotEmpty()) {
                        if (isRecording) {
                            isRecording = false
                            val voicemailId = UUID.randomUUID().toString()
                            val mockAudioUrl = "https://firebasestorage.googleapis.com/v0/b/mock/audio.mp3"
                            db.collection("users").document(udcin).collection("voicemails").document(voicemailId)
                                .set(mapOf(
                                    "senderUdcin" to myUdcin,
                                    "audioUrl" to mockAudioUrl,
                                    "timestamp" to System.currentTimeMillis()
                                ))
                        } else {
                            isRecording = true
                        }
                    }
                },
                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(72.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Voicemail, contentDescription = "Voicemail", modifier = Modifier.size(32.dp))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DialerKeypad(onDigitClick: (String) -> Unit, onDeleteClick: () -> Unit) {
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#")
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.width(300.dp)
    ) {
        items(keys) { key ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onDigitClick(key) }
            ) {
                Text(text = key, fontSize = 28.sp, fontWeight = FontWeight.Medium)
            }
        }
        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .clickable { onDeleteClick() }
            ) {
                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete", modifier = Modifier.size(32.dp))
            }
        }
    }
}
