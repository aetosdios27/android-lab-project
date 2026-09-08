package edu.muj.studentportal

import android.content.Intent
import android.os.Bundle
import edu.muj.studentportal.databinding.ActivityDashboardBinding

class DashboardActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        val binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.attendanceButton.setOnClickListener { startActivity(Intent(this, AttendanceActivity::class.java)) }
        binding.marksButton.setOnClickListener { startActivity(Intent(this, MarksActivity::class.java)) }
        binding.timetableButton.setOnClickListener { startActivity(Intent(this, TimetableActivity::class.java)) }
        binding.coursesButton.setOnClickListener { startActivity(Intent(this, CoursesActivity::class.java)) }
        binding.noticesButton.setOnClickListener { startActivity(Intent(this, NoticesActivity::class.java)) }
        binding.serviceRequestButton.setOnClickListener { startActivity(Intent(this, ServiceRequestActivity::class.java)) }
        binding.profileButton.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        binding.upcomingCard.setOnClickListener { startActivity(Intent(this, TimetableActivity::class.java)) }
        binding.noticeCard.setOnClickListener { startActivity(Intent(this, NoticesActivity::class.java)) }
    }
}
