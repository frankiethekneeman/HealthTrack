package com.healthtrack.app.activities.newintervention.viewholders

import android.view.View
import com.healthtrack.app.data.models.Intervention
import com.healthtrack.app.data.models.schedule.Schedule
import com.healthtrack.app.data.models.schedule.TriggeredOneTimeSchedule

class NewTriggeredOneTime(view: View): NewTriggeredHolder(view) {
    override fun getSchedule(intervention: Intervention): Schedule {
        return TriggeredOneTimeSchedule(
            intervention.id ?: 0,
            null,
            getTrigger(),
            getDuration()
        )
    }
}