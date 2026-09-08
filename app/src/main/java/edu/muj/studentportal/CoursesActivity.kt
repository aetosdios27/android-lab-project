package edu.muj.studentportal

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import edu.muj.studentportal.adapter.CourseAdapter
import edu.muj.studentportal.data.MockData
import edu.muj.studentportal.databinding.ActivityCoursesBinding

class CoursesActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        val binding = ActivityCoursesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setTitle(R.string.academic_courses)
        configureToolbar(binding.toolbar)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = CourseAdapter(MockData.courses)
    }
}
