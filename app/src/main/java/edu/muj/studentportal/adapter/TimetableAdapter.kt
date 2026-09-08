package edu.muj.studentportal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.muj.studentportal.R
import edu.muj.studentportal.databinding.ItemTimetableBinding
import edu.muj.studentportal.model.TimetableItem

class TimetableAdapter(private var items: List<TimetableItem> = emptyList()) : RecyclerView.Adapter<TimetableAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemTimetableBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemTimetableBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val item = items[position]
        val context = binding.root.context
        binding.eyebrow.text = context.getString(R.string.academic_time, item.startTime, item.endTime)
        binding.title.text = item.subject
        binding.detail.text = context.getString(R.string.academic_room, item.room, item.faculty)
        binding.status.visibility = android.view.View.GONE
    }

    fun showDay(schedule: List<TimetableItem>) {
        items = schedule
        notifyDataSetChanged()
    }
}
