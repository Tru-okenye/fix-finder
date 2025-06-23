package com.example.fix_finder.ui.components


import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fix_finder.data.model.Booking
import com.example.fix_finder.viewmodel.ClientBookingsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientBookingsScreen(
    navController: NavController,
    onBack: () -> Unit
) {
    val viewModel: ClientBookingsViewModel = viewModel()
    val bookings by viewModel.bookings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showRatingDialog by remember { mutableStateOf(false) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadClientBookings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (bookings.isEmpty()) {
                Text("You have no bookings.")
            } else {
                bookings.forEach { booking ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Service: ${booking.serviceTitle}")
                            Text("Provider: ${booking.providerName}")
                            Text("Message: ${booking.message}")
                            Text("Status: ${booking.status}")

                            Spacer(Modifier.height(8.dp))

                            if (booking.status == "accepted") {
                                Button(onClick = {
                                    viewModel.markBookingAsCompleted(booking.id)
                                }) {
                                    Text("Mark as Completed")
                                }
                            }

                            if (booking.status == "completed") {
                                Log.d("UI", "Showing Rate Provider button for booking ${booking.id}, isRated = ${booking.isRated}")
                                Button(
                                    onClick = {
                                        val db = FirebaseFirestore.getInstance()
                                        db.collection("bookings").document(booking.id)
                                            .get(Source.SERVER)
                                            .addOnSuccessListener { snapshot ->
                                                val isRated = snapshot.getBoolean("isRated") ?: false
                                                if (!isRated) {
                                                    selectedBooking = booking
                                                    showRatingDialog = true
                                                } else {
                                                    Log.w("UI", "Blocked opening rating dialog. Booking ${booking.id} already rated.")
                                                    // 👇 Show Snackbar
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            message = "You've already rated this provider.",
                                                            actionLabel = "OK",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("UI", "Failed to check isRated status for booking ${booking.id}", e)
                                            }
                                    },

                                            enabled = booking.isRated != true
                                ) {
                                    Text(if (booking.isRated == true) "Rated" else "Rate Provider")
                                }
                            }

                            if (booking.status == "accepted" || booking.status == "completed") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                                        navController.currentBackStackEntry?.savedStateHandle?.set("recipientId", booking.providerId)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("currentUserId", currentUserId)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("isClient", false)

                                        navController.navigate("chat")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Chat with ${booking.providerName}")
                                }
                            }

                            Button(
                                onClick = {
                                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("currentUserId", currentUserId)
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("isClient", true)
                                    navController.navigate("messages_screen")
                                }
                            ) {
                                Text("Messages")
                            }



                            if (booking.status != "cancelled" && booking.status != "completed" && booking.status != "accepted") {

                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.cancelBooking(booking.id) { success ->
                                            if (!success) {
                                                // Optional: show Snackbar or Toast
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Cancel Booking")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showRatingDialog && selectedBooking != null) {
            RatingDialog(
                providerId = selectedBooking!!.providerId,
                bookingId = selectedBooking!!.id,
                onDismiss = {
                    showRatingDialog = false
                    selectedBooking = null
                },
                onSubmit = { rating, review ->
                    viewModel.submitProviderRating(
                        selectedBooking!!.providerId,
                        selectedBooking!!.id,
                        rating,
                        review
                    )
                    showRatingDialog = false
                    selectedBooking = null
//                    viewModel.loadClientBookings()
                }
            )
        }
    }
}
