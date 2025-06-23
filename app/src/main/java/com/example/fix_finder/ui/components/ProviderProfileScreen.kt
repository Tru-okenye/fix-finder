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
import com.example.fix_finder.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileScreen(
    providerId: String,
    onBack: () -> Unit
) {
    var provider by remember { mutableStateOf<User?>(null) }
    var loading by remember { mutableStateOf(true)}
    var averageRating by remember { mutableStateOf(0.0) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }



        LaunchedEffect(providerId) {

            val userRef = FirebaseFirestore.getInstance().collection("users").document(providerId)

            userRef.get().addOnSuccessListener { doc ->
                provider = doc.toObject(User::class.java)
                averageRating = doc.getDouble("averageRating") ?: 0.0
                loading = false
            }

            userRef.collection("reviews")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    reviews = result.documents.mapNotNull { it.toObject(Review::class.java) }
                }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(providerId)
            .get()
            .addOnSuccessListener { doc ->
                provider = doc.toObject(User::class.java)
                loading = false
            }
            .addOnFailureListener {
                loading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Provider Profile") },
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
                provider?.let { user ->
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

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("⭐ Average Rating: ${String.format("%.1f", averageRating)}", style = MaterialTheme.typography.titleMedium)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Client Reviews", style = MaterialTheme.typography.titleMedium)
                        if (reviews.isEmpty()) {
                            Text("No reviews yet.")
                        } else {
                            reviews.forEach { review ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Rating: ${review.rating}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Review: ${review.review}")
                                    }
                                }
                            }
                        }

                    }
                } ?: Text("Provider not found.")
            }
        }
    }
}
