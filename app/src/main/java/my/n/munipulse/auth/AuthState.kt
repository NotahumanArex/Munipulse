package my.n.munipulse.auth


sealed class AuthState {
    // The initial state before the user does anything
    object Idle : AuthState()

    // When the network request is happening (show a progress bar)
    object Loading : AuthState()

    // When login/registration is completed successfully (navigate to Feed)
    object Success : AuthState()

    // When something goes wrong (show a Toast or Snackbar)
    data class Error(val message: String) : AuthState()
}