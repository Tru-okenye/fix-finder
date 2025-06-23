package com.example.fix_finder.ui.screens.home


import Service
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
//import com.example.fix_finder.data.model.Service
import com.example.fix_finder.viewmodel.ProviderServicesViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderHomeScreen(onLogout: () -> Unit,
                       navController: NavController,
                       onAddService: () -> Unit,
                       onEditService: (Service) -> Unit,
                       onViewBookingRequests: () -> Unit,
                       providerName: String,
                       viewModel: ProviderServicesViewModel = viewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.loadServices()
    }

    val services by viewModel.services.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome, $providerName") },
                actions = {

                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.primary // <- For Logout button
                )
            )

        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
//            verticalArrangement = Arrangement.Top,
//            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Your Dashboard", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))


            Box(modifier = Modifier.weight(1f)) {
                ServiceManagementUI(
                    services = services,
                    onAddService = onAddService,
                    onEditService = onEditService,
                    onDeleteService = { service -> viewModel.deleteService(service.id) }
                )
            }


            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onViewBookingRequests,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Booking Requests")
            }


        }
    }
}

@Composable
fun ServiceManagementUI(
    services: List<Service>,
    onAddService: () -> Unit,
     onEditService: (Service) -> Unit,
    onDeleteService: (Service) -> Unit
){
    println("Rendering UI with ${services.size} services")
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Your Services", style = MaterialTheme.typography.titleLarge)
            Button(onClick = onAddService) {
                Text("Add Service")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (services.isEmpty()) {
            Text("You haven't added any services yet.")
        } else {
            services.forEach { service ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(service.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(service.description)
                        Spacer(Modifier.height(4.dp))
                        Text("Price: ${service.price}")

                        Spacer(Modifier.height(8.dp))
                        Row {
                            OutlinedButton(onClick = { onEditService(service) }) {
                                Text("Edit")
                            }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(onClick = { onDeleteService(service) }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

