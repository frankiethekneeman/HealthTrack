package com.healthtrack.app.data.views

import com.healthtrack.app.data.models.Intervention
import java.time.Instant

data class ReminderState(
    val today: Day,
    val interventions: List<Intervention> = listOf()
)

fun ReminderState?.orEmpty(startOfDay: Instant): ReminderState {
    return this ?: ReminderState(today = Day(startOfDay = startOfDay))
}
