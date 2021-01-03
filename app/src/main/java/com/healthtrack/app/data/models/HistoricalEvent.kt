package com.healthtrack.app.data.models

import androidx.room.*
import java.time.Instant

@Entity(
    indices = [Index(value = ["intervention_id"]), Index(value = ["id"])],
    foreignKeys = [ForeignKey(entity = Intervention::class,
        parentColumns = ["id"],
        childColumns = ["intervention_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
class HistoricalEvent(
    @ColumnInfo(name = "occurrence")
    val occurrence: Instant,
    @ColumnInfo(name="event_type")
    val eventType: Trigger,
    @ColumnInfo(name="intervention_id")
    val interventionId: Long?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
}
