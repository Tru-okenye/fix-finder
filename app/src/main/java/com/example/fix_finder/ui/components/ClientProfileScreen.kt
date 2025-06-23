package com.example.fix_finder.ui.components


import User
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfileScreen(
    clientId: String,
    onBack: () -> Unit
) {
    var client by remember { mutableStateOf<User?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(clientId) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(clientId)
            .get()
            .addOnSuccessListener { doc ->
                client = doc.toObject(User::class.java)
                loading = false
            }
            .addOnFailureListener {
                loading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Client Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                client?.let { user ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (!user.profileImageUrl.isNullOrEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(user.profileImageUrl),
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(bottom = 16.dp)
                            )
                        }

                        Text("Name: ${user.name}", style = MaterialTheme.typography.titleMedium)
                        Text("Email: ${user.email}")
                        Text("Phone: ${user.phone}")
                        Text("Location: ${user.location ?: "Not set"}")
                    }
                } ?: Text("Client not found.")
            }
        }
    }
}
