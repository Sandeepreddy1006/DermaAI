package com.simats.dermacareai.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.dermacareai.R
import com.simats.dermacareai.network.models.AnalysisResponse

class HistoryAdapter(
    private var items: MutableList<AnalysisResponse>,
    private val onItemClick: (AnalysisResponse) -> Unit,
    private val onDeleteClick: (AnalysisResponse, Int) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvResult: TextView = view.findViewById(R.id.tvResult)
        val ivDelete: android.widget.ImageView = view.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.result_title
        holder.tvDate.text = item.created_at.substring(0, 10)
        holder.tvResult.text = "Confidence: ${item.confidence_score}%"
        
        holder.ivDelete.setOnClickListener {
            onDeleteClick(item, position)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, items.size)
    }

    override fun getItemCount() = items.size
}
