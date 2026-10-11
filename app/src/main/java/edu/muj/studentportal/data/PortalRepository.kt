package edu.muj.studentportal.data

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

/** All role, ownership and state rules live here, independent of screen visibility. */
class PortalRepository(private val database: PortalDatabase, private val importLegacy: (suspend (PortalDao) -> Unit)? = null) {
    private val dao = database.dao()
    private val initialization = Mutex()
    private var initialized = false

    suspend fun initialize() = initialization.withLock {
        if (!initialized) {
            database.withTransaction { if (dao.users().isEmpty()) { DemoSeed.populate(dao); importLegacy?.invoke(dao) } }
            initialized = true
        }
    }
    suspend fun authenticate(login: String, password: String): User? {
        initialize()
        val user = dao.login(login.trim().uppercase(Locale.US)) ?: return null
        return withContext(Dispatchers.Default) { if (PasswordVerifier.matches(password, user)) user.publicUser() else null }
    }
    suspend fun snapshot(userId: Long): PortalSnapshot {
        initialize()
        return database.withTransaction {
            val user = actor(userId)
            val allEnrollments = dao.enrollments()
            val courses = dao.courses().filter { if (user.isTeacher) it.teacherId == user.id
                else allEnrollments.any { enrollment -> enrollment.studentId == user.id && enrollment.courseId == it.id } }
            val courseIds = courses.map { it.id }.toSet()
            val enrollments = allEnrollments.filter { it.courseId in courseIds && (user.isTeacher || it.studentId == user.id) }
            val peopleIds = enrollments.map { it.studentId }.toSet() + courses.map { it.teacherId } + user.id
            val sessions = dao.sessions().filter { it.courseId in courseIds }
            val sessionIds = sessions.map { it.id }.toSet()
            val requests = dao.requests().filter { it.courseId in courseIds && (user.isTeacher || it.studentId == user.id) }
            val requestIds = requests.map { it.id }.toSet()
            PortalSnapshot(user, courses, dao.users().filter { it.id in peopleIds }.map { it.publicUser() }, enrollments,
                dao.schedule().filter { it.courseId in courseIds }, sessions,
                dao.attendance().filter { it.sessionId in sessionIds && (user.isTeacher || it.studentId == user.id) },
                dao.marks().filter { it.courseId in courseIds && (user.isTeacher || (it.studentId == user.id && it.published)) },
                dao.notices().filter { it.courseId in courseIds }, requests,
                dao.responses().filter { it.requestId in requestIds })
        }
    }
    private suspend fun actor(id: Long) = dao.user(id)?.publicUser() ?: error("Your account is unavailable. Sign in again.")
    private suspend fun assignedTeacher(userId: Long, courseId: Long): User {
        val user = actor(userId)
        require(user.isTeacher && dao.courses().any { it.id == courseId && it.teacherId == userId }) { "You can only manage your assigned classes." }
        return user
    }
    private suspend fun enrolledStudent(userId: Long, courseId: Long) {
        require(!actor(userId).isTeacher && dao.enrollments().any { it.studentId == userId && it.courseId == courseId }) { "Choose one of your enrolled courses." }
    }
    private fun validateText(value: String, min: Int, max: Int, label: String) {
        require(value.trim().length in min..max) { "$label must contain $min–$max characters." }
    }
    suspend fun saveAttendance(userId: Long, courseId: Long, date: String, choices: Map<Long, Boolean>) = database.withTransaction {
        assignedTeacher(userId, courseId)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        require(Regex("\\d{4}-\\d{2}-\\d{2}").matches(date) && runCatching { formatter.parse(date) }.isSuccess) { "Choose a valid session date." }
        require(date <= formatter.format(java.util.Date())) { "Attendance cannot be recorded for a future date." }
        val roster = dao.enrollments().filter { it.courseId == courseId }.map { it.studentId }.toSet()
        require(roster.isNotEmpty() && choices.keys == roster) { "Record attendance for every enrolled student." }
        val session = dao.sessions().firstOrNull { it.courseId == courseId && it.date == date }
        val id = session?.id ?: dao.insertSession(SessionEntity(courseId = courseId, date = date))
        dao.saveAttendance(choices.map { AttendanceEntity(id, it.key, it.value) })
    }
    suspend fun saveMarks(userId: Long, courseId: Long, scores: Map<Long, Int?>, publish: Boolean) = database.withTransaction {
        assignedTeacher(userId, courseId)
        val roster = dao.enrollments().filter { it.courseId == courseId }.map { it.studentId }.toSet()
        require(scores.keys == roster && roster.isNotEmpty()) { "Include every enrolled student." }
        require(scores.values.all { it == null || it in 0..30 }) { "Each mark must be between 0 and 30." }
        require(!publish || scores.values.none { it == null }) { "Enter every student's mark before publishing." }
        // Saving a revision as a draft deliberately hides it until it is published again.
        dao.saveMarks(scores.map { MarkEntity(courseId, it.key, it.value, published = publish, updatedAt = System.currentTimeMillis()) })
    }
    suspend fun saveNotice(userId: Long, courseId: Long, title: String, body: String, id: Long = 0): Long = database.withTransaction {
        assignedTeacher(userId, courseId)
        validateText(title, 3, 120, "Title"); validateText(body, 10, 3000, "Announcement")
        val now = System.currentTimeMillis()
        if (id == 0L) dao.insertNotice(NoticeEntity(courseId = courseId, authorId = userId,
            title = title.trim(), body = body.trim(), createdAt = now, updatedAt = now))
        else {
            val old = dao.notices().firstOrNull { it.id == id } ?: error("This notice no longer exists.")
            require(old.authorId == userId && old.courseId == courseId) { "You can only edit your own notices." }
            dao.updateNotice(old.copy(title = title.trim(), body = body.trim(), updatedAt = now)); id
        }
    }
    suspend fun deleteNotice(userId: Long, id: Long) = database.withTransaction {
        val notice = dao.notices().firstOrNull { it.id == id } ?: error("This notice no longer exists.")
        assignedTeacher(userId, notice.courseId)
        require(notice.authorId == userId) { "You can only delete your own notices." }
        dao.deleteNotice(id)
    }
    private suspend fun editableRequest(userId: Long, id: Long): RequestEntity {
        val request = dao.requests().firstOrNull { it.id == id } ?: error("This request no longer exists.")
        enrolledStudent(userId, request.courseId)
        require(request.studentId == userId) { "You can only edit your own requests." }
        require(request.status == "Open" && dao.responses().none { it.requestId == id }) { "Reviewed requests cannot be edited or deleted." }
        return request
    }
    suspend fun saveRequest(userId: Long, courseId: Long, subject: String, description: String,
        category: String, priority: String, id: Long = 0): Long = database.withTransaction {
        enrolledStudent(userId, courseId)
        validateText(subject, 3, 120, "Subject"); validateText(description, 10, 3000, "Description")
        require(category in listOf("Attendance", "Marks", "Coursework", "Other")) { "Choose a request category." }
        require(priority in listOf("Normal", "Urgent")) { "Choose a priority." }
        val now = System.currentTimeMillis()
        if (id == 0L) dao.insertRequest(RequestEntity(courseId = courseId, studentId = userId,
            subject = subject.trim(), description = description.trim(), category = category,
            priority = priority, createdAt = now, updatedAt = now))
        else {
            val old = editableRequest(userId, id)
            require(old.courseId == courseId) { "A request's course cannot be changed after submission." }
            dao.updateRequest(old.copy(subject = subject.trim(), description = description.trim(), category = category,
                priority = priority, updatedAt = now)); id
        }
    }
    suspend fun deleteRequest(userId: Long, id: Long) = database.withTransaction {
        editableRequest(userId, id); dao.deleteRequest(id)
    }
    suspend fun respond(userId: Long, id: Long, message: String, status: String) = database.withTransaction {
        val request = dao.requests().firstOrNull { it.id == id } ?: error("This request no longer exists.")
        assignedTeacher(userId, request.courseId)
        validateText(message, 3, 3000, "Reply")
        require(status in listOf("In review", "Resolved")) { "Choose a valid status." }
        val now = System.currentTimeMillis()
        dao.insertResponse(ResponseEntity(requestId = id, authorId = userId, body = message.trim(), createdAt = now))
        dao.updateRequest(request.copy(status = status, updatedAt = now))
    }
    suspend fun saveProfile(userId: Long, phone: String, email: String) = database.withTransaction {
        actor(userId)
        require(phone.isEmpty() || (phone.count { it in '0'..'9' } in 10..15 && phone.matches(Regex("\\+?[0-9][0-9 ()-]*")))) { "Enter a phone number with 10–15 digits, or leave it blank." }
        require(email.length <= 254 && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { "Enter a valid email address." }
        dao.updateProfile(userId, phone.trim(), email.trim())
    }
}
