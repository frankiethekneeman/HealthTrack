package com.healthtrack.app.data.models.schedule

interface ScheduleVisitor<out T>: OneTimeScheduleVisitor<T> {
    fun visit(visited: IntervalSchedule): T
    fun visit(visited: TriggeredSchedule): T
}