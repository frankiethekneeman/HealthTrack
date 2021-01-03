package com.healthtrack.app.data.daos.schedule

import androidx.lifecycle.LiveData
import com.healthtrack.app.data.models.schedule.Schedule

interface ScheduleDao<T: Schedule> {
    fun getForInterventions(interventionIds: Set<Long>): LiveData<List<T>>
}
