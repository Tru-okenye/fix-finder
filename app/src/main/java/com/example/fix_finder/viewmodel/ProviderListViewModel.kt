package com.example.fix_finder.viewmodel


import Service
import User
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fix_finder.utils.calculateDistance
//import com.example.fix_finder.data.model.Service
//import com.example.fix_finder.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProviderListViewModel : ViewModel() {

    private val _providersWithServices = MutableStateFlow<Map<User, List<Service>>>(emptyMap())
    val providersWithServices = _providersWithServices.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()



    private val db = FirebaseFirestore.getInstance()

    init {
        fetchProvidersAndServices()
    }

    private fun fetchProvidersAndServices() {
        _isLoading.value = true

        db.collection("users")
            .whereEqualTo("userType", "Provider")
            .get()
            .addOnSuccessListener { usersSnapshot ->
                val providers = usersSnapshot.documents.mapNotNull { it.toObject(User::class.java) }

                val tempMap = mutableMapOf<User, List<Service>>()
                var remaining = providers.size

                if (remaining == 0) {
                    _providersWithServices.value = emptyMap()
                    _isLoading.value = false
                    return@addOnSuccessListener
                }

                providers.forEach { provider ->
                    db.collection("services")
                        .whereEqualTo("providerId", provider.uid)
                        .get()
                        .addOnSuccessListener { serviceSnap ->
                            val services = serviceSnap.toObjects(Service::class.java)
                            tempMap[provider] = services
                            remaining--
                            if (remaining == 0) {
                                _providersWithServices.value = tempMap
                                _isLoading.value = false
                            }
                        }
                        .addOnFailureListener {
                            remaining--
                            if (remaining == 0) {
                                _providersWithServices.value = tempMap
                                _isLoading.value = false
                            }
                        }
                }
            }
            .addOnFailureListener {
                _providersWithServices.value = emptyMap()
                _isLoading.value = false
            }
    }



}
