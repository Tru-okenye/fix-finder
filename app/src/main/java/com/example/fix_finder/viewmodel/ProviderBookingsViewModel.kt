package com.example.fix_finder.viewmodel


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fix_finder.data.model.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProviderBookingsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings

    fun loadProviderBookings() {
        val currentUser = auth.currentUser ?: return
        val providerId = currentUser.uid

        viewModelScope.launch {
            db.collection("bookings")
                .whereEqualTo("providerId", providerId)
                .get()
                .addOnSuccessListener { result ->
                    val bookingList = result.documents.mapNotNull { doc ->
                        doc.toObject(Booking::class.java)?.copy(id = doc.id)
                    }
                    _bookings.value = bookingList
                }
        }
    }


    fun updateBookingStatus(bookingId: String, newStatus: String) {
        Log.d("BookingAction", "Updating booking $bookingId to $newStatus")
        viewModelScope.launch {
            db.collection("bookings")
                .document(bookingId)
                .update("status", newStatus)
                .addOnSuccessListener {
                    Log.d("BookingAction", "Successfully updated booking $bookingId to $newStatus")
                    loadProviderBookings()
                }
                .addOnFailureListener { e ->
                    Log.e("BookingAction", "Failed to update booking $bookingId", e)
                }
        }
    }



}
