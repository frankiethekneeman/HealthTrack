package com.healthtrack.app.activities.newintervention.viewholders

import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import com.healthtrack.app.R
import com.healthtrack.app.data.models.Intervention
import com.healthtrack.app.data.models.Trigger
import com.healthtrack.app.data.models.schedule.Schedule
import com.healthtrack.app.data.models.schedule.TriggeredSchedule
import java.time.Duration

open class NewTriggeredHolder(private val view: View): NewScheduleViewHolder(view) {
    private val triggers: Spinner = view.findViewById(R.id.eventTrigger)

    override fun bind() {
        triggers.adapter = ArrayAdapter(
            view.context,
            R.layout.support_simple_spinner_dropdown_item,
            Trigger.TRIGGERABLE.map { it.displayName }.toTypedArray()
        )
    }

    override fun getSchedule(intervention: Intervention): Schedule {
        return TriggeredSchedule(
            intervention.id ?: 0,
            getTrigger(),
            getDuration()
        )
    }
    protected fun getTrigger(): Trigger {
        return Trigger.TRIGGERABLE[triggers.selectedItemPosition]
    }
    protected fun getDuration(): Duration {
        return Duration.ofHours(asLong(view.findViewById<EditText>(R.id.delay_hours).text.toString()))
            .plusMinutes(asLong(view.findViewById<EditText>(R.id.delay_minutes).text.toString()))
    }
}