package com.healthtrack.app.activities.newintervention

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.healthtrack.app.R
import com.healthtrack.app.activities.newintervention.viewholders.*
import com.healthtrack.app.data.models.Intervention
import com.healthtrack.app.data.models.schedule.*

class NewScheduleAdapter(private val schedules: MutableList<ScheduleType>) : RecyclerView.Adapter<NewScheduleViewHolder>() {
    private val holders: MutableList<NewScheduleViewHolder?> = schedules.map { null }.toMutableList()

    enum class ScheduleType(val displayName: String) {
        INTERVAL("On a cadence"),
        TRIGGERED("With some other event"),
        TIME_ONE_TIME("At a specific time every day"),
        TRIGGERED_ONE_TIME("Once a day, with some other event");
    }

    override fun getItemViewType(position: Int): Int {
        return schedules[position].ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewScheduleViewHolder {
        val inflate = { layout: Int -> LayoutInflater.from(parent.context).inflate(layout, parent, false) }
        return when (ScheduleType.values()[viewType]) {
            ScheduleType.INTERVAL -> NewIntervalHolder(inflate(R.layout.new_interval_shedule_layout))
            ScheduleType.TRIGGERED -> NewTriggeredHolder(inflate(R.layout.new_triggered_schedule_layout))
            ScheduleType.TIME_ONE_TIME -> NewTimeOneTimeHolder(inflate(R.layout.new_time_one_time_schedule_layout))
            ScheduleType.TRIGGERED_ONE_TIME -> NewTriggeredOneTime(inflate(R.layout.new_triggered_one_time_schedule))
        }
    }

    override fun onBindViewHolder(holder: NewScheduleViewHolder, position: Int) {
        holder.bind()
        holders[position] = holder
        holder.onDelete = { removeSchedule(position)}
    }

    override fun getItemCount() = schedules.size

    fun addSchedule(scheduleType: ScheduleType) {
        schedules.add(scheduleType)
        holders.add(null)
        notifyItemInserted(schedules.size - 1)
    }

    fun getSchedules(intervention: Intervention): List<Schedule> {
        return holders.flatMap{ listOfNotNull(it?.getSchedule(intervention)) }
    }

    private fun removeSchedule(position: Int) {
        schedules.removeAt(position)
        notifyItemRemoved(position)
        holders.removeAt(position)
        for (i in position until holders.size) {
            holders[i]?.onDelete = {removeSchedule(i)}
        }
    }

}
