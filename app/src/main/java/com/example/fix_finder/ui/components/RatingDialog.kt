package com.example.fix_finder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun RatingDialog(
    providerId: String,
    bookingId: String,
    onDismiss: () -> Unit,
    onSubmit: (Float, String) -> Unit
) {
    var rating by remember { mutableStateOf(0f) }
    var reviewText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onSubmit(rating, reviewText) },
                enabled = rating > 0f
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Rate Your Provider") },
        text = {
            Column {
                Text("Please rate the provider's service:")
                Slider(
                    value = rating,
                    onValueChange = { rating = it },
                    steps = 4,
                    valueRange = 1f..5f
                )
                Text("Rating: ${rating.toInt()}/5")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text("Write a review (optional)") }
                )
            }
        }
    )
}
