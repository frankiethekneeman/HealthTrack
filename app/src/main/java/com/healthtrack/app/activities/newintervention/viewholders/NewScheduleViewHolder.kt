package com.healthtrack.app.activities.newintervention.viewholders

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.healthtrack.app.R
import com.healthtrack.app.data.models.Intervention
import com.healthtrack.app.data.models.schedule.Schedule
import java.lang.Long.parseLong

// Why can't I figure out how to set an upper bound on T?
abstract class NewScheduleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    lateinit var onDelete: () -> Unit
    init {
        itemView.findViewById<ImageView>(R.id.delete)?.setOnClickListener {
            onDelete()
        }
    }
    abstract fun bind()

    abstract fun getSchedule(intervention: Intervention): Schedule

    protected fun asLong(str: String): Long {
        if (str.isEmpty()) {
            return 0
        }
        return parseLong(str)
    }
}