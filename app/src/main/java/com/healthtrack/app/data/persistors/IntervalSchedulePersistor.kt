package com.healthtrack.app.data.persistors

import com.healthtrack.app.data.daos.schedule.IntervalScheduleDao
import com.healthtrack.app.data.daos.schedule.TimeOneTimeScheduleDao
import com.healthtrack.app.data.daos.schedule.TriggeredOneTimeScheduleDao
import com.healthtrack.app.data.models.schedule.IntervalSchedule
import com.healthtrack.app.data.models.schedule.OneTimeScheduleVisitor
import com.healthtrack.app.data.models.schedule.TimeOneTimeSchedule
import com.healthtrack.app.data.models.schedule.TriggeredOneTimeSchedule
import javax.inject.Inject

class IntervalSchedulePersistor @Inject constructor(
    private val intervalScheduleDao: IntervalScheduleDao,
    private val triggeredOneTimeScheduleDao: TriggeredOneTimeScheduleDao,
    private val timeOneTimeScheduleDao: TimeOneTimeScheduleDao
) {

    fun persist(schedule: IntervalSchedule): Long {
        schedule.id = intervalScheduleDao.insert(schedule)
        if (schedule.startingSchedule != null) {
            schedule.startingSchedule!!.interventionId = schedule.interventionId
            schedule.startingSchedule!!.parentScheduleId = schedule.id
            schedule.startingSchedule!!.id = schedule.startingSchedule!!
                .acceptOneTimeVisitor(object: OneTimeScheduleVisitor<Long> {
                    override fun visit(visited: TimeOneTimeSchedule): Long {
                        return timeOneTimeScheduleDao.insert(visited)
                    }

                    override fun visit(visited: TriggeredOneTimeSchedule): Long {
                        return triggeredOneTimeScheduleDao.insert(visited)
                    }
                })
        }
        return schedule.id!!

    }

}