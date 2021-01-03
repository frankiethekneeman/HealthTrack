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
    indices = [Index(value=["id"]), Index(value=["intervention_id"])],
    foreignKeys = [ForeignKey(entity = Intervention::class,
        parentColumns = ["id"],
        childColumns = ["intervention_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
class TriggeredSchedule(
    interventionId: Long,
    @ColumnInfo(name="trigger")
    val trigger: Trigger,
    @ColumnInfo(name="delay")
    val delay: Duration
): Schedule(interventionId) {
    override fun getNextOccurrence(
        history: List<HistoricalEvent>, clock: Clock, since: Instant
    ): LocalDateTime? {
        val lastTrigger =  history.firstOrNull{
            trigger == it.eventType || (trigger == Trigger.MEAL && Trigger.MEALS.contains(it.eventType))
        }?: return null
        if(getPrevOccurrence(history)?.occurrence?.isAfter(lastTrigger.occurrence) == true) {
            return null
        }

        return LocalDateTime.ofInstant(lastTrigger.occurrence, clock.zone).plus(delay)
    }

    override fun <T> accept(visitor: ScheduleVisitor<T>): T {
        return visitor.visit(this)
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

    override fun toString(): String {
        return "($interventionId) ${delay.toMinutes()} minutes after each time ${trigger.displayName} [id: $id]"
    }
}