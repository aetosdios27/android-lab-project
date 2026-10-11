package edu.muj.studentportal.data

import androidx.room.*

@Entity(tableName = "users", indices = [Index(value = ["login"], unique = true)])
data class UserEntity(@PrimaryKey val id: Long, val login: String, val name: String, val role: String,
    val email: String, val phone: String, val section: String, val salt: String, val verifier: String)

@Entity(tableName = "courses", foreignKeys = [ForeignKey(entity = UserEntity::class,
    parentColumns = ["id"], childColumns = ["teacherId"])], indices = [Index("teacherId")])
data class CourseEntity(@PrimaryKey val id: Long, val code: String, val name: String, val credits: Int,
    val teacherId: Long, val section: String)

@Entity(tableName = "enrollments", primaryKeys = ["courseId", "studentId"], foreignKeys = [
    ForeignKey(entity = CourseEntity::class, parentColumns = ["id"], childColumns = ["courseId"]),
    ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["studentId"])],
    indices = [Index("studentId")])
data class EnrollmentEntity(val courseId: Long, val studentId: Long)

@Entity(tableName = "schedule", foreignKeys = [ForeignKey(entity = CourseEntity::class,
    parentColumns = ["id"], childColumns = ["courseId"])], indices = [Index("courseId")])
data class ScheduleEntity(@PrimaryKey val id: Long, val courseId: Long, val weekday: Int,
    val start: String, val end: String, val room: String)

@Entity(tableName = "sessions", foreignKeys = [ForeignKey(entity = CourseEntity::class,
    parentColumns = ["id"], childColumns = ["courseId"])],
    indices = [Index(value = ["courseId", "date"], unique = true)])
data class SessionEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val courseId: Long, val date: String)

@Entity(tableName = "attendance", primaryKeys = ["sessionId", "studentId"], foreignKeys = [
    ForeignKey(entity = SessionEntity::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.CASCADE),
    ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["studentId"])], indices = [Index("studentId")])
data class AttendanceEntity(val sessionId: Long, val studentId: Long, val present: Boolean)

@Entity(tableName = "marks", primaryKeys = ["courseId", "studentId"], foreignKeys = [
    ForeignKey(entity = CourseEntity::class, parentColumns = ["id"], childColumns = ["courseId"]),
    ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["studentId"])], indices = [Index("studentId")])
data class MarkEntity(val courseId: Long, val studentId: Long, val score: Int?, val maximum: Int = 30,
    val published: Boolean = false, val updatedAt: Long)

@Entity(tableName = "notices", foreignKeys = [
    ForeignKey(entity = CourseEntity::class, parentColumns = ["id"], childColumns = ["courseId"]),
    ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["authorId"])],
    indices = [Index("courseId"), Index("authorId")])
data class NoticeEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val courseId: Long,
    val authorId: Long, val title: String, val body: String, val createdAt: Long, val updatedAt: Long)

@Entity(tableName = "requests", foreignKeys = [
    ForeignKey(entity = CourseEntity::class, parentColumns = ["id"], childColumns = ["courseId"]),
    ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["studentId"])],
    indices = [Index("courseId"), Index("studentId")])
data class RequestEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val courseId: Long,
    val studentId: Long, val subject: String, val description: String, val category: String,
    val priority: String, val status: String = "Open", val createdAt: Long, val updatedAt: Long)

@Entity(tableName = "responses", foreignKeys = [
    ForeignKey(entity = RequestEntity::class, parentColumns = ["id"], childColumns = ["requestId"], onDelete = ForeignKey.CASCADE),
    ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["authorId"])],
    indices = [Index("requestId"), Index("authorId")])
data class ResponseEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val requestId: Long,
    val authorId: Long, val body: String, val createdAt: Long)

data class User(val id: Long, val login: String, val name: String, val role: String,
    val email: String, val phone: String, val section: String) {
    val isTeacher get() = role == "Teacher"
}
fun UserEntity.publicUser() = User(id, login, name, role, email, phone, section)

data class PortalSnapshot(val user: User, val courses: List<CourseEntity>, val people: List<User>,
    val enrollments: List<EnrollmentEntity>, val schedule: List<ScheduleEntity>, val sessions: List<SessionEntity>,
    val attendance: List<AttendanceEntity>, val marks: List<MarkEntity>, val notices: List<NoticeEntity>,
    val requests: List<RequestEntity>, val responses: List<ResponseEntity>) {
    fun course(id: Long) = courses.first { it.id == id }
    fun person(id: Long) = people.first { it.id == id }
    fun roster(courseId: Long) = enrollments.filter { it.courseId == courseId }.map { person(it.studentId) }.sortedBy { it.name }
    fun attendanceFor(courseId: Long, studentId: Long = user.id): List<Pair<SessionEntity, AttendanceEntity>> =
        sessions.filter { it.courseId == courseId }.mapNotNull { session ->
            attendance.firstOrNull { it.sessionId == session.id && it.studentId == studentId }?.let { session to it }
        }.sortedByDescending { it.first.date }
}
