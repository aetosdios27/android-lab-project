package edu.muj.studentportal.data

import edu.muj.studentportal.model.*

/** Local demonstration records; no network or university systems are contacted. */
object MockData {
    val attendance = listOf(
        AttendanceItem("CSE3101", "Computer Networks", 34, 40),
        AttendanceItem("CSE3102", "Software Engineering", 31, 36),
        AttendanceItem("CSE3103", "Android App Development", 28, 30),
        AttendanceItem("CSE3104", "Theory of Computation", 26, 34),
        AttendanceItem("CSE3105", "Database Management Systems", 37, 42)
    )
    val marks = listOf(
        MarksItem("CSE3101", "Computer Networks", 25, 30, "A"),
        MarksItem("CSE3102", "Software Engineering", 27, 30, "A+"),
        MarksItem("CSE3103", "Android App Development", 29, 30, "A+"),
        MarksItem("CSE3104", "Theory of Computation", 23, 30, "B+"),
        MarksItem("CSE3105", "Database Management Systems", 26, 30, "A")
    )
    val courses = listOf(
        Course("CSE3101", "Computer Networks", 4, "Dr. Rajesh Kumar"),
        Course("CSE3102", "Software Engineering", 4, "Dr. Neha Sharma"),
        Course("CSE3103", "Android App Development", 3, "Prof. Ankit Jain"),
        Course("CSE3104", "Theory of Computation", 4, "Dr. Priya Mehta"),
        Course("CSE3105", "Database Management Systems", 4, "Dr. Sameer Gupta")
    )
    val weekdays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    val timetable = mapOf(
        "Monday" to listOf(
            TimetableItem("09:00", "10:00", "Software Engineering", "AB1-203", "Dr. Neha Sharma"),
            TimetableItem("10:00", "11:00", "Computer Networks", "AB1-204", "Dr. Rajesh Kumar"),
            TimetableItem("11:00", "12:00", "Android App Development Lab", "Lab 3", "Prof. Ankit Jain"),
            TimetableItem("14:00", "15:00", "Theory of Computation", "AB1-205", "Dr. Priya Mehta")
        ),
        "Tuesday" to listOf(
            TimetableItem("09:00", "10:00", "Database Management Systems", "AB1-206", "Dr. Sameer Gupta"),
            TimetableItem("10:00", "11:00", "Theory of Computation", "AB1-205", "Dr. Priya Mehta"),
            TimetableItem("11:00", "12:00", "Computer Networks", "AB1-204", "Dr. Rajesh Kumar"),
            TimetableItem("14:00", "16:00", "Android Lab", "Lab 3", "Prof. Ankit Jain")
        ),
        "Wednesday" to listOf(
            TimetableItem("09:00", "10:00", "Android App Development", "AB1-203", "Prof. Ankit Jain"),
            TimetableItem("10:00", "11:00", "Software Engineering", "AB1-203", "Dr. Neha Sharma"),
            TimetableItem("11:00", "12:00", "Database Management Systems", "AB1-206", "Dr. Sameer Gupta"),
            TimetableItem("14:00", "16:00", "Computer Networks Lab", "Lab 2", "Dr. Rajesh Kumar")
        ),
        "Thursday" to listOf(
            TimetableItem("09:00", "10:00", "Theory of Computation", "AB1-205", "Dr. Priya Mehta"),
            TimetableItem("10:00", "11:00", "Computer Networks", "AB1-204", "Dr. Rajesh Kumar"),
            TimetableItem("11:00", "12:00", "Software Engineering", "AB1-203", "Dr. Neha Sharma"),
            TimetableItem("14:00", "16:00", "Database Systems Lab", "Lab 1", "Dr. Sameer Gupta")
        ),
        "Friday" to listOf(
            TimetableItem("09:00", "10:00", "Database Management Systems", "AB1-206", "Dr. Sameer Gupta"),
            TimetableItem("10:00", "11:00", "Android App Development", "AB1-203", "Prof. Ankit Jain"),
            TimetableItem("11:00", "12:00", "Theory of Computation", "AB1-205", "Dr. Priya Mehta"),
            TimetableItem("14:00", "15:00", "Software Engineering Tutorial", "AB1-203", "Dr. Neha Sharma")
        )
    )
    val notices = listOf(
        Notice("Mid Semester Examination Schedule Released", "The Semester V mid-semester examinations will be held from 21 to 26 September 2026. Computer Networks: 21 September; Software Engineering: 22 September; Android App Development: 23 September; Theory of Computation: 24 September; Database Management Systems: 26 September. Each examination runs from 10:00 AM to 11:30 AM. Report to Academic Block 1 at least 30 minutes early with your university ID. Room allocations will be displayed on the department notice board. This is a demonstration schedule, not an official university announcement.", "08 Sep 2026", "Examination"),
        Notice("Android Lab Evaluation", "The Android App Development practical evaluation is scheduled for 14 September 2026, 11:00 AM–1:00 PM in Lab 3. Bring your laptop, a working offline build, and your lab record. Be ready to explain Activity navigation, XML layouts, RecyclerView adapters, and local data storage. Evaluation includes a live demonstration and a short viva conducted by Prof. Ankit Jain. Arrive ten minutes before your allotted lab slot.", "07 Sep 2026", "Academic"),
        Notice("Library Timing Extended During Exams", "The central library reading halls will remain open from 8:00 AM to 11:00 PM between 10 and 26 September 2026 to support mid-semester preparation. The circulation desk closes at 8:00 PM; book issue and return services are available before that time. Carry your university ID, keep phones on silent, and leave shared desks clear. Group discussions should use the designated discussion rooms.", "05 Sep 2026", "General"),
        Notice("Campus Placement Registration Open", "The Training and Placement Cell invites eligible students to register for the September campus recruitment preparation programme by 15 September 2026. Visit the placement office in Academic Block 2 between 10:00 AM and 4:00 PM with your university ID and a one-page resume. Semester V students may register for internship preparation. A briefing on 11 September at 3:00 PM in the Central Auditorium covers eligibility, aptitude practice and the recruitment calendar. This is a local prototype announcement.", "03 Sep 2026", "Placement")
    )
}
