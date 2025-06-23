package com.example.fix_finder.data.model


data class Booking(
    var id: String = "",
    val serviceId: String = "",
    val serviceTitle: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val message: String = "",
    val status: String = "pending",
    val isRated: Boolean? = false,
    val timestamp: Long = System.currentTimeMillis()

)
