package com.cloudx.demoappjava.loglistview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudx.demoappjava.R
import com.google.android.material.color.MaterialColors
import io.cloudx.sdk.internal.CloudXLogger

class RecyclerViewLogAdapter(recyclerView: RecyclerView) :
    RecyclerView.Adapter<RecyclerViewLogAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val time: TextView = view.findViewById(R.id.text_time)
        val tag: TextView = view.findViewById(R.id.text_tag)
        val msg: TextView = view.findViewById(R.id.text_msg)
    }

    init {
        registerAdapterDataObserver(ScrollToBottomDataObserver(recyclerView))
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.log_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val logListItem = logs[position]
        with(viewHolder) {
            time.text = logListItem.time

            tag.text = logListItem.logItem.tag
            tag.setTextColor(tag.tagTextColor(logListItem))

            var text = logListItem.logItem.msg

            // Handle custom color marker
            val greenMarker = "color=green"
            val redMarker = "color=red"
            val isGreen = text.contains(greenMarker)
            val isRed = text.contains(redMarker)
            if (isGreen) {
                // Remove the marker from text
                text = text.replace(greenMarker, "").trimEnd()
                val greenColor = ContextCompat.getColor(msg.context, R.color.log_success)
                msg.setTextColor(greenColor)
            } else if (isRed) {
                // Remove the marker from text
                text = text.replace(redMarker, "").trimEnd()
                val redColor = ContextCompat.getColor(msg.context, R.color.log_error)
                msg.setTextColor(redColor)
            } else {
                // Optional: fallback to normal color based on type
                msg.setTextColor(ContextCompat.getColor(msg.context, R.color.black))
            }

            msg.text = text
        }
    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return logs.size
    }

    private val logs = mutableListOf<LogListItem>()

    fun addLog(log: LogListItem) {
        logs += log
        notifyItemInserted(logs.size - 1)
    }

    fun addLogs(newLogs: List<LogListItem>) {
        logs += newLogs
        notifyItemRangeInserted(logs.size - newLogs.size, newLogs.size)
    }
}

private fun View.tagTextColor(logListItem: LogListItem) = if (logListItem.isSuccessLog) {
    ContextCompat.getColor(context, R.color.log_success)
} else when (logListItem.logItem.type) {
    CloudXLogger.LogItem.Type.Debug -> MaterialColors.getColor(
        this, com.google.android.material.R.attr.colorOutline
    )

    CloudXLogger.LogItem.Type.Info -> ContextCompat.getColor(context, R.color.log_info)

    CloudXLogger.LogItem.Type.Warn -> ContextCompat.getColor(context, R.color.log_warn)

    CloudXLogger.LogItem.Type.Error -> ContextCompat.getColor(context, R.color.log_error)
}

private class ScrollToBottomDataObserver(
    val recyclerView: RecyclerView
) : RecyclerView.AdapterDataObserver() {

    private val layoutManager = recyclerView.layoutManager as LinearLayoutManager

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)
        val lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()

        if (lastVisiblePosition == -1 || positionStart - lastVisiblePosition <= 2) {
            recyclerView.scrollToPosition(positionStart)
        }
    }
}