package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ContactsScreen() {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).contactDao() }
    val contacts by dao.getAllContacts().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    
    var newUdcin by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Contact") },
            text = {
                Column {
                    OutlinedTextField(value = newUdcin, onValueChange = { newUdcin = it }, label = { Text("UDCIN") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Name") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        dao.insertContact(Contact(newUdcin, newName, null))
                        showDialog = false
                        newUdcin = ""
                        newName = ""
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Contact")
            }
        }
    ) { padding ->
        if (contacts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No contacts saved")
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(contacts) { contact ->
                    ListItem(
                        headlineContent = { Text(contact.displayName) },
                        supportingContent = { Text(contact.udcin) },
                        leadingContent = {
                            Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                        }
                    )
                }
            }
        }
    }
}
