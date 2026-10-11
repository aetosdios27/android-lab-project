package edu.muj.studentportal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import edu.muj.studentportal.databinding.ActivityLoginBinding
import edu.muj.studentportal.ui.systemInsets
import edu.muj.studentportal.ui.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val app get() = application as PortalApplication
    private var signingIn = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (app.session.userId() != null) { openWorkspace(); return }
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root); binding.root.systemInsets()
        binding.loginContent.post {
            val max = binding.root.dp(480)
            if (binding.loginContent.width > max) binding.loginContent.layoutParams = binding.loginContent.layoutParams.apply { width = max }
        }
        binding.loginButton.setOnClickListener { signIn() }
        binding.passwordInput.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) { signIn(); true } else false
        }
        binding.demoButton.setOnClickListener {
            MaterialAlertDialogBuilder(this).setTitle("Demo accounts")
                .setMessage("Student\n2427030291 · muj123\nPushpendra Singh\n\nTeacher\nT001 · teach123\nDr. Usha Jain · Android & Networks\n\nSecond teacher\nT002 · teach123\nDr. Neha Sharma · remaining courses\n\nAll accounts share this device's database. These are local demonstration accounts.")
                .setPositiveButton("Close", null).show()
        }
    }
    private fun signIn() {
        if (signingIn) return
        val login = binding.registrationInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString().orEmpty()
        binding.registrationLayout.error = null; binding.passwordLayout.error = null
        binding.loginError.visibility = View.GONE
        if (login.isBlank()) { binding.registrationLayout.error = getString(R.string.login_id_error); binding.registrationInput.requestFocus(); return }
        if (password.isBlank()) { binding.passwordLayout.error = getString(R.string.login_password_error); binding.passwordInput.requestFocus(); return }
        signingIn = true; binding.loginButton.isEnabled = false; binding.loginButton.text = "Signing in…"
        lifecycleScope.launch {
            try {
                val user = app.repository.authenticate(login, password)
                if (user == null) { binding.loginError.text = getString(R.string.invalid_credentials); binding.loginError.visibility = View.VISIBLE }
                else { app.session.login(user.id, binding.rememberMe.isChecked); binding.passwordInput.text?.clear(); openWorkspace() }
            } catch (error: Exception) { if (error is CancellationException) throw error
                binding.loginError.text = "Unable to open local data. Try again."; binding.loginError.visibility = View.VISIBLE
            } finally { signingIn = false; binding.loginButton.isEnabled = true; binding.loginButton.setText(R.string.sign_in) }
        }
    }
    private fun openWorkspace() {
        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)); finish()
    }
}
