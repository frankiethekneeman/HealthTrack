package com.healthtrack.app.data.daos.schedule

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.healthtrack.app.data.models.schedule.TriggeredOneTimeSchedule

@Dao
interface TriggeredOneTimeScheduleDao: ScheduleDao<TriggeredOneTimeSchedule> {
    @Query("SELECT * FROM TriggeredOneTimeSchedule WHERE intervention_id IN (:interventionIds) ")
    override fun getForInterventions(interventionIds: Set<Long>): LiveData<List<TriggeredOneTimeSchedule>>

    @Insert
    fun insert(s: TriggeredOneTimeSchedule): Long
}