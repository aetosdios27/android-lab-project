package edu.muj.studentportal

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private val navigate = Runnable {
        val destination = if (PortalPreferences(this).hasSession()) DashboardActivity::class.java else LoginActivity::class.java
        startActivity(Intent(this, destination))
        finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }
    override fun onStart() { super.onStart(); handler.postDelayed(navigate, 1000) }
    override fun onStop() { handler.removeCallbacks(navigate); super.onStop() }
}
