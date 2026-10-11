package edu.muj.studentportal

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import edu.muj.studentportal.databinding.ActivityMainBinding
import edu.muj.studentportal.ui.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var model: PortalViewModel; private set
    private lateinit var binding: ActivityMainBinding
    private var selectedTab = "home"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val app = application as PortalApplication
        val id = app.session.userId() ?: run { logout(); return }
        model = ViewModelProvider(this, PortalViewModel.factory(app.repository, id))[PortalViewModel::class.java]
        binding = ActivityMainBinding.inflate(layoutInflater); setContentView(binding.root); binding.root.systemInsets()
        selectedTab = savedInstanceState?.getString("tab") ?: "home"
        if (savedInstanceState == null) supportFragmentManager.beginTransaction().replace(R.id.content, WorkspaceFragment.create("home")).commit()
        binding.navigation.setOnItemSelectedListener { item ->
            val tab = when (item.itemId) { R.id.nav_study -> "study"; R.id.nav_notices -> "notices"; R.id.nav_requests -> "requests"; R.id.nav_account -> "account"; else -> "home" }
            if (tab != selectedTab || supportFragmentManager.backStackEntryCount > 0) showTab(tab)
            true
        }
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        supportFragmentManager.addOnBackStackChangedListener { updateToolbar() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val current = supportFragmentManager.findFragmentById(R.id.content)
                if (current is FormFragment && current.hasChanges()) {
                    MaterialAlertDialogBuilder(this@MainActivity).setTitle("Discard unsaved changes?")
                        .setMessage("Your changes have not been saved.").setNegativeButton("Keep editing", null)
                        .setPositiveButton("Discard") { _, _ -> navigateBack() }.show()
                } else navigateBack()
            }
        })
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.state.collect { state ->
                    binding.loading.visibility = if (state.loading || state.busy) View.VISIBLE else View.GONE
                    state.snapshot?.let { binding.navigation.menu.findItem(R.id.nav_study).title = if (it.user.isTeacher) "Classes" else "Study" }
                    updateToolbar()
                    val message = state.error ?: state.message
                    if (message != null) {
                        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                        if (binding.navigation.isShown) snackbar.setAnchorView(binding.navigation)
                        snackbar.show(); model.consumeMessage()
                    }
                }
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) { outState.putString("tab", selectedTab); super.onSaveInstanceState(outState) }
    private fun navigateBack() {
        if (model.state.value.busy) return
        if (supportFragmentManager.backStackEntryCount > 0) supportFragmentManager.popBackStack()
        else if (selectedTab != "home") { binding.navigation.selectedItemId = R.id.nav_home }
        else finish()
    }
    fun open(fragment: Fragment) {
        supportFragmentManager.beginTransaction().setReorderingAllowed(true).replace(R.id.content, fragment)
            .addToBackStack(null).commit()
    }
    fun showTab(tab: String) {
        selectedTab = tab
        supportFragmentManager.popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction().replace(R.id.content, WorkspaceFragment.create(tab)).commit()
        binding.navigation.selectedItemId = when (tab) {
            "study" -> R.id.nav_study; "notices" -> R.id.nav_notices; "requests" -> R.id.nav_requests
            "account" -> R.id.nav_account; else -> R.id.nav_home
        }
        updateToolbar()
    }
    fun title(value: String) { binding.toolbar.title = value }
    private fun updateToolbar() {
        val detail = supportFragmentManager.backStackEntryCount > 0
        binding.navigation.visibility = if (detail) View.GONE else View.VISIBLE
        if (detail) { binding.toolbar.setNavigationIcon(R.drawable.ic_back); binding.toolbar.navigationContentDescription = getString(R.string.back) }
        else { binding.toolbar.navigationIcon = null; binding.toolbar.title = when (selectedTab) {
            "study" -> if (model.state.value.snapshot?.user?.isTeacher == true) "Classes" else "Study"
            "notices" -> "Notices"; "requests" -> "Requests"; "account" -> "Account"; else -> "MUJ Portal"
        } }
        binding.toolbar.subtitle = if (detail) null else model.state.value.snapshot?.user?.let {
            if (it.isTeacher) "Teacher workspace" else it.section
        }
    }
    fun logout() {
        (application as PortalApplication).session.logout()
        startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)); finish()
    }
}
