package edu.muj.studentportal

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.muj.studentportal.data.*
import edu.muj.studentportal.ui.dateKey
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PortalRepositoryTest {
    private lateinit var db: PortalDatabase
    private lateinit var repository: PortalRepository
    @Before fun setup() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), PortalDatabase::class.java).build()
        repository = PortalRepository(db); repository.initialize()
    }
    @After fun close() { db.close() }
    private suspend fun rejected(action: suspend () -> Unit) {
        try { action(); fail("Expected authorization or validation rejection") } catch (_: IllegalArgumentException) { }
    }
    @Test fun authenticationDeterminesRoleAndRejectsInvalidCredentials() = runBlocking {
        assertEquals("Student", repository.authenticate("2427030291", "muj123")?.role)
        assertEquals("Teacher", repository.authenticate("t001", "teach123")?.role)
        assertNull(repository.authenticate("T001", "muj123"))
        assertNull(repository.authenticate("unknown", "teach123"))
    }
    @Test fun snapshotsDoNotLeakOtherStudentsOrUnassignedCourses() = runBlocking {
        val student = repository.snapshot(1)
        assertTrue(student.attendance.all { it.studentId == 1L })
        assertTrue(student.marks.all { it.studentId == 1L && it.published })
        assertFalse(student.people.any { it.id == 2L })
        val teacher = repository.snapshot(10)
        assertEquals(setOf(1L, 2L), teacher.courses.map { it.id }.toSet())
        assertEquals(3, teacher.roster(1).size)
    }
    @Test fun correctingAttendanceDoesNotDoubleCountASession() = runBlocking {
        repository.saveAttendance(10, 1, dateKey(), mapOf(1L to true, 2L to false, 3L to true))
        val before = repository.snapshot(1)
        repository.saveAttendance(10, 1, dateKey(), mapOf(1L to false, 2L to true, 3L to true))
        val after = repository.snapshot(1)
        assertEquals(before.sessions.size, after.sessions.size)
        assertEquals(before.attendance.size, after.attendance.size)
        assertFalse(after.attendanceFor(1).first { it.first.date == dateKey() }.second.present)
    }
    @Test fun incompleteAttendanceAndWrongTeacherAreRejectedWithoutWrites() = runBlocking {
        val count = db.dao().sessions().size
        rejected { repository.saveAttendance(10, 1, dateKey(), mapOf(1L to true)) }
        rejected { repository.saveAttendance(11, 1, dateKey(), mapOf(1L to true, 2L to true, 3L to true)) }
        rejected { repository.saveAttendance(1, 1, dateKey(), mapOf(1L to true, 2L to true, 3L to true)) }
        rejected { repository.saveAttendance(10, 1, "2099-01-01", mapOf(1L to true, 2L to true, 3L to true)) }
        assertEquals(count, db.dao().sessions().size)
    }
    @Test fun marksRemainHiddenUntilValidCompletePublication() = runBlocking {
        val scores = mapOf(1L to 28, 2L to 24, 3L to 26)
        repository.saveMarks(10, 1, scores, false)
        assertFalse(repository.snapshot(1).marks.any { it.courseId == 1L })
        rejected { repository.saveMarks(10, 1, mapOf(1L to 31, 2L to 24, 3L to 26), true) }
        rejected { repository.saveMarks(10, 1, mapOf(1L to null, 2L to 24, 3L to 26), true) }
        repository.saveMarks(10, 1, scores, true)
        assertEquals(28, repository.snapshot(1).marks.first { it.courseId == 1L }.score)
        repository.saveMarks(10, 1, scores, false)
        assertFalse(repository.snapshot(1).marks.any { it.courseId == 1L })
    }
    @Test fun requestCrudEnforcesOwnershipAndReviewLifecycle() = runBlocking {
        val id = repository.saveRequest(1, 1, "Attendance correction", "Please review the last session.", "Attendance", "Normal")
        assertTrue(repository.snapshot(10).requests.any { it.id == id })
        assertFalse(repository.snapshot(2).requests.any { it.id == id })
        rejected { repository.saveRequest(2, 1, "Another subject", "This is another description.", "Other", "Normal", id) }
        rejected { repository.deleteRequest(2, id) }
        repository.saveRequest(1, 1, "Updated correction", "Please review the Monday session.", "Attendance", "Urgent", id)
        repository.respond(10, id, "I will check the register.", "In review")
        assertEquals("In review", repository.snapshot(1).requests.first { it.id == id }.status)
        assertEquals(1, repository.snapshot(1).responses.count { it.requestId == id })
        rejected { repository.deleteRequest(1, id) }
        rejected { repository.saveRequest(1, 1, "New subject", "A revised description here.", "Other", "Normal", id) }
        rejected { repository.respond(11, id, "This should be rejected.", "Resolved") }
        repository.respond(10, id, "The register has been corrected.", "Resolved")
        assertEquals("Resolved", repository.snapshot(1).requests.first { it.id == id }.status)
        val deletable = repository.saveRequest(1, 1, "Course question", "Please clarify the coursework.", "Coursework", "Normal")
        repository.deleteRequest(1, deletable)
        assertFalse(repository.snapshot(1).requests.any { it.id == deletable })
    }
    @Test fun noticesRequireAssignedTeacherAndSupportUpdatesAndDeletion() = runBlocking {
        rejected { repository.saveNotice(1, 1, "Invalid notice", "Students must not publish notices.") }
        rejected { repository.saveNotice(11, 1, "Invalid notice", "Unassigned teachers cannot publish.") }
        val id = repository.saveNotice(10, 1, "Next lab", "Bring your working application build.")
        assertTrue(repository.snapshot(1).notices.any { it.id == id })
        assertFalse(repository.snapshot(11).notices.any { it.id == id })
        repository.saveNotice(10, 1, "Updated lab", "Bring your laptop and working build.", id)
        assertEquals("Updated lab", repository.snapshot(1).notices.first { it.id == id }.title)
        rejected { repository.deleteNotice(11, id) }
        repository.deleteNotice(10, id)
        assertFalse(repository.snapshot(1).notices.any { it.id == id })
    }
    @Test fun profileValidationRejectsMalformedDataAndRetainsValidEdits() = runBlocking {
        rejected { repository.saveProfile(1, "123", "student@example.edu") }
        rejected { repository.saveProfile(1, "9876543210", "bad email") }
        repository.saveProfile(1, "+91 98765 43210", "student@example.edu")
        assertEquals("student@example.edu", repository.snapshot(1).user.email)
        assertEquals("+91 98765 43210", repository.snapshot(1).user.phone)
    }
    @Test fun recordsSurviveDatabaseCloseAndReopen() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val name = "persistence-test.db"
        context.deleteDatabase(name)
        val first = Room.databaseBuilder(context, PortalDatabase::class.java, name).build()
        val repo = PortalRepository(first); repo.initialize()
        val id = repo.saveRequest(1, 1, "Persistent request", "This request must survive reopening.", "Other", "Normal")
        repo.respond(10, id, "Saved response for the student.", "Resolved")
        first.close()
        val second = Room.databaseBuilder(context, PortalDatabase::class.java, name).build()
        try {
            val data = PortalRepository(second).snapshot(1)
            assertEquals("Resolved", data.requests.first { it.id == id }.status)
            assertEquals(1, data.responses.count { it.requestId == id })
        } finally { second.close(); context.deleteDatabase(name) }
    }
    @Test fun legacyImportPreservesEditsAndRunsOnlyOnInitialCreation() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val profile = context.getSharedPreferences("muj_portal", android.content.Context.MODE_PRIVATE)
        val requests = context.getSharedPreferences("muj_requests", android.content.Context.MODE_PRIVATE)
        val savedProfile = profile.all; val savedRequests = requests.all
        fun restore(preferences: android.content.SharedPreferences, values: Map<String, *>) {
            preferences.edit().clear().apply { values.forEach { (key, value) -> when (value) {
                is String -> putString(key, value); is Boolean -> putBoolean(key, value); is Long -> putLong(key, value)
                is Int -> putInt(key, value); is Float -> putFloat(key, value)
            } } }.commit()
        }
        val migrated = Room.inMemoryDatabaseBuilder(context, PortalDatabase::class.java).build()
        try {
            profile.edit().putString("phone", "9876543210").putString("alternate_email", "old@example.edu").commit()
            requests.edit().putString("latest_id", "SR-2026-001").putString("latest_subject", "Older saved request")
                .putString("latest_description", "Preserve the previous description.").commit()
            val repo = PortalRepository(migrated, LegacyImporter(context)::import)
            repo.initialize(); repo.initialize()
            assertEquals("old@example.edu", repo.snapshot(1).user.email)
            assertEquals(1, repo.snapshot(1).requests.size)
            assertTrue(repo.snapshot(1).requests.first().description.contains("Imported"))
        } finally { migrated.close(); restore(profile, savedProfile); restore(requests, savedRequests) }
    }
    @Test fun rememberedSessionAndLogoutHaveExplicitPersistence() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val preferences = context.getSharedPreferences("portal_session_v2", android.content.Context.MODE_PRIVATE)
        val original = preferences.all["user_id"] as? Long
        try {
            val store = SessionStore(context)
            store.login(1, false)
            assertEquals(1L, store.userId())
            assertNull(SessionStore(context).userId())
            store.login(10, true)
            assertEquals(10L, SessionStore(context).userId())
            store.logout()
            assertNull(store.userId()); assertNull(SessionStore(context).userId())
        } finally {
            preferences.edit().apply { if (original == null) remove("user_id") else putLong("user_id", original) }.commit()
        }
    }
}
