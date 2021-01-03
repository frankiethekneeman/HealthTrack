package com.healthtrack.app.data.models.schedule

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.healthtrack.app.data.models.HistoricalEvent
import com.healthtrack.app.data.models.Intervention
import com.healthtrack.app.data.models.Trigger
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

@Entity(
    indices = [Index(value = ["id"]), Index(value = ["intervention_id"]), Index(value = ["parent_schedule_id"])],

    foreignKeys = [ForeignKey(
        entity = Intervention::class,
        parentColumns = ["id"],
        childColumns = ["intervention_id"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = IntervalSchedule::class,
        parentColumns = ["id"],
        childColumns = ["parent_schedule_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
class TriggeredOneTimeSchedule(
    interventionId: Long,
    parentScheduleId: Long?,
    @ColumnInfo(name = "trigger")
    val trigger: Trigger,
    @ColumnInfo(name = "delay")
    val delay: Duration
) : OneTimeSchedule(interventionId, parentScheduleId) {
    override fun <T> acceptOneTimeVisitor(visitor: OneTimeScheduleVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun getDailyOccurrence(
        history: List<HistoricalEvent>,
        clock: Clock,
        since: Instant
    ): LocalDateTime? {
        val lastTrigger = getTrigger(history) ?: return null
        return LocalDateTime.ofInstant(lastTrigger.occurrence, clock.zone).plus(delay)
    }

    override fun getTriggerTime(
        history: List<HistoricalEvent>,
        clock: Clock,
        since: Instant
    ): Instant? {
        return getTrigger(history)?.occurrence
    }

    override fun description(): String {
        return "${delayDescription()} after ${trigger.displayName}."

    }

    private fun delayDescription(): String {
        if (delay.isZero) {
            return "Immediately"
        } else {
            val hours = delay.toHours()
            val minutes = delay.minusHours(hours).toMinutes()
            if (minutes == 0L) {
                return "$hours hours"
            }
            if (hours == 0L) {
                return "$minutes minutes"
            }
            return "$hours hours and $minutes minutes"
        }
    }

    private fun getTrigger(history: List<HistoricalEvent>): HistoricalEvent? {
        if (trigger == Trigger.MEAL) {
            return history.firstOrNull { Trigger.MEALS.contains(it.eventType) }
        }
        return history.firstOrNull { it.eventType == trigger }
    }

    override fun toString(): String {
        return "($interventionId) ${delay.toMinutes()} minutes after the first time ${trigger.displayName} [id: $id, parentScheduleId: $parentScheduleId]"
    }
}