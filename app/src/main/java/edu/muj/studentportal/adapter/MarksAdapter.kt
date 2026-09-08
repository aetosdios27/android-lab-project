package edu.muj.studentportal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.muj.studentportal.R
import edu.muj.studentportal.databinding.ItemMarksBinding
import edu.muj.studentportal.model.MarksItem

class MarksAdapter(private val items: List<MarksItem>) : RecyclerView.Adapter<MarksAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemMarksBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemMarksBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val item = items[position]
        val context = binding.root.context
        binding.eyebrow.text = item.subjectCode
        binding.title.text = item.subjectName
        binding.detail.text = context.getString(R.string.academic_internal, item.internalMarks, item.maxInternalMarks)
        binding.status.text = context.getString(R.string.academic_grade, item.grade)
    }
}
