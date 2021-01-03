package com.healthtrack.app.activities.configuration

import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.healthtrack.app.R
import com.healthtrack.app.data.models.schedule.IntervalSchedule
import com.healthtrack.app.data.models.schedule.Schedule

class ScheduleBulletsAdapter(private val schedules: List<Schedule>) : RecyclerView.Adapter<ScheduleBulletsAdapter.ScheduleHolder>() {
    class ScheduleHolder(view: View): RecyclerView.ViewHolder(view) {
        val mainDescription: TextView = view.findViewById(R.id.mainDescription)
        val startingScheduleDescription: TextView = view.findViewById(R.id.startingScheduleDescription)
        val startingScheduleView: View = view.findViewById(R.id.startingSchedule)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.schedule_bullet_layout, parent, false)
        return ScheduleHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleHolder, position: Int) {
        val schedule = schedules[position]
        holder.mainDescription.text = schedule.description()
        if (schedule is IntervalSchedule) {
            holder.startingScheduleDescription.text = schedule.startingSchedule?.description()
            holder.startingScheduleView.visibility = VISIBLE
        }
    }

    override fun getItemCount() = schedules.size
}
