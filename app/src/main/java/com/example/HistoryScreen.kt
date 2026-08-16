package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).callHistoryDao() }
    val history by dao.getAllHistory().collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }

    if (history.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No call history")
        }
    } else {
        LazyColumn(Modifier.fillMaxSize()) {
            items(history) { call ->
                ListItem(
                    headlineContent = { Text(call.remoteUdcin) },
                    supportingContent = { Text(dateFormat.format(Date(call.timestamp))) },
                    leadingContent = {
                        val icon = when (call.type) {
                            "INCOMING" -> Icons.Filled.CallReceived
                            "OUTGOING" -> Icons.Filled.CallMade
                            else -> Icons.Filled.CallMissed
                        }
                        val tint = if (call.type == "MISSED") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        Icon(icon, contentDescription = call.type, tint = tint)
                    }
                )
            }
        }
    }
}
