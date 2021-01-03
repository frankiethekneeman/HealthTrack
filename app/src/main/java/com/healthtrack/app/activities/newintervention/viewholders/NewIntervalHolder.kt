package com.healthtrack.app.activities.newintervention.viewholders

import android.view.View
import android.widget.*
import com.healthtrack.app.R
import com.healthtrack.app.data.models.Intervention
import com.healthtrack.app.data.models.Trigger
import com.healthtrack.app.data.models.schedule.*
import java.time.Duration
import java.time.LocalTime

class NewIntervalHolder(private val view: View): NewScheduleViewHolder(view) {
    private val initialCondition: RadioGroup = view.findViewById(R.id.initialCondition)
    private val startTime: TimePicker = view.findViewById(R.id.startTime)
    private val triggerLayout: LinearLayout = view.findViewById(R.id.triggerLayout)
    private val triggers: Spinner = view.findViewById(R.id.eventTrigger)

    override fun bind() {
        triggers.adapter = ArrayAdapter(
            view.context,
            R.layout.support_simple_spinner_dropdown_item,
            Trigger.TRIGGERABLE.map { it.displayName }.toTypedArray()
        )

        initialCondition.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.time) {
                startTime.visibility = View.VISIBLE
                triggerLayout.visibility = View.GONE
            } else {
                startTime.visibility = View.GONE
                triggerLayout.visibility = View.VISIBLE
            }
        }
    }

    override fun getSchedule(intervention: Intervention): Schedule {
        val interval = IntervalSchedule(
            intervention.id ?: 0,
            getInterval()
        )
        interval.startingSchedule = getStartingSchedule(intervention, interval)
        return interval
    }

    private fun getStartingSchedule(intervention: Intervention, parent: IntervalSchedule): OneTimeSchedule {
        return if (initialCondition.checkedRadioButtonId == R.id.time) {
            getTimeStartingSchedule(intervention, parent)
        } else {
            getTriggerStartingSchedule(intervention, parent)
        }

    }

    private fun getTriggerStartingSchedule(
        intervention: Intervention,
        parent: IntervalSchedule
    ): OneTimeSchedule {
        return TriggeredOneTimeSchedule(
            intervention.id ?: 0,
            parent.id,
            Trigger.TRIGGERABLE[triggers.selectedItemPosition],
            Duration.ofHours(asLong(view.findViewById<EditText>(R.id.delay_hours).text.toString()))
                .plusMinutes(asLong(view.findViewById<EditText>(R.id.delay_minutes).text.toString()))
        )
    }

    private fun getTimeStartingSchedule(intervention: Intervention, parent: IntervalSchedule): TimeOneTimeSchedule {
        val timePicker = view.findViewById<TimePicker>(R.id.startTime)
        return TimeOneTimeSchedule(
            intervention.id ?: 0,
            parent.id,
            LocalTime.of(timePicker.hour, timePicker.minute)
        )
    }

    private fun getInterval(): Duration {
        return Duration.ofHours(asLong(view.findViewById<EditText>(R.id.hours).text.toString()))
            .plusMinutes(asLong(view.findViewById<EditText>(R.id.minutes).text.toString()))
    }
}