package edu.muj.studentportal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.muj.studentportal.R
import edu.muj.studentportal.databinding.ItemCourseBinding
import edu.muj.studentportal.model.Course

class CourseAdapter(private val items: List<Course>) : RecyclerView.Adapter<CourseAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemCourseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val item = items[position]
        val context = binding.root.context
        binding.eyebrow.text = item.subjectCode
        binding.title.text = item.subjectName
        binding.detail.text = item.faculty
        binding.status.text = context.getString(R.string.academic_credits, item.credits)
    }
}
