package com.healthtrack.app.data.models.schedule

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.healthtrack.app.data.models.HistoricalEvent
import com.healthtrack.app.data.models.Intervention
import java.time.*

@Entity(
    indices = [Index(value=["id"]), Index(value=["intervention_id"]), Index(value = ["parent_schedule_id"])],
    foreignKeys = [ForeignKey(entity = Intervention::class,
        parentColumns = ["id"],
        childColumns = ["intervention_id"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(entity = IntervalSchedule::class,
        parentColumns = ["id"],
        childColumns = ["parent_schedule_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
class TimeOneTimeSchedule(
    interventionId: Long,
    parentScheduleId: Long?,
    @ColumnInfo(name = "time")
    val time: LocalTime
): OneTimeSchedule(interventionId, parentScheduleId) {
    override fun <T> acceptOneTimeVisitor(visitor: OneTimeScheduleVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun getDailyOccurrence(history: List<HistoricalEvent>, clock: Clock, since: Instant): LocalDateTime {
        val cutoff = LocalDateTime.ofInstant(since, clock.zone)
        val today = time.atDate(LocalDate.now(clock))
        return if (today.isBefore(cutoff)) {
            time.atDate(LocalDate.now(clock).plusDays(1))
        } else {
            today
        }
    }

    override fun getTriggerTime(
        history: List<HistoricalEvent>,
        clock: Clock,
        since: Instant
    ): Instant {
        val target = getDailyOccurrence(history, clock, since)
        return target.toInstant(clock.zone.rules.getOffset(target))
    }

    override fun description(): String {
        return "At $time."
    }

    override fun toString(): String {
        return "($interventionId) Daily at $time [id: $id, parentScheduleId: $parentScheduleId]"
    }
}