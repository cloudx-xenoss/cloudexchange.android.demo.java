package com.cloudx.demoappjava.loglistview

import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.cloudx.demoappjava.activity.MainActivity.INIT_SUCCESS
import io.cloudx.sdk.internal.CloudXLogger
import io.cloudx.sdk.internal.common.utcNowEpochMillis
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun setupLogListView(
    recyclerView: RecyclerView,
    // null - do not include tag into log list view; otherwise include with a name returned as a func result.
    logTagMappingRule: (forTag: String) -> String?
) {
    val lifecycleOwner = recyclerView.findViewTreeLifecycleOwner()
        ?: throw IllegalStateException("couldn't find view tree lifecycle owner")

    val adapter = RecyclerViewLogAdapter(recyclerView)
    recyclerView.adapter = adapter

    CloudXLogger.logFlow.map { logItem ->
        logTagMappingRule(logItem.tag)?.let { tag ->
            logItem.copy(tag = tag)
        }
    }.onEach { logItem ->
        logItem?.let {
            adapter.addLog(logItem.toLogListItem())
        }
    }.launchIn(lifecycleOwner.lifecycleScope)
}

class LogListItem(
    val time: String,
    // Special highlighting for such logs (initialization success, for example)
    val isSuccessLog: Boolean,
    val logItem: CloudXLogger.LogItem
)

private fun CloudXLogger.LogItem.toLogListItem(): LogListItem {
    return LogListItem(
        time = now(),
        // TODO. As ugly as it gets.
        isSuccessLog = msg == INIT_SUCCESS,
        logItem = this
    )
}

private fun now() = SimpleDateFormat("HH:mm:ss", Locale.ROOT).format(
    Date(utcNowEpochMillis())
)