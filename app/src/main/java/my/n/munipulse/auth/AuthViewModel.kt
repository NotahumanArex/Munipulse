package com.munipulse.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.munipulse.app.repository.FirebaseAuthRepositoryImpl

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.n.munipulse.auth.AuthRepository
import my.n.munipulse.auth.AuthState

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    // Internal mutable state that only the ViewModel can change
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)

    // Public immutable state that your Activity/Fragment/Compose screen observes
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Expose the current user so the UI knows if someone is already logged in
    val currentUser get() = repository.currentUser

    fun login(email: String, password: String) {
        // Basic validation
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            repository.loginWithEmail(email, password).fold(
                onSuccess = {
                    _authState.value = AuthState.Success
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.localizedMessage ?: "An error occurred during login.")
                }
            )
        }
    }

    fun register(email: String, password: String, firstName: String, lastName: String) {
        // Basic validation
        if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank()) {
            _authState.value = AuthState.Error("All fields are required.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            repository.registerWithEmail(email, password, firstName, lastName).fold(
                onSuccess = {
                    _authState.value = AuthState.Success
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.localizedMessage ?: "Registration failed.")
                }
            )
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            repository.loginWithGoogle(idToken).fold(
                onSuccess = {
                    _authState.value = AuthState.Success
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.localizedMessage ?: "Google sign-in failed.")
                }
            )
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = AuthState.Idle
    }

    // Call this if the user dismisses an error dialog so it doesn't pop up again
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }
}
class AuthViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            // Manually injecting the implementation
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(FirebaseAuthRepositoryImpl()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}