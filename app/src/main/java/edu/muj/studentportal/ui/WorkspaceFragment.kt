package edu.muj.studentportal.ui

import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import edu.muj.studentportal.data.*
import java.util.Calendar

class WorkspaceFragment : BaseListFragment() {
    private val tab get() = requireArguments().getString("tab") ?: "home"
    override fun render(snapshot: PortalSnapshot): List<PortalRow> = when (tab) {
        "study" -> courses(snapshot)
        "notices" -> notices(snapshot)
        "requests" -> requests(snapshot)
        "account" -> account(snapshot)
        else -> home(snapshot)
    }
    override fun primary(snapshot: PortalSnapshot): Pair<String, () -> Unit>? = when {
        tab == "requests" && !snapshot.user.isTeacher -> "New request" to { host.open(FormFragment.create("request")) }
        tab == "notices" && snapshot.user.isTeacher -> "Write announcement" to { host.open(FormFragment.create("notice")) }
        else -> null
    }
    private fun home(s: PortalSnapshot) = buildList {
        add(heading("welcome", if (s.user.isTeacher) "Teaching overview" else "Hello, ${s.user.name.substringBefore(' ')}", "${dateLabel(dateKey())}\n${if (s.user.isTeacher) s.user.name else s.user.section}"))
        if (s.user.isTeacher) {
            val pending = s.requests.count { it.status != "Resolved" }
            val unpublished = s.courses.count { course -> s.marks.none { it.courseId == course.id && it.published } }
            add(PortalRow("work", "Your teaching workspace", "$pending active requests · $unpublished classes with unpublished marks",
                "${s.courses.size} assigned classes", "Review your classes", click = { host.showTab("study") }))
        } else {
            val credits = s.courses.sumOf { it.credits }
            val records = s.attendance
            add(PortalRow("overview", "Semester overview", "${s.courses.size} enrolled courses · $credits registered credits",
                s.user.login, "Attendance · ${attendanceSummary(records.count { it.present }, records.size)}"))
            val risk = s.courses.filter { course -> val recordsForCourse = s.attendanceFor(course.id)
                attendanceAtRisk(recordsForCourse.count { it.second.present }, recordsForCourse.size) }
            if (risk.isNotEmpty()) {
                add(heading("attention", "Needs your attention"))
                risk.forEach { course -> val recordsForCourse = s.attendanceFor(course.id)
                    add(PortalRow("risk${course.id}", course.name, attendanceSummary(recordsForCourse.count { it.second.present }, recordsForCourse.size),
                        course.code, "Below 75% · View attendance", attention = true, click = { openCourse(course.id) })) }
            }
        }
        add(heading("today", "Today’s classes"))
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val slots = s.schedule.filter { it.weekday == today }
        if (slots.isEmpty()) add(PortalRow("noclass", "No classes scheduled today", "Your weekly timetable is available in ${if (s.user.isTeacher) "Classes" else "Study"}.", badge = "View timetable", click = { host.open(ScheduleFragment()) }))
        else slots.forEach { slot -> val course = s.course(slot.courseId)
            add(PortalRow("slot${slot.id}", course.name, "${slot.room}\n${s.person(course.teacherId).name}",
                "${slot.start}–${slot.end} · ${course.code}", "Open course", click = { openCourse(course.id) })) }
        add(heading("updates", "Latest announcements"))
        if (s.notices.isEmpty()) add(PortalRow("nonotice", "You’re up to date", "Announcements from your course teachers will appear here."))
        s.notices.take(2).forEach { add(noticeRow(s, it)) }
        if (s.user.isTeacher && s.requests.isNotEmpty()) {
            add(heading("pending", "Student requests"))
            s.requests.filter { it.status != "Resolved" }.take(2).forEach { add(requestRow(s, it)) }
        }
    }
    private fun courses(s: PortalSnapshot) = buildList {
        add(heading("courses", if (s.user.isTeacher) "Your assigned classes" else "Your courses",
            if (s.user.isTeacher) "Record attendance, publish marks and support your students." else "Attendance, internal marks and class information in one place."))
        add(PortalRow("schedule", "Weekly timetable", "Class times and rooms for your ${if (s.user.isTeacher) "assigned classes" else "enrolled courses"}.", badge = "View timetable", click = { host.open(ScheduleFragment()) }))
        if (s.courses.isEmpty()) add(PortalRow("empty", "No courses assigned", "This account has no active enrollments or teaching assignments."))
        s.courses.forEach { course ->
            val records = s.attendanceFor(course.id)
            val summary = if (s.user.isTeacher) "${s.roster(course.id).size} students · ${s.sessions.count { it.courseId == course.id }} recorded sessions"
                else attendanceSummary(records.count { it.second.present }, records.size)
            add(PortalRow("course${course.id}", course.name, "${s.person(course.teacherId).name}\n$summary",
                "${course.code} · ${course.credits} credits · ${course.section}", "View course", click = { openCourse(course.id) }))
        }
    }
    private fun notices(s: PortalSnapshot) = buildList {
        add(heading("notices", "Course announcements", if (s.user.isTeacher) "Published to students enrolled in your classes." else "Updates from the teachers of your enrolled courses."))
        if (s.notices.isEmpty()) add(PortalRow("empty", "No announcements yet", if (s.user.isTeacher) "Write an announcement to keep your class informed." else "New announcements from your teachers will appear here."))
        s.notices.forEach { add(noticeRow(s, it)) }
    }
    private fun requests(s: PortalSnapshot) = buildList {
        add(heading("requests", if (s.user.isTeacher) "Student requests" else "Your requests",
            if (s.user.isTeacher) "Review course questions and reply with a clear next step." else "Ask a course teacher for help and follow their response."))
        if (s.requests.isEmpty()) add(PortalRow("empty", "No requests yet", if (s.user.isTeacher) "Requests from enrolled students will appear here." else "Create a request about attendance, marks or coursework."))
        val active = s.requests.filter { it.status != "Resolved" }
        val resolved = s.requests.filter { it.status == "Resolved" }
        if (active.isNotEmpty()) add(heading("active", "Active · ${active.size}"))
        active.forEach { add(requestRow(s, it)) }
        if (resolved.isNotEmpty()) add(heading("resolved", "Resolved · ${resolved.size}"))
        resolved.forEach { add(requestRow(s, it)) }
    }
    private fun account(s: PortalSnapshot) = listOf(
        heading("account", s.user.name, "${s.user.role} · ${s.user.section}"),
        PortalRow("identity", "Account details", "ID: ${s.user.login}\n${s.user.email}\n${s.user.phone.ifBlank { "Phone not added" }}"),
        PortalRow("edit", "Contact information", "Keep your email and phone number up to date.", badge = "Edit contact details", click = { host.open(FormFragment.create("profile")) }),
        PortalRow("about", "About this prototype", "MUJ Student Management Portal\nKotlin · XML · Room / SQLite\n\nSample academic data is stored on this device. Teacher changes appear in student accounts here. This app is not connected to university systems."),
        PortalRow("logout", "Sign out", "Return to sign in. Saved academic records and requests stay on this device.", badge = "Sign out of this account", click = {
            MaterialAlertDialogBuilder(requireContext()).setTitle("Sign out?").setMessage("Your saved data will stay on this device.")
                .setNegativeButton("Cancel", null).setPositiveButton("Sign out") { _, _ -> host.logout() }.show()
        })
    )
    private fun openCourse(id: Long) { host.open(CourseFragment.create(id)) }
    private fun noticeRow(s: PortalSnapshot, notice: NoticeEntity) = PortalRow("notice${notice.id}", notice.title,
        notice.body.take(140).let { if (notice.body.length > 140) "$it…" else it },
        "${s.course(notice.courseId).code} · ${timestamp(notice.updatedAt)}", "Read announcement", click = { host.open(NoticeFragment.create(notice.id)) })
    private fun requestRow(s: PortalSnapshot, request: RequestEntity) = PortalRow("request${request.id}", request.subject,
        if (s.user.isTeacher) "${s.person(request.studentId).name} · ${request.category}" else "${request.category} · ${s.course(request.courseId).name}",
        "REQ-${request.id.toString().padStart(4, '0')} · ${timestamp(request.updatedAt)}", "${request.status} · ${request.priority}",
        attention = request.priority == "Urgent" && request.status != "Resolved", click = { host.open(RequestFragment.create(request.id)) })
    companion object { fun create(tab: String) = WorkspaceFragment().apply { arguments = Bundle().apply { putString("tab", tab) } } }
}
