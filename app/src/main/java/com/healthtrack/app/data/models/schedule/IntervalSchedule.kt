package com.healthtrack.app.data.models.schedule

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.healthtrack.app.data.models.HistoricalEvent
import com.healthtrack.app.data.models.Intervention
import java.time.*

@Entity(
    indices = [Index(value=["id"]), Index(value=["intervention_id"])],
    foreignKeys = [ForeignKey(entity = Intervention::class,
        parentColumns = ["id"],
        childColumns = ["intervention_id"],
        onDelete = CASCADE
    )]
)
class IntervalSchedule(
        interventionId: Long,
        @ColumnInfo(name="interval")
        val betweenInstances: Duration
): Schedule(interventionId){
    @Ignore
    var startingSchedule: OneTimeSchedule? = null

    override fun getNextOccurrence(
        history: List<HistoricalEvent>, clock: Clock, since: Instant
    ): LocalDateTime? {
        val triggerTime = startingSchedule?.getTriggerTime(history, clock, since) ?: return null
        val relevantHistory = history.takeWhile { !it.occurrence.isBefore(triggerTime) }
        val prev = this.getPrevOccurrence(relevantHistory)
        return if (prev == null) {
            // Trigger Time may not coincide with schedule time in the case of "delay after trigger",
            // So it's important to delegate.
            startingSchedule?.getNextOccurrence(relevantHistory, clock, since)
        } else {
            LocalDateTime.ofInstant(prev.occurrence, clock.zone).plus(betweenInstances)
        }
    }

    override fun <T> accept(visitor: ScheduleVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun description(): String {
        return "${durationDescription(betweenInstances)}."
    }

    private fun durationDescription(duration: Duration): String {
        if (duration.isZero) {
            return "Constantly"
        } else {
            val hours = duration.toHours()
            val minutes = duration.minusHours(hours).toMinutes()
            if (minutes == 0L) {
                return "Every $hours hours"
            }
            if (hours == 0L) {
                return "Every $minutes minutes"
            }
            return "Every $hours hours and $minutes minutes"
        }
    }

    override fun toString(): String {
        return "($interventionId) Every ${betweenInstances.toMinutes()} minutes starting ${startingSchedule}, [id: $id]"
    }
}