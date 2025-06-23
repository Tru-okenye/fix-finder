package com.example.fix_finder.ui.components


import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fix_finder.data.model.Booking
import com.example.fix_finder.viewmodel.ProviderBookingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingRequestsScreen(
    onBack: () -> Unit,
    navController: NavController,
//    currentUserId: String,
    viewModel: ProviderBookingsViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadProviderBookings()
    }

    val bookings by viewModel.bookings.collectAsState()
    // Get current user ID from FirebaseAuth
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Requests") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            if (bookings.isEmpty()) {
                Text("No booking requests yet.")
            } else {
                bookings.forEach { booking ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Button(
                                onClick = {
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("clientId", booking.clientId)
                                    navController.navigate("client_profile")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("View Client Profile")
                            }

                            Text("Service: ${booking.serviceTitle}", style = MaterialTheme.typography.titleMedium)
                            Text("Client: ${booking.clientName}")
                            Text("Message: ${booking.message}")
                            Text("Status: ${booking.status}")

                            if (booking.status == "pending") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row {
                                    Button(
                                        onClick = {
                                            Log.d("BookingAction", "Accept clicked for booking ID: ${booking.id}")
                                            viewModel.updateBookingStatus(booking.id, "accepted")
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Accept")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            Log.d("BookingAction", "Reject clicked for booking ID: ${booking.id}")
                                            viewModel.updateBookingStatus(booking.id, "rejected")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Reject")
                                    }
                                }
                            }

                            if (booking.status == "accepted" || booking.status == "completed") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set("recipientId", booking.clientId)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("currentUserId", currentUserId)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("isClient", true)

                                        navController.navigate("chat")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Chat with ${booking.clientName}")
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
                                        ?.set("isClient", false)
                                    navController.navigate("messages_screen")
                                }
                            ) {
                                Text("Messages")
                            }

                        }
                    }
                }
            }
        }
    }
}
