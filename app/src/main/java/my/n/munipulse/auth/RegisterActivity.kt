package my.n.munipulse.auth

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.munipulse.app.ui.auth.AuthViewModel
import com.munipulse.app.ui.auth.AuthViewModelFactory
import kotlinx.coroutines.launch
import my.n.munipulse.MainActivity
import my.n.munipulse.R
// You will need to import your specific AuthViewModel and AuthState here

class RegisterActivity : AppCompatActivity() {

    // Share the same ViewModel logic
    private val viewModel: AuthViewModel by viewModels { AuthViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // 1. Link your XML views
        val firstNameEditText = findViewById<TextInputEditText>(R.id.et_first_name)
        val lastNameEditText = findViewById<TextInputEditText>(R.id.et_last_name)
        val emailEditText = findViewById<TextInputEditText>(R.id.et_email)
        val passwordEditText = findViewById<TextInputEditText>(R.id.et_password)
        val registerButton = findViewById<MaterialButton>(R.id.btn_signup)
        val loginText = findViewById<TextView>(R.id.tv_go_login)

        // 2. Handle Button Clicks
        registerButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Pass all 4 fields to the ViewModel
            viewModel.register(email, password, firstName, lastName)
        }

        loginText.setOnClickListener {
            // Just finish this activity to go back to the Login screen
            finish()
        }

        // 3. Observe the ViewModel State
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Idle -> {
                            registerButton.text = "Sign Up"
                            registerButton.isEnabled = true
                        }
                        is AuthState.Loading -> {
                            registerButton.text = "Creating account..."
                            registerButton.isEnabled = false
                        }
                        is AuthState.Success -> {
                            registerButton.text = "Success!"
                            viewModel.clearError()

                            Toast.makeText(this@RegisterActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()

                            // 2. Navigate straight into the app!
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            startActivity(intent)

                            // finishAffinity() closes BOTH the Register screen AND the Login screen
                            // underneath it, so hitting "Back" closes the app completely.
                            finishAffinity()
                        }
                        is AuthState.Error -> {
                            registerButton.text = "Sign Up"
                            registerButton.isEnabled = true
                            Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_LONG).show()
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }
}