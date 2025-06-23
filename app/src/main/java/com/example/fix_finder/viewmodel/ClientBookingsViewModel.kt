package com.example.fix_finder.viewmodel


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fix_finder.data.model.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.Source


class ClientBookingsViewModel : ViewModel() {

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings = _bookings.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun loadClientBookings() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("bookings")
            .whereEqualTo("clientId", userId)
            .get()
            .addOnSuccessListener { result ->
                val bookingsList = result.documents.mapNotNull {
                    it.toObject(Booking::class.java)?.copy(id = it.id)
                }.also { loaded ->
                    loaded.forEach { b ->
                        Log.d("BookingLoad", "Booking ${b.id} has isRated = ${b.isRated}")
                    }
                }
                _bookings.value = bookingsList
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun cancelBooking(bookingId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            db.collection("bookings").document(bookingId)
                .update("status", "cancelled")
                .addOnSuccessListener {
                    loadClientBookings()
                    onComplete(true)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        }
    }

    fun markBookingAsCompleted(bookingId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("bookings").document(bookingId)
            .update("status", "completed")
            .addOnSuccessListener {
                // Reload bookings so UI updates immediately
                loadClientBookings()
            }
    }


    fun submitProviderRating(providerId: String, bookingId: String, rating: Float, review: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Step 1: Check if this booking has already been rated
        db.collection("bookings").document(bookingId)
            .get(Source.SERVER)
            .addOnSuccessListener { documentSnapshot ->
                val isRated = documentSnapshot.getBoolean("isRated") ?: false
                if (isRated) {
                    Log.w("SubmitRating", "Booking $bookingId has already been rated. Skipping.")
                    return@addOnSuccessListener
                }

                // Step 2: Proceed to submit the review
                val reviewData = mapOf(
                    "rating" to rating,
                    "review" to review,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "clientId" to currentUserId
                )

                db.collection("users").document(providerId)
                    .collection("reviews")
                    .add(reviewData)
                    .addOnSuccessListener {
                        Log.d("SubmitRating", "Review added successfully. Updating average rating...")
                        updateProviderAverageRating(providerId)

                        // Step 3: Mark booking as rated
                        db.collection("bookings").document(bookingId)
                            .update("isRated", true)
                            .addOnSuccessListener {
                                // Step 4: Refresh local state
                                db.collection("bookings").document(bookingId)
                                    .get(Source.SERVER)
                                    .addOnSuccessListener { snapshot ->
                                        val updatedBooking = snapshot.toObject(Booking::class.java)?.copy(id = bookingId)
                                        if (updatedBooking != null) {
                                            Log.d("RatingUpdate", "Booking $bookingId updated isRated: ${updatedBooking.isRated}")

                                            val updatedList = _bookings.value.map {
                                                if (it.id == bookingId) updatedBooking else it
                                            }
                                            _bookings.value = updatedList
                                        }
                                    }
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("SubmitRating", "Failed to submit review", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("SubmitRating", "Failed to check booking rating status", e)
            }
    }


    private fun updateProviderAverageRating(providerId: String) {
        Log.d("RatingUpdate", "Updating average rating for provider: $providerId")

        val providerReviewsRef = db.collection("users").document(providerId).collection("reviews")

        providerReviewsRef.get().addOnSuccessListener { snapshot ->
            val ratings = snapshot.documents.mapNotNull { it.getDouble("rating") }
            Log.d("RatingUpdate", "Ratings found: $ratings")

            if (ratings.isNotEmpty()) {
                val average = ratings.average()
                Log.d("RatingUpdate", "Calculated average: $average")

                db.collection("users").document(providerId)
                    .update("averageRating", average)
                    .addOnSuccessListener {
                        Log.d("RatingUpdate", "Successfully updated averageRating")
                    }
                    .addOnFailureListener { e ->
                        Log.e("RatingUpdate", "Failed to update averageRating", e)
                    }
            } else {
                Log.d("RatingUpdate", "No ratings found.")
            }
        }.addOnFailureListener { e ->
            Log.e("RatingUpdate", "Failed to fetch reviews", e)
        }
    }


}
