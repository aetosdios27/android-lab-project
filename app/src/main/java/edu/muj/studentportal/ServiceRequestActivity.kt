package edu.muj.studentportal

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import edu.muj.studentportal.databinding.ActivityServiceRequestBinding
import java.util.Locale

class ServiceRequestActivity : BaseActivity() {
    private lateinit var binding: ActivityServiceRequestBinding
    private val requests by lazy { getSharedPreferences("muj_requests", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        binding = ActivityServiceRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setTitle(R.string.forms_service_title)
        configureToolbar(binding.toolbar)
        binding.requestType.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.forms_request_types,
            android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        showLatestRequest()
        binding.submitButton.setOnClickListener { submitRequest() }
        // Android restores all identified form views after onCreate on rotation.
    }

    private fun submitRequest() {
        val subject = binding.subjectInput.text?.toString()?.trim().orEmpty()
        val description = binding.descriptionInput.text?.toString()?.trim().orEmpty()
        binding.subjectLayout.error = null
        binding.descriptionLayout.error = null

        if (binding.requestType.selectedItemPosition <= 0) {
            notifyUser(R.string.forms_select_type)
            binding.requestType.requestFocus()
            return
        }
        if (subject.isEmpty()) {
            binding.subjectLayout.error = getString(R.string.forms_subject_error)
            binding.subjectInput.requestFocus()
            return
        }
        if (description.length < 10) {
            binding.descriptionLayout.error = getString(R.string.forms_description_error)
            binding.descriptionInput.requestFocus()
            return
        }
        val priority = when (binding.priorityGroup.checkedRadioButtonId) {
            R.id.normalPriority -> getString(R.string.forms_normal)
            R.id.urgentPriority -> getString(R.string.forms_urgent)
            else -> {
                notifyUser(R.string.forms_priority_error)
                return
            }
        }
        if (!binding.confirmationCheck.isChecked) {
            notifyUser(R.string.forms_confirm_error)
            return
        }

        // Typed casts tolerate missing or incorrectly typed stored demo values.
        val previousCounter = (requests.all["counter"] as? Number)?.toLong()?.coerceAtLeast(0L) ?: 0L
        if (previousCounter == Long.MAX_VALUE) {
            notifyUser(R.string.forms_save_failed)
            return
        }
        val counter = previousCounter + 1L
        val requestId = String.format(Locale.US, "SR-2026-%03d", counter)
        binding.submitButton.isEnabled = false
        // Commit the counter and full latest record together before announcing success.
        val saved = requests.edit()
            .putLong("counter", counter)
            .putString("latest_id", requestId)
            .putString("latest_type", binding.requestType.selectedItem.toString())
            .putString("latest_subject", subject)
            .putString("latest_description", description)
            .putString("latest_priority", priority)
            .putString("latest_status", "Open")
            .putBoolean("latest_confirmed", true)
            .putLong("latest_created_at", System.currentTimeMillis())
            .commit()
        binding.submitButton.isEnabled = true
        if (!saved) {
            notifyUser(R.string.forms_save_failed)
            return
        }

        binding.requestType.setSelection(0)
        binding.subjectInput.text?.clear()
        binding.descriptionInput.text?.clear()
        binding.priorityGroup.clearCheck()
        binding.confirmationCheck.isChecked = false
        binding.subjectLayout.error = null
        binding.descriptionLayout.error = null
        showLatestRequest()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.forms_request_submitted)
            .setMessage(getString(R.string.forms_request_success, requestId))
            .setPositiveButton(R.string.forms_done, null)
            .show()
    }

    private fun showLatestRequest() {
        val saved = requests.all
        val id = saved["latest_id"] as? String
        if (id.isNullOrBlank()) {
            binding.latestRequest.setText(R.string.forms_no_request)
            return
        }
        binding.latestRequest.text = getString(
            R.string.forms_request_summary,
            id,
            saved["latest_type"] as? String ?: "—",
            saved["latest_subject"] as? String ?: "—",
            saved["latest_priority"] as? String ?: "—",
            saved["latest_status"] as? String ?: "Open"
        )
    }

    private fun notifyUser(message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
