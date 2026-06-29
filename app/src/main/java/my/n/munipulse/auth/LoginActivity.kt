package com.munipulse.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

import kotlinx.coroutines.launch
import my.n.munipulse.MainActivity
import my.n.munipulse.R
import my.n.munipulse.auth.AuthState
import my.n.munipulse.auth.RegisterActivity

class LoginActivity : AppCompatActivity() {

    // Instantiate the ViewModel using the factory
    private val viewModel: AuthViewModel by viewModels { AuthViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Make sure this matches your XML file name (e.g., activity_login.xml)
        setContentView(R.layout.activity_login)


        // 1. Link your XML views using your exact IDs
        val emailEditText = findViewById<TextInputEditText>(R.id.et_email)
        val passwordEditText = findViewById<TextInputEditText>(R.id.et_password)
        val loginButton = findViewById<MaterialButton>(R.id.btn_login)
        val googleButton = findViewById<MaterialButton>(R.id.btn_google)
        val githubButton = findViewById<MaterialButton>(R.id.btn_github)
        val signUpText = findViewById<TextView>(R.id.tv_go_signup)
        val forgotPasswordText = findViewById<TextView>(R.id.tv_forgot)
        val rememberMeCheck = findViewById<CheckBox>(R.id.cb_remember)
        val adminLoginText = findViewById<TextView>(R.id.btn_admin_login)

        signUpText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // 2. Handle Button Clicks
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            viewModel.login(email, password)
        }

        signUpText.setOnClickListener {
            // Navigate to your Register Activity
             val intent = Intent(this, RegisterActivity::class.java)
             startActivity(intent)
        }

        googleButton.setOnClickListener {
            // TODO: Trigger Google Sign-In intent
            Toast.makeText(this, "Google Sign-In clicked", Toast.LENGTH_SHORT).show()
        }

        forgotPasswordText.setOnClickListener {
            // TODO: Navigate to Forgot Password screen
        }

        // 3. Observe the ViewModel State
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Idle -> {
                            // Reset UI to default
                            loginButton.text = "Log In"
                            loginButton.isEnabled = true
                            emailEditText.isEnabled = true
                            passwordEditText.isEnabled = true
                        }
                        is AuthState.Loading -> {
                            // Change button text and disable inputs to prevent double-clicks
                            loginButton.text = "Logging in..."
                            loginButton.isEnabled = false
                            emailEditText.isEnabled = false
                            passwordEditText.isEnabled = false
                        }
                        is AuthState.Success -> {
                            loginButton.text = "Success!"
                            viewModel.clearError()

                            Toast.makeText(this@LoginActivity, "Welcome back!", Toast.LENGTH_SHORT).show()

                            // 2. Navigate to the Main Dashboard!
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)

                            // VERY IMPORTANT: Call finish() so the user can't press the
                            // Android "Back" button to return to the Login screen!
                            finish()
                        }
                        is AuthState.Error -> {
                            // Restore UI and show the error message
                            loginButton.text = "Log In"
                            loginButton.isEnabled = true
                            emailEditText.isEnabled = true
                            passwordEditText.isEnabled = true

                            Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }
}