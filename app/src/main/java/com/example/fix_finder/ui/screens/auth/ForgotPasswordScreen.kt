package com.example.fix_finder.ui.screens.auth


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forgot Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Enter your email to receive password reset instructions.")

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val emailText = email.text.trim()
                    if (emailText.isEmpty()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please enter your email.")
                        }
                        return@Button
                    }

                    isLoading = true

                    // Check if the email exists in Firestore
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .whereEqualTo("email", emailText)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                // Email exists, proceed to send reset
                                auth.sendPasswordResetEmail(emailText)
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        scope.launch {
                                            if (task.isSuccessful) {
                                                snackbarHostState.showSnackbar("Password reset email sent")
                                                navController.popBackStack()
                                            } else {
                                                snackbarHostState.showSnackbar(task.exception?.message ?: "Failed to send reset email")
                                            }
                                        }
                                    }
                            } else {
                                isLoading = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("No account found with this email.")
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Error checking email: ${e.message}")
                            }
                        }

                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Send Reset Link")
                }
            }
        }
    }
}
