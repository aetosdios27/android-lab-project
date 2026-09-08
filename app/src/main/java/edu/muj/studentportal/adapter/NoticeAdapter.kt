package edu.muj.studentportal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.muj.studentportal.R
import edu.muj.studentportal.databinding.ItemNoticeBinding
import edu.muj.studentportal.model.Notice

class NoticeAdapter(private val items: List<Notice>, private val onNoticeClick: (Notice) -> Unit) : RecyclerView.Adapter<NoticeAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemNoticeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val item = items[position]
        val context = binding.root.context
        binding.eyebrow.text = item.category
        binding.title.text = item.title
        binding.detail.text = item.description
        binding.detail.maxLines = 2
        binding.detail.ellipsize = android.text.TextUtils.TruncateAt.END
        binding.status.text = item.date
        binding.root.setOnClickListener { onNoticeClick(item) }
    }
}
