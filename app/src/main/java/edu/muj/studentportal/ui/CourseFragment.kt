package edu.muj.studentportal.ui

import android.os.Bundle
import edu.muj.studentportal.data.PortalSnapshot

class CourseFragment : BaseListFragment() {
    private val courseId get() = requireArguments().getLong("id")
    override fun render(s: PortalSnapshot): List<PortalRow> = buildList {
        val course = s.courses.firstOrNull { it.id == courseId } ?: run { add(PortalRow("missing", "Course unavailable", "You do not have access to this course.")); return@buildList }
        host.title(course.code)
        add(heading("course", course.name, "${course.code} · ${course.credits} credits · Section ${course.section}\n${s.person(course.teacherId).name}"))
        if (s.user.isTeacher) {
            add(heading("actions", "Manage this class"))
            add(PortalRow("attendance", "Record attendance", "Choose a date and mark each enrolled student. Existing sessions can be corrected.", badge = "Open attendance register", click = { host.open(FormFragment.create("attendance", courseId)) }))
            val published = s.marks.count { it.courseId == courseId && it.published }
            add(PortalRow("marks", "Internal assessment", "$published of ${s.roster(courseId).size} results published · Maximum 30 marks", badge = "Enter or publish marks", click = { host.open(FormFragment.create("marks", courseId)) }))
            add(PortalRow("notice", "Keep your students informed", "Publish a course announcement.", badge = "Write announcement", click = { host.open(FormFragment.create("notice", courseId)) }))
            add(heading("roster", "Enrolled students · ${s.roster(courseId).size}"))
            s.roster(courseId).forEach { student ->
                val records = s.attendanceFor(courseId, student.id)
                val mark = s.marks.firstOrNull { it.courseId == courseId && it.studentId == student.id }
                add(PortalRow("student${student.id}", student.name, "${attendanceSummary(records.count { it.second.present }, records.size)}\nInternal: ${mark?.score?.let { "$it / 30" } ?: "Not entered"} · ${if (mark?.published == true) "Published" else "Draft"}", student.login))
            }
        } else {
            val records = s.attendanceFor(courseId)
            val present = records.count { it.second.present }
            add(heading("attendance", "Attendance"))
            add(PortalRow("summary", attendanceSummary(present, records.size),
                if (attendanceAtRisk(present, records.size)) "You are below the 75% attendance threshold. Speak to your teacher if a record needs correction." else "Attendance is calculated from recorded class sessions. Threshold: 75%.",
                badge = if (records.isEmpty()) "Awaiting teacher records" else if (attendanceAtRisk(present, records.size)) "Below threshold" else "At or above threshold",
                attention = attendanceAtRisk(present, records.size)))
            add(heading("marks", "Internal assessment"))
            val mark = s.marks.firstOrNull { it.courseId == courseId && it.studentId == s.user.id }
            add(PortalRow("result", mark?.score?.let { "$it / ${mark.maximum}" } ?: "Not published yet",
                if (mark == null) "Your teacher has not published this assessment. Check back after results are released." else "Published ${timestamp(mark.updatedAt)}. This internal score is not a semester GPA."))
            add(PortalRow("help", "Need a correction or clarification?", "Send a request to ${s.person(course.teacherId).name}.", badge = "New course request", click = { host.open(FormFragment.create("request", courseId)) }))
            add(heading("history", "Attendance history"))
            if (records.isEmpty()) add(PortalRow("nohistory", "No attendance recorded", "Session records will appear after your teacher saves attendance."))
            records.forEach { (session, record) -> add(PortalRow("session${session.id}", dateLabel(session.date), badge = if (record.present) "Present" else "Absent", attention = !record.present)) }
        }
        add(heading("schedule", "Class timetable"))
        s.schedule.filter { it.courseId == courseId }.forEach { slot ->
            add(PortalRow("slot${slot.id}", "${weekdayLabel(slot.weekday)} · ${slot.start}–${slot.end}", slot.room))
        }
    }
    companion object { fun create(id: Long) = CourseFragment().apply { arguments = Bundle().apply { putLong("id", id) } } }
}
