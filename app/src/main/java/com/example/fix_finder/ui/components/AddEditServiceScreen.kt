package com.example.fix_finder.ui.components

import Service
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
//import com.example.fix_finder.data.model.Service
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fix_finder.viewmodel.ProviderServicesViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.util.Log



@Composable
fun AddEditServiceScreen(
    initialService: Service? = null,
    onSave: (Service) -> Unit,
    viewModel: ProviderServicesViewModel = viewModel(),
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(initialService?.title ?: "") }
    var description by remember { mutableStateOf(initialService?.description ?: "") }
    var price by remember { mutableStateOf(initialService?.price ?: "") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        Text(text = if (initialService == null) "Add Service" else "Edit Service", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") })
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            val context = LocalContext.current

            Button(onClick = {
                val providerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val newService = Service(
                    id = initialService?.id ?: System.currentTimeMillis().toString(),
                    title = title,
                    description = description,
                    price = price,
                    providerId = providerId
                )


                Log.d("AddEditServiceScreen", "Created Service: $newService")

                if (initialService == null) {
                    viewModel.addService(newService)
                    Toast.makeText(context, "Service added!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.updateService(newService)
                    Toast.makeText(context, "Service updated!", Toast.LENGTH_SHORT).show()
                }

                onSave(newService)
            }) {
                Text(text = if (initialService == null) "Add" else "Update")
            }

            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}
