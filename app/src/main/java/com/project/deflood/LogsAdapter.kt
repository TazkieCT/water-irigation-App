package com.project.deflood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.deflood.data.LogEntry
import com.project.deflood.data.LogGroup

class LogsAdapter(private val groups: MutableList<LogGroup>) : RecyclerView.Adapter<LogsAdapter.GroupViewHolder>() {

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textPumpHeader: TextView = itemView.findViewById(R.id.textPumpHeader)
        val textTimeRange: TextView = itemView.findViewById(R.id.textTimeRange)
        val textAvgLevelA: TextView = itemView.findViewById(R.id.textAvgLevelA)
        val textAvgLevelB: TextView = itemView.findViewById(R.id.textAvgLevelB)
        val textHighLevelA: TextView = itemView.findViewById(R.id.textHighLevelA)
        val textHighLevelB: TextView = itemView.findViewById(R.id.textHighLevelB)
        val textLowLevelA: TextView = itemView.findViewById(R.id.textLowLevelA)
        val textLowLevelB: TextView = itemView.findViewById(R.id.textLowLevelB)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log_group_header, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.textPumpHeader.text = "Pump: ${group.pump}"
        holder.textTimeRange.text = "${group.startTime} - ${group.endTime}"
        
        val avgLevelA = group.logs.map { it.levelA }.average()
        val avgLevelB = group.logs.map { it.levelB }.average()
        holder.textAvgLevelA.text = "Avg Level A: ${(avgLevelA * 100).toInt()}%"
        holder.textAvgLevelB.text = "Avg Level B: ${(avgLevelB * 100).toInt()}%"
        
        val highLevelA = group.logs.maxOfOrNull { it.levelA } ?: 0.0
        val lowLevelA = group.logs.minOfOrNull { it.levelA } ?: 0.0
        val highLevelB = group.logs.maxOfOrNull { it.levelB } ?: 0.0
        val lowLevelB = group.logs.minOfOrNull { it.levelB } ?: 0.0
        
        holder.textHighLevelA.text = "High Level A: ${(highLevelA * 100).toInt()}%"
        holder.textHighLevelB.text = "High Level B: ${(highLevelB * 100).toInt()}%"
        holder.textLowLevelA.text = "Low Level A: ${(lowLevelA * 100).toInt()}%"
        holder.textLowLevelB.text = "Low Level B: ${(lowLevelB * 100).toInt()}%"
    }

    override fun getItemCount(): Int = groups.size

    fun updateLogs(newLogs: List<LogEntry>) {
        groups.clear()
        if (newLogs.isEmpty()) {
            notifyDataSetChanged()
            return
        }

        val sortedLogs = newLogs.sortedBy { it.waktu }
        var currentGroup: MutableList<LogEntry>? = null
        var currentPump = -1

        for (log in sortedLogs) {
            if (currentPump != log.pump) {
                if (currentGroup != null && currentGroup.isNotEmpty()) {
                    groups.add(createGroup(currentGroup, currentPump))
                }
                currentGroup = mutableListOf()
                currentPump = log.pump
            }
            currentGroup?.add(log)
        }

        if (currentGroup != null && currentGroup.isNotEmpty()) {
            groups.add(createGroup(currentGroup, currentPump))
        }

        notifyDataSetChanged()
    }

    private fun createGroup(logs: List<LogEntry>, pump: Int): LogGroup {
        val startTime = extractTime(logs.first().waktu)
        val endTime = extractTime(logs.last().waktu)
        return LogGroup(pump, startTime, endTime, logs)
    }

    private fun extractTime(dateTime: String): String {
        return if (dateTime.contains(" ")) {
            dateTime.split(" ")[1]
        } else {
            dateTime
        }
    }
}

