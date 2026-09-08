package edu.muj.studentportal

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import edu.muj.studentportal.databinding.ActivityProfileBinding

class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var preferences: PortalPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setTitle(R.string.forms_profile_title)
        configureToolbar(binding.toolbar)
        preferences = PortalPreferences(this)

        // Do not replace unsaved edits restored by Android during rotation.
        if (savedInstanceState == null) {
            binding.phoneInput.setText(preferences.phone)
            binding.alternateEmailInput.setText(preferences.alternateEmail)
        }
        binding.saveProfileButton.setOnClickListener { saveProfile() }
        binding.logoutButton.setOnClickListener {
            preferences.logout()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun saveProfile() {
        val phone = binding.phoneInput.text?.toString()?.trim().orEmpty()
        val alternateEmail = binding.alternateEmailInput.text?.toString()?.trim().orEmpty()
        binding.phoneLayout.error = null
        binding.alternateEmailLayout.error = null
        val digitCount = phone.count { it in '0'..'9' }
        val validPhone = digitCount in 10..15 &&
            phone.matches(Regex("\\+?[0-9][0-9 ()-]*"))
        if (!validPhone) {
            binding.phoneLayout.error = getString(R.string.forms_phone_error)
            binding.phoneInput.requestFocus()
            return
        }
        if (alternateEmail.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(alternateEmail).matches()) {
            binding.alternateEmailLayout.error = getString(R.string.forms_email_error)
            binding.alternateEmailInput.requestFocus()
            return
        }
        preferences.saveProfile(phone, alternateEmail)
        Toast.makeText(this, R.string.forms_profile_saved, Toast.LENGTH_SHORT).show()
    }
}
