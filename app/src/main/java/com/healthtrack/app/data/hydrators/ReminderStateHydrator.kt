package com.healthtrack.app.data.hydrators

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.healthtrack.app.data.daos.HistoricalEventsDao
import com.healthtrack.app.data.views.ReminderState
import com.healthtrack.app.data.views.orEmpty
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class ReminderStateHydrator @Inject constructor(
    private val interventionHydrator: InterventionHydrator,
    private val historicalEventsDao: HistoricalEventsDao,
    private val clock: Clock
) {
    fun getReminderState(): LiveData<ReminderState> {
        val defaultYesterday = Instant.now(clock).minus(Duration.ofHours(12))
        val state = MediatorLiveData<ReminderState>()
        state.addSource(interventionHydrator.getAllInterventions()) {
            state.value = state.value.orEmpty(defaultYesterday).copy(interventions = it)
        }
        state.addSource(historicalEventsDao.getViewOfToday(defaultYesterday)) {
            state.value = state.value.orEmpty(defaultYesterday).copy(today = it)
        }
        return state
    }
}