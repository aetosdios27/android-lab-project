package edu.muj.studentportal

import android.content.Context

/** Only remembered sessions survive process death; user-entered data stays on this device. */
class PortalPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("muj_portal", Context.MODE_PRIVATE)
    companion object { private var activeSession = false }
    var remembered: Boolean
        get() = prefs.all["remembered"] as? Boolean ?: false
        set(value) { prefs.edit().putBoolean("remembered", value).apply() }
    var phone: String
        get() = prefs.all["phone"] as? String ?: "+91 98765 43210"
        set(value) { prefs.edit().putString("phone", value).apply() }
    var alternateEmail: String
        get() = prefs.all["alternate_email"] as? String ?: ""
        set(value) { prefs.edit().putString("alternate_email", value).apply() }
    fun hasSession() = activeSession || remembered
    fun login(remember: Boolean) { activeSession = true; remembered = remember }
    fun logout() { activeSession = false; remembered = false }
    fun saveProfile(phone: String, alternateEmail: String) {
        prefs.edit().putString("phone", phone).putString("alternate_email", alternateEmail).apply()
    }
}
