package com.example.fix_finder.ui.navigation

import LoginScreen
import Service
import User
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
//import com.example.fix_finder.data.model.User
//import com.example.fix_finder.data.model.Services
import com.example.fix_finder.ui.components.AddEditServiceScreen
import com.example.fix_finder.ui.components.BookingRequestsScreen
import com.example.fix_finder.ui.components.BookingScreen
import com.example.fix_finder.ui.components.ChangePasswordScreen
import com.example.fix_finder.ui.components.ChatScreen
import com.example.fix_finder.ui.components.ClientBookingsScreen
import com.example.fix_finder.ui.components.ClientProfileScreen
import com.example.fix_finder.ui.components.EditProfileScreen
import com.example.fix_finder.ui.components.MessagesScreen
import com.example.fix_finder.ui.components.ProviderListingScreen
import com.example.fix_finder.ui.components.ProviderProfileScreen
import com.example.fix_finder.ui.components.SettingsScreen
import com.example.fix_finder.ui.screens.auth.AuthViewModel
import com.example.fix_finder.ui.screens.auth.ForgotPasswordScreen
import com.example.fix_finder.ui.screens.auth.SignUpScreen
import com.example.fix_finder.ui.screens.common.LandingScreen
import com.example.fix_finder.ui.screens.home.ClientHomeScreen
import com.example.fix_finder.ui.screens.home.ProviderHomeScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavGraph(navController: NavHostController, isDarkTheme: Boolean,
                onToggleTheme: () -> Unit, authViewModel: AuthViewModel, startDestination: String = "landing") {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login Screen
        composable("login") {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController)
        }

        // Sign Up Screen
        composable("signup") {
            SignUpScreen(navController = navController)
        }

        // Home Screen
        composable("landing") {
            LandingScreen(navController = navController)
        }

        composable("client_home") {
            ClientHomeScreen(
                navController = navController, // ✅ add this

                onLogout = {
                    navController.navigate("login") {
                        popUpTo("client_home") { inclusive = true }
                    }
                },
                name = authViewModel.currentUser.value?.name ?: "Client"
            )
        }

        composable("client_bookings") {
            ClientBookingsScreen(
                navController = navController,

                onBack = { navController.popBackStack() }
            )
        }



        composable("provider_home") {

            ProviderHomeScreen(
                navController = navController, // ✅ add this

                onLogout = {
                    navController.navigate("login") {
                        popUpTo("provider_home") { inclusive = true }
                    }
                },
                onAddService = {
                    navController.navigate("add_service")
                },
                onEditService = { service ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("edit_service", service)
                    navController.navigate("edit_service")
                },
                        onViewBookingRequests = {
                    navController.navigate("booking_requests")
                },
                providerName = authViewModel.currentUser.value?.name ?: "Provider"
            )
        }

        composable("booking_requests") {
            BookingRequestsScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }


        composable("chat") {
            val navBackStackEntry = navController.previousBackStackEntry
            val recipientId = navBackStackEntry?.savedStateHandle?.get<String>("recipientId")
            val currentUserId = navBackStackEntry?.savedStateHandle?.get<String>("currentUserId")
            val isClient = navBackStackEntry?.savedStateHandle?.get<Boolean>("isClient") ?: true

            if (recipientId != null && currentUserId != null) {
                ChatScreen(
                    currentUserId = currentUserId,
                    recipientId = recipientId,
                    isClient = isClient
                )
            }
        }

        composable("add_service") {
            AddEditServiceScreen(
                onSave = { service ->
                    // Save to DB or list
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable("edit_service") {
            val service = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Service>("edit_service")

            AddEditServiceScreen(
                initialService = service,
                onSave = { updated ->
                    // Save updated data
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable("browse_providers") {
            ProviderListingScreen(navController = navController)
        }

        composable("booking_screen") {
            val service = navController.previousBackStackEntry
                ?.savedStateHandle?.get<Service>("selected_service")
            val provider = navController.previousBackStackEntry
                ?.savedStateHandle?.get<User>("selected_provider")

            if (service != null && provider != null) {
                BookingScreen(navController = navController, service = service, provider = provider)
            }
        }


        composable("messages_screen") {
            val currentUserId = navController.previousBackStackEntry
                ?.savedStateHandle?.get<String>("currentUserId") ?: return@composable
            val isClient = navController.previousBackStackEntry
                ?.savedStateHandle?.get<Boolean>("isClient") ?: true

            MessagesScreen(
                currentUserId = currentUserId,
                isClient = isClient,
                navController = navController,
                onBack = { navController.popBackStack() }

            )
        }

        composable("settings") {
            SettingsScreen(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onEditProfile = { navController.navigate("edit_profile") },
                onChangePassword = { navController.navigate("change_password") },
                onNotifications = { navController.navigate("messages_screen") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("change_password") {
            ChangePasswordScreen(
                onBack = { navController.popBackStack() },
                onPasswordChanged = {
                    // Show success, then navigate back or to home
                    navController.popBackStack()
                },
                onError = { message ->
                    // You can add a Snackbar or Toast here if you want
                }
            )
        }
        composable("edit_profile") {
            EditProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }



        composable("client_profile") {
            val clientId = navController.previousBackStackEntry
                ?.savedStateHandle?.get<String>("clientId")

            if (clientId != null) {
                ClientProfileScreen(
                    clientId = clientId,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable("provider_profile") {
            val providerId = navController.previousBackStackEntry
                ?.savedStateHandle?.get<String>("providerId")

            if (providerId != null) {
                ProviderProfileScreen(
                    providerId = providerId,
                    onBack = { navController.popBackStack() }
                )
            }
        }


    }
}
