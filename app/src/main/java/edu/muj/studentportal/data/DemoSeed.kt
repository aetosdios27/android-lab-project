package edu.muj.studentportal.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DemoSeed {
    suspend fun populate(dao: PortalDao) {
        fun account(id: Long, login: String, name: String, role: String, section: String): UserEntity {
            val salt = PasswordVerifier.salt()
            return UserEntity(id, login, name, role, "${login.lowercase(Locale.US)}@example.edu", "", section,
                salt, PasswordVerifier.hash(if (role == "Teacher") "teach123" else "muj123", salt))
        }
        dao.insertUsers(listOf(
            account(1, "2427030291", "Pushpendra Singh", "Student", "5H · Semester V"),
            account(2, "2427030292", "Aditi Mehta", "Student", "5H · Semester V"),
            account(3, "2427030293", "Rohan Verma", "Student", "5H · Semester V"),
            account(10, "T001", "Dr. Usha Jain", "Teacher", "Computer Science & Engineering"),
            account(11, "T002", "Dr. Neha Sharma", "Teacher", "Computer Science & Engineering")
        ))
        dao.insertCourses(listOf(
            CourseEntity(1, "CSE3142", "Android App Development", 3, 10, "5H"),
            CourseEntity(2, "CSE3101", "Computer Networks", 4, 10, "5H"),
            CourseEntity(3, "CSE3102", "Software Engineering", 4, 11, "5H"),
            CourseEntity(4, "CSE3104", "Theory of Computation", 4, 11, "5H"),
            CourseEntity(5, "CSE3105", "Database Management Systems", 4, 11, "5H")
        ))
        dao.insertEnrollments((1L..5L).flatMap { course -> (1L..3L).map { EnrollmentEntity(course, it) } })
        dao.insertSchedule(listOf(
            ScheduleEntity(1, 1, 2, "09:00", "10:00", "AB1 · 203"),
            ScheduleEntity(2, 2, 2, "10:00", "11:00", "AB1 · 204"),
            ScheduleEntity(3, 3, 2, "11:00", "12:00", "AB1 · 205"),
            ScheduleEntity(4, 5, 3, "09:00", "10:00", "AB1 · 206"),
            ScheduleEntity(5, 1, 3, "14:00", "16:00", "Android lab · 3"),
            ScheduleEntity(6, 4, 4, "09:00", "10:00", "AB1 · 205"),
            ScheduleEntity(7, 2, 4, "10:00", "11:00", "AB1 · 204"),
            ScheduleEntity(8, 3, 5, "10:00", "11:00", "AB1 · 203"),
            ScheduleEntity(9, 5, 5, "14:00", "16:00", "Database lab · 1"),
            ScheduleEntity(10, 1, 6, "09:00", "10:00", "AB1 · 203"),
            ScheduleEntity(11, 4, 6, "11:00", "12:00", "AB1 · 205")
        ))
        for (course in 1L..5L) {
            for (index in 1..8) {
                val calendar = Calendar.getInstance().apply { add(Calendar.DATE, -index * 2 - course.toInt()) }
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
                val id = dao.insertSession(SessionEntity(courseId = course, date = date))
                dao.saveAttendance((1L..3L).map { student ->
                    AttendanceEntity(id, student, if (course == 4L && student == 1L) index <= 5 else (index + student.toInt()) % 7 != 0)
                })
            }
            dao.saveMarks((1L..3L).map { MarkEntity(course, it, 21 + ((course + it) % 8).toInt(),
                published = course != 1L, updatedAt = System.currentTimeMillis()) })
        }
        val now = System.currentTimeMillis()
        dao.insertNotice(NoticeEntity(courseId = 1, authorId = 10, title = "Bring your working build to the next lab",
            body = "Prepare a short demonstration of your app. Show navigation, validation and local persistence, and be ready to explain one database operation. Bring your laptop and project source.", createdAt = now - 86400000, updatedAt = now - 86400000))
        dao.insertNotice(NoticeEntity(courseId = 3, authorId = 11, title = "Project review: requirements and test cases",
            body = "For the next Software Engineering class, prepare your requirements summary and three meaningful test cases. We will review these in class before discussing implementation.", createdAt = now - 172800000, updatedAt = now - 172800000))
    }
}
