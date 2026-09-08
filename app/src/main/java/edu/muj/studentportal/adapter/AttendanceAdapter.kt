package edu.muj.studentportal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.muj.studentportal.R
import edu.muj.studentportal.databinding.ItemAttendanceBinding
import edu.muj.studentportal.model.AttendanceItem
import android.graphics.Color
import kotlin.math.roundToInt

class AttendanceAdapter(private val items: List<AttendanceItem>) : RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemAttendanceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val item = items[position]
        val context = binding.root.context
        binding.eyebrow.text = item.subjectCode
        binding.title.text = item.subjectName
        binding.detail.text = context.getString(R.string.academic_attendance_count, item.attendedClasses, item.totalClasses)
        val percentage = if (item.totalClasses > 0) (item.attendedClasses * 100.0 / item.totalClasses).roundToInt() else 0
        val onTrack = item.totalClasses > 0 && item.attendedClasses.toLong() * 100 >= item.totalClasses.toLong() * 75
        val color = Color.parseColor(if (onTrack) "#147D64" else "#B34B35")
        val status = context.getString(if (onTrack) R.string.academic_safe else R.string.academic_low)
        binding.status.text = context.getString(R.string.academic_attendance_status, percentage, status)
        binding.status.setTextColor(color)
        binding.progress.progress = percentage.coerceIn(0, 100)
        binding.progress.setIndicatorColor(color)
        binding.progress.contentDescription = binding.status.text
    }
}
