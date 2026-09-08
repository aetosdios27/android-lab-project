package edu.muj.studentportal

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import edu.muj.studentportal.adapter.MarksAdapter
import edu.muj.studentportal.data.MockData
import edu.muj.studentportal.databinding.ActivityMarksBinding

class MarksActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        val binding = ActivityMarksBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setTitle(R.string.academic_marks)
        configureToolbar(binding.toolbar)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = MarksAdapter(MockData.marks)
    }
}
