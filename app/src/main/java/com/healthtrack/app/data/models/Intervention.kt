package com.healthtrack.app.data.models

import androidx.room.*
import com.healthtrack.app.data.models.schedule.Schedule

@Entity(
    indices = [Index(value=["id"])]
)
class Intervention (
    @ColumnInfo(name="name")
    val name: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
    @Ignore
    var schedules: List<Schedule> = listOf()

    override fun toString(): String {
        return "$name (${id}) ${schedules.joinToString(",")}"
    }
}