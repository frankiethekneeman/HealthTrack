package com.healthtrack.app.data.models.schedule

interface OneTimeScheduleVisitor<out T> {
    fun visit(visited: TimeOneTimeSchedule): T
    fun visit(visited: TriggeredOneTimeSchedule): T


}
