package edu.muj.studentportal.data

import android.content.Context

/** Preserve the prior prototype's user-entered data on the first v2 database creation. */
class LegacyImporter(private val context: Context) {
    suspend fun import(dao: PortalDao) {
        val profile = context.getSharedPreferences("muj_portal", Context.MODE_PRIVATE).all
        val phone = profile["phone"] as? String
        val email = profile["alternate_email"] as? String
        if (phone != null || !email.isNullOrBlank()) {
            val user = dao.user(1)!!
            dao.updateProfile(1, phone ?: user.phone, email?.takeIf { it.isNotBlank() } ?: user.email)
        }
        val request = context.getSharedPreferences("muj_requests", Context.MODE_PRIVATE).all
        if ((request["latest_id"] as? String).isNullOrBlank()) return
        val subject = (request["latest_subject"] as? String)?.takeIf { it.isNotBlank() } ?: return
        val description = request["latest_description"] as? String ?: "Imported from the previous prototype."
        val now = (request["latest_created_at"] as? Number)?.toLong() ?: System.currentTimeMillis()
        dao.insertRequest(RequestEntity(courseId = 1, studentId = 1, subject = subject,
            description = "$description\n\nImported from the previous prototype and assigned to the Android course teacher for this demo.",
            category = "Other", priority = (request["latest_priority"] as? String)?.takeIf { it in listOf("Normal", "Urgent") } ?: "Normal",
            createdAt = now, updatedAt = now))
    }
}
