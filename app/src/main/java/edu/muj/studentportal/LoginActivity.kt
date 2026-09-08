package edu.muj.studentportal

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.muj.studentportal.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.loginButton.setOnClickListener {
            val registration = binding.registrationInput.text.toString().trim()
            val password = binding.passwordInput.text.toString()
            binding.registrationLayout.error = null
            binding.passwordLayout.error = null
            when {
                registration.isBlank() -> { binding.registrationLayout.error = getString(R.string.registration_required); binding.registrationInput.requestFocus() }
                password.isBlank() -> { binding.passwordLayout.error = getString(R.string.password_required); binding.passwordInput.requestFocus() }
                registration != "229301234" || password != "muj123" -> binding.passwordLayout.error = getString(R.string.invalid_credentials)
                else -> {
                    PortalPreferences(this).login(binding.rememberMe.isChecked)
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
            }
        }
    }
}
