package com.healthtrack.app.data.daos.schedule

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.healthtrack.app.data.models.schedule.TriggeredSchedule

@Dao
interface TriggeredScheduleDao: ScheduleDao<TriggeredSchedule> {
    @Query("SELECT * FROM TriggeredSchedule WHERE intervention_id IN (:interventionIds) ")
    override fun getForInterventions(interventionIds: Set<Long>): LiveData<List<TriggeredSchedule>>

    @Insert
    fun insert(s: TriggeredSchedule): Long
}
