package com.healthtrack.app.activities.newintervention.viewholders

import android.view.View
import android.widget.TimePicker
import com.healthtrack.app.R
import com.healthtrack.app.data.models.Intervention
import com.healthtrack.app.data.models.schedule.Schedule
import com.healthtrack.app.data.models.schedule.TimeOneTimeSchedule
import java.time.LocalTime

class NewTimeOneTimeHolder(private val view: View): NewScheduleViewHolder(view) {
    override fun bind() {
        //Nothing to bind, android does all the work.
    }

    override fun getSchedule(intervention: Intervention): Schedule {
        val timePicker = view.findViewById<TimePicker>(R.id.timePicker)
        return TimeOneTimeSchedule(
            intervention.id ?: 0,
            null,
            LocalTime.of(timePicker.hour, timePicker.minute)
        )
    }
}