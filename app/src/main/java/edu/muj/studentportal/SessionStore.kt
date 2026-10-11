package edu.muj.studentportal

import android.content.Context

/** Only an explicitly remembered account survives process termination. */
class SessionStore(context: Context) {
    private val preferences = context.getSharedPreferences("portal_session_v2", Context.MODE_PRIVATE)
    private var currentId: Long? = null
    fun userId(): Long? = currentId ?: (preferences.all["user_id"] as? Long)?.takeIf { it > 0 }
    fun login(id: Long, remember: Boolean) {
        currentId = id
        preferences.edit().apply { if (remember) putLong("user_id", id) else remove("user_id") }.apply()
    }
    fun logout() { currentId = null; preferences.edit().clear().apply() }
}
