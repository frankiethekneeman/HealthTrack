package com.healthtrack.app.data.daos.schedule

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.healthtrack.app.data.models.schedule.TimeOneTimeSchedule

@Dao
interface TimeOneTimeScheduleDao: ScheduleDao<TimeOneTimeSchedule> {
    @Query("SELECT * FROM TimeOneTimeSchedule WHERE intervention_id IN (:interventionIds) ")
    override fun getForInterventions(interventionIds: Set<Long>): LiveData<List<TimeOneTimeSchedule>>

    @Insert
    fun insert(s: TimeOneTimeSchedule): Long
}
