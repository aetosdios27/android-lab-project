package edu.muj.studentportal.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.muj.studentportal.R
import edu.muj.studentportal.databinding.ItemHeadingBinding
import edu.muj.studentportal.databinding.ItemRowBinding

data class PortalRow(val key: String, val title: String, val body: String = "", val meta: String = "",
    val badge: String = "", val heading: Boolean = false, val attention: Boolean = false,
    val click: (() -> Unit)? = null)

class PortalRows : ListAdapter<PortalRow, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<PortalRow>() {
    override fun areItemsTheSame(a: PortalRow, b: PortalRow) = a.key == b.key
    override fun areContentsTheSame(a: PortalRow, b: PortalRow) = a == b
}) {
    override fun getItemViewType(position: Int) = if (getItem(position).heading) 1 else 0
    override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (type == 1) Heading(ItemHeadingBinding.inflate(inflater, parent, false))
            else Card(ItemRowBinding.inflate(inflater, parent, false))
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val row = getItem(position)
        when (holder) {
            is Heading -> { holder.binding.title.text = row.title; holder.binding.body.text = row.body
                holder.binding.body.visibility = if (row.body.isBlank()) View.GONE else View.VISIBLE
                ViewCompat.setAccessibilityHeading(holder.binding.title, true) }
            is Card -> holder.binding.apply {
                title.text = row.title; body.text = row.body; meta.text = row.meta; badge.text = row.badge
                body.visibility = if (row.body.isBlank()) View.GONE else View.VISIBLE
                meta.visibility = if (row.meta.isBlank()) View.GONE else View.VISIBLE
                badge.visibility = if (row.badge.isBlank()) View.GONE else View.VISIBLE
                val context = root.context
                rowCard.setCardBackgroundColor(ContextCompat.getColor(context, if (row.attention) R.color.attention_surface else R.color.surface))
                badge.setTextColor(ContextCompat.getColor(context, if (row.attention) R.color.attention else R.color.primary))
                root.isClickable = row.click != null; root.isFocusable = row.click != null
                root.setOnClickListener(if (row.click != null) View.OnClickListener { row.click.invoke() } else null)
            }
        }
    }
    private class Heading(val binding: ItemHeadingBinding) : RecyclerView.ViewHolder(binding.root)
    private class Card(val binding: ItemRowBinding) : RecyclerView.ViewHolder(binding.root)
}
