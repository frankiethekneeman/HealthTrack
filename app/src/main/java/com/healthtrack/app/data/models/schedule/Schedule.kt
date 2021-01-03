package com.healthtrack.app.data.models.schedule

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.healthtrack.app.data.models.HistoricalEvent
import com.healthtrack.app.data.models.Trigger
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime

abstract class Schedule(@ColumnInfo(name="intervention_id") var interventionId: Long?) {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
    /**
     * Get the next occurrence of this schedule item, if one exists.
     *
     * @param history A list of historical events sorted from most recent to least recent - limited to the current day's events.
     */
    abstract fun getNextOccurrence(
        history: List<HistoricalEvent>,
        clock: Clock,
        since: Instant
    ): LocalDateTime?
    abstract fun <T> accept(visitor: ScheduleVisitor<T>): T
    abstract fun description(): String

    fun getPrevOccurrence(history: List<HistoricalEvent>): HistoricalEvent? {
        return history.firstOrNull { it.eventType == Trigger.INTERVENTION && it.interventionId == interventionId }
    }

}
