package com.example.fix_finder.ui.components



import Service
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fix_finder.viewmodel.ProviderListViewModel
//import com.example.fix_finder.data.model.Service
//import com.example.fix_finder.data.model.User
import User

@Composable
fun ProviderListingScreen(
    navController: NavController,
    viewModel: ProviderListViewModel = viewModel()
) {
    val providersWithServices by viewModel.providersWithServices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        providersWithServices.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No providers available.")
            }
        }

        else -> {
            Column(modifier = Modifier.padding(16.dp)) {

                // 🔍 Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by location or name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // 🔍 Apply filter on providers
                val filtered = providersWithServices
                    .filterKeys { provider ->
                        val query = searchQuery.trim().lowercase()
                        val name = provider.name?.lowercase() ?: ""
                        val location = provider.location?.lowercase() ?: ""

                        name.contains(query) || location.contains(query)
                    }


                if (filtered.isEmpty()) {
                    Text("No results match your search.", style = MaterialTheme.typography.bodyMedium)
                }

                filtered.forEach { (provider, services) ->
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            navController.currentBackStackEntry
                                ?.savedStateHandle?.set("providerId", provider.uid)
                            navController.navigate("provider_profile")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Profile")
                    }

                    Text(
                        "${provider.name} - ${provider.location}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    services.forEach { service ->
                        Card(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(service.title, style = MaterialTheme.typography.titleSmall)
                                Text(service.description)
                                Text("KES ${service.price}")

                                Button(onClick = {
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle?.set("selected_service", service)
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle?.set("selected_provider", provider)
                                    navController.navigate("booking_screen")
                                }) {
                                    Text("Book")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
