package com.healthtrack.app.data.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.healthtrack.app.data.HealthTrackDatabase
import com.healthtrack.app.data.models.Intervention
import com.healthtrack.app.data.models.schedule.*

@Dao
interface InterventionDao {
    @Insert
    fun insert(intervention: Intervention): Long
    @Query("SELECT * FROM intervention")
    fun getAll(): LiveData<List<Intervention>>
    @Query("SELECT * FROM intervention WHERE id = :id")
    fun get(id: Long): Intervention
    @Delete
    fun delete(intervention: Intervention)

    @Transaction
    fun save(intervention: Intervention, db: HealthTrackDatabase): Long {
        intervention.id =  insert(intervention)
        intervention.schedules.forEach {
            it.interventionId = intervention.id
            it.id = it.accept(object: ScheduleVisitor<Long> {
                override fun visit(visited: IntervalSchedule): Long {
                    return db.intervalScheduleDao().save(visited, db)
                }

                override fun visit(visited: TriggeredSchedule): Long {
                    return db.triggeredScheduleDao().insert(visited)
                }

                override fun visit(visited: TimeOneTimeSchedule): Long {
                    return db.timeOneTimeScheduleDao().insert(visited)
                }

                override fun visit(visited: TriggeredOneTimeSchedule): Long {
                    return db.triggeredOneTimeScheduleDao().insert(visited)
                }
            })
        }
        return intervention.id !!
    }
}

