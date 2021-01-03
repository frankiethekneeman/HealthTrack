package com.healthtrack.app.data.daos.schedule

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.healthtrack.app.data.HealthTrackDatabase
import com.healthtrack.app.data.models.schedule.IntervalSchedule
import com.healthtrack.app.data.models.schedule.OneTimeScheduleVisitor
import com.healthtrack.app.data.models.schedule.TimeOneTimeSchedule
import com.healthtrack.app.data.models.schedule.TriggeredOneTimeSchedule

@Dao
interface IntervalScheduleDao: ScheduleDao<IntervalSchedule> {
    @Query("SELECT * FROM IntervalSchedule WHERE intervention_id IN (:interventionIds) ")
    override fun getForInterventions(interventionIds: Set<Long>): LiveData<List<IntervalSchedule>>

    @Insert
    fun insert(schedule: IntervalSchedule): Long

    fun save(schedule: IntervalSchedule, db: HealthTrackDatabase): Long {
        schedule.id = insert(schedule)
        if (schedule.startingSchedule != null) {
            schedule.startingSchedule!!.interventionId = schedule.interventionId
            schedule.startingSchedule!!.parentScheduleId = schedule.id
            schedule.startingSchedule!!.id = schedule.startingSchedule!!.acceptOneTimeVisitor(object:
                OneTimeScheduleVisitor<Long> {
                override fun visit(visited: TimeOneTimeSchedule): Long {
                    return db.timeOneTimeScheduleDao().insert(visited)
                }

                override fun visit(visited: TriggeredOneTimeSchedule): Long {
                    return db.triggeredOneTimeScheduleDao().insert(visited)
                }

            })
        }
        return schedule.id!!
    }
}