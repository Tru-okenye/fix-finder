package com.example.fix_finder.data.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val delivered: Boolean = false,
    val seen: Boolean = false
)
