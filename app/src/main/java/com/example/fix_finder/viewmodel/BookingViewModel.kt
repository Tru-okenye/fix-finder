package com.example.fix_finder.viewmodel



import Service
import User
import androidx.lifecycle.ViewModel
import com.example.fix_finder.data.model.Booking
//import com.example.fix_finder.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BookingViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun confirmBooking(service: Service, provider: User, message: String, onResult: (Boolean) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onResult(false)
            return
        }

        // Fetch client info from Firestore
        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val clientName = document.getString("name") ?: "Unknown"

                // Generate document ID
                val bookingRef = db.collection("bookings").document()
                val bookingId = bookingRef.id

                val booking = Booking(
                    id = bookingId,
                    serviceId = service.id,
                    serviceTitle = service.title,
                    providerId = provider.uid,
                    providerName = provider.name,
                    clientId = currentUser.uid,
                    clientName = clientName,
                    message = message
                )

                bookingRef.set(booking)
                    .addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { onResult(false) }
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

}
