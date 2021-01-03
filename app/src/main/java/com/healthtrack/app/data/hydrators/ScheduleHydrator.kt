package com.healthtrack.app.data.hydrators

import com.healthtrack.app.data.models.schedule.*

class ScheduleHydrator(schedules: Set<Schedule>): ScheduleVisitor<Schedule> {
    private val subSchedules = schedules.filterIsInstance<OneTimeSchedule>()
        .filter{it.parentScheduleId != null}
        .map{it.parentScheduleId!! to it}
        .toMap()

    override fun visit(visited: IntervalSchedule): Schedule {
        visited.startingSchedule = subSchedules[visited.id]
        return visited
    }

    override fun visit(visited: TriggeredSchedule): Schedule {
        return visited
    }

    override fun visit(visited: TimeOneTimeSchedule): Schedule {
        return visited
    }

    override fun visit(visited: TriggeredOneTimeSchedule): Schedule {
        return visited
    }
}