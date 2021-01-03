package com.healthtrack.app.data.models.schedule

import androidx.room.ColumnInfo
import com.healthtrack.app.data.models.HistoricalEvent
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime

abstract class OneTimeSchedule(
    interventionId: Long,
    @ColumnInfo(name = "parent_schedule_id")
    var parentScheduleId: Long?
): Schedule(interventionId) {
    final override fun getNextOccurrence(
        history: List<HistoricalEvent>,
        clock: Clock,
        since: Instant
    ): LocalDateTime? {
        val prev = getPrevOccurrence(history)
        return if (prev != null) {
            //This has already happened today, so... don't.
            null
        } else {
            getDailyOccurrence(history, clock, since)
        }
    }

    final override fun <T> accept(visitor: ScheduleVisitor<T>): T {
        return this.acceptOneTimeVisitor(visitor)
    }

    abstract fun <T> acceptOneTimeVisitor(visitor: OneTimeScheduleVisitor<T>): T

    abstract fun getDailyOccurrence(
        history: List<HistoricalEvent>,
        clock: Clock,
        since: Instant
    ): LocalDateTime?

    abstract fun getTriggerTime(history: List<HistoricalEvent>, clock: Clock, since: Instant): Instant?
}
