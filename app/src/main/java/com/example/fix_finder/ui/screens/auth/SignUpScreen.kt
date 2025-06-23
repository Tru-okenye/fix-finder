package com.example.fix_finder.ui.screens.auth


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.fix_finder.ui.components.UserTypeDropdown
import com.example.fix_finder.utils.RequestLocationPermission
import com.example.fix_finder.utils.getAddressFromCoordinates
import com.example.fix_finder.utils.getCurrentLocation

@Composable
fun SignUpScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current

    var locationCoordinates by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var locationText by remember { mutableStateOf("") }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("Client") }

    RequestLocationPermission {
        getCurrentLocation(context) { location ->
            location?.let {
                locationCoordinates = it.latitude to it.longitude
                locationText = getAddressFromCoordinates(context, it.latitude, it.longitude)
            }
        }
    }

    LaunchedEffect(authState, currentUser) {
        if (authState is AuthState.Success && currentUser != null) {
            val route = when (currentUser?.userType?.lowercase()) {
                "client" -> "client_home"
                "provider" -> "provider_home"
                else -> null
            }
            route?.let {
                navController.navigate(it) {
                    popUpTo("signup") { inclusive = true }
                }
            }
            authViewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {



                item {
                    Text(
                        text = "Create an Account",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(20.dp))
                }

                item {
                    var fullNameError by remember { mutableStateOf<String?>(null) }
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            fullNameError = if (it.isBlank()) "Full name is required" else null
                        },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        isError = fullNameError != null,
                        supportingText = {
                            fullNameError?.let { Text(it) }
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }

                item {
                    var emailError by remember { mutableStateOf<String?>(null) }
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = if (!android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches())
                                "Enter a valid email" else null
                        },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        isError = emailError != null,
                        supportingText = {
                            emailError?.let { Text(it) }
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }

                item {
                    var passwordError by remember { mutableStateOf<String?>(null) }
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = if (it.length < 6)
                                "Password must be at least 6 characters" else null
                        },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        isError = passwordError != null,
                        supportingText = {
                            passwordError?.let { Text(it) }
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }

                item {
                    var phoneError by remember { mutableStateOf<String?>(null) }
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            phoneError = if (!it.matches(Regex("^\\+?[0-9]{10,13}\$")))
                                "Enter a valid phone number" else null
                        },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        isError = phoneError != null,
                        supportingText = {
                            phoneError?.let { Text(it) }
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }

                item {
                    OutlinedTextField(
                        value = locationText,
                        onValueChange = { locationText = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                }

                item {
                    UserTypeDropdown(
                        selectedType = userType,
                        onTypeSelected = { userType = it }
                    )
                    Spacer(Modifier.height(24.dp))
                }

                item {
                    Button(
                        onClick = {
                            val isValid = fullName.isNotBlank() &&
                                    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                                    password.length >= 6 &&
                                    phone.matches(Regex("^\\+?[0-9]{10,13}\$"))

                            if (isValid) {
                                authViewModel.register(
                                    name = fullName,
                                    email = email,
                                    password = password,
                                    phone = phone,
                                    userType = userType,
                                    location = locationText
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = authState != AuthState.Loading
                    ) {
                        if (authState == AuthState.Loading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Sign Up")
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    when (authState) {
                        is AuthState.Error -> {
                            Text(
                                text = (authState as AuthState.Error).error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        is AuthState.Success -> {
                            Text(
                                text = (authState as AuthState.Success).message,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        else -> {}
                    }

                    Spacer(Modifier.height(16.dp))

                    TextButton(onClick = {
                        navController.navigate("login") {
                            popUpTo("signup") { inclusive = true }
                        }
                    }) {
                        Text("Already have an account? Log In")
                    }
                }
            }
        }

    }
}
