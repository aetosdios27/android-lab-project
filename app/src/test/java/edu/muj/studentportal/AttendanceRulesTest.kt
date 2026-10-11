package edu.muj.studentportal

import edu.muj.studentportal.ui.attendanceAtRisk
import edu.muj.studentportal.ui.attendanceSummary
import org.junit.Assert.*
import org.junit.Test

class AttendanceRulesTest {
    @Test fun thresholdUsesExactFractionAndHandlesNoSessions() {
        assertFalse(attendanceAtRisk(0, 0))
        assertFalse(attendanceAtRisk(3, 4))
        assertTrue(attendanceAtRisk(74, 99))
        assertFalse(attendanceAtRisk(75, 100))
        assertEquals("No sessions recorded", attendanceSummary(0, 0))
    }
}
