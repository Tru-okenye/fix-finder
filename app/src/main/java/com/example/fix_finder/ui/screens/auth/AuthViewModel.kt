package com.example.fix_finder.ui.screens.auth


import User
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.example.fix_finder.data.model.User
import com.example.fix_finder.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository() // injected or default
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        userType: String,
        location: String
    ) {
        println("Registering user...")
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.registerUser(name, email, password, phone, userType, location)
            println("Register result: $result")
            _authState.value = result.fold(
                onSuccess = {
                    println("Registration succeeded")
                    _currentUser.value = it
                    AuthState.Success("Registration successful")
                },
                onFailure = {
                    println("Registration failed: ${it.message}")
                    AuthState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.loginUser(email, password)
            _authState.value = result.fold(
                onSuccess = {
                    fetchUser(it)
                    AuthState.Success("Login successful")
                },
                onFailure = { AuthState.Error(it.message ?: "Login failed") }
            )
        }
    }

    private fun fetchUser(uid: String) {
        viewModelScope.launch {
            val result = authRepository.getUser(uid)
            result.onSuccess { user -> _currentUser.value = user }
        }
    }


}
