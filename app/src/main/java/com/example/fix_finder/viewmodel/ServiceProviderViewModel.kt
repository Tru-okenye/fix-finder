package com.example.fix_finder.viewmodel

import User


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServiceProviderViewModel : ViewModel() {
    private val _providers = MutableStateFlow<List<User>>(emptyList())
    val providers = _providers.asStateFlow()

    fun loadProviders() {
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("userType", "provider")
            .get()
            .addOnSuccessListener { result ->
                val providerList = result.mapNotNull { it.toObject(User::class.java) }
                _providers.value = providerList
            }
    }
}
