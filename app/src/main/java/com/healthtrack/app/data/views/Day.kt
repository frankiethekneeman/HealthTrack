package com.healthtrack.app.data.views

import com.healthtrack.app.data.models.HistoricalEvent
import java.time.Instant

data class Day(
    val startOfDay: Instant,
    val events: List<HistoricalEvent> = listOf()
)
