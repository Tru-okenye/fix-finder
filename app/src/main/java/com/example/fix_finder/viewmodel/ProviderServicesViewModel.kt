package com.example.fix_finder.viewmodel


import Service
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.example.fix_finder.data.model.Service
import com.example.fix_finder.data.repository.FirestoreServiceRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProviderServicesViewModel(
    private val repository: FirestoreServiceRepository = FirestoreServiceRepository()
) : ViewModel() {

    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services: StateFlow<List<Service>> = _services

    fun loadServices() {
        viewModelScope.launch {
            _services.value = repository.getProviderServices()
        }
    }


    fun addService(service: Service) {
        viewModelScope.launch {
            repository.addService(service)
            loadServices()
        }
    }

    fun updateService(service: Service) {
        viewModelScope.launch {
            repository.updateService(service)
            loadServices()
        }
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            repository.deleteService(serviceId)
            loadServices()
        }
    }
}
