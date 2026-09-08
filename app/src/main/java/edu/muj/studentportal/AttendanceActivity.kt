package edu.muj.studentportal

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import edu.muj.studentportal.adapter.AttendanceAdapter
import edu.muj.studentportal.data.MockData
import edu.muj.studentportal.databinding.ActivityAttendanceBinding
import kotlin.math.roundToInt

class AttendanceActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        val binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setTitle(R.string.academic_attendance)
        configureToolbar(binding.toolbar)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = AttendanceAdapter(MockData.attendance)

        val attended = MockData.attendance.sumOf { it.attendedClasses }
        val total = MockData.attendance.sumOf { it.totalClasses }
        val percentage = if (total > 0) (attended * 100.0 / total).roundToInt() else 0
        binding.summary.text = getString(R.string.academic_overall, percentage)
    }
}
