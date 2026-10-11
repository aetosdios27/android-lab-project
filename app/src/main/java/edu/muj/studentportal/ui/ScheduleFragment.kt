package edu.muj.studentportal.ui

import edu.muj.studentportal.data.PortalSnapshot

class ScheduleFragment : BaseListFragment() {
    override fun render(s: PortalSnapshot): List<PortalRow> = buildList {
        host.title("Weekly timetable")
        add(heading("intro", "Your teaching week".takeIf { s.user.isTeacher } ?: "Your study week", "Recurring class times · Section 5H"))
        for (day in 2..6) {
            add(heading("day$day", weekdayLabel(day)))
            val slots = s.schedule.filter { it.weekday == day }
            if (slots.isEmpty()) add(PortalRow("empty$day", "No scheduled classes"))
            slots.forEach { slot -> val course = s.course(slot.courseId)
                add(PortalRow("slot${slot.id}", course.name, "${slot.room}\n${s.person(course.teacherId).name}",
                    "${slot.start}–${slot.end} · ${course.code}", "Open course", click = { host.open(CourseFragment.create(course.id)) })) }
        }
    }
}
