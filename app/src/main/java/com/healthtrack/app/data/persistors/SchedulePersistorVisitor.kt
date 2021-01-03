package com.healthtrack.app.data.persistors

import com.healthtrack.app.data.daos.schedule.TimeOneTimeScheduleDao
import com.healthtrack.app.data.daos.schedule.TriggeredOneTimeScheduleDao
import com.healthtrack.app.data.daos.schedule.TriggeredScheduleDao
import com.healthtrack.app.data.models.schedule.*
import javax.inject.Inject

class SchedulePersistorVisitor @Inject constructor(
    private val intervalSchedulePersistor: IntervalSchedulePersistor,
    private val triggeredScheduleDao: TriggeredScheduleDao,
    private val timeOneTimeScheduleDao: TimeOneTimeScheduleDao,
    private val triggeredOneTimeScheduleDao: TriggeredOneTimeScheduleDao
): ScheduleVisitor<Long> {
    override fun visit(visited: IntervalSchedule): Long {
        return intervalSchedulePersistor.persist(visited)
    }

    override fun visit(visited: TriggeredSchedule): Long {
        return triggeredScheduleDao.insert(visited)
    }

    override fun visit(visited: TimeOneTimeSchedule): Long {
        return timeOneTimeScheduleDao.insert(visited)
    }

    override fun visit(visited: TriggeredOneTimeSchedule): Long {
        return triggeredOneTimeScheduleDao.insert(visited)
    }
}