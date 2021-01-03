package com.healthtrack.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.healthtrack.app.data.daos.schedule.IntervalScheduleDao
import com.healthtrack.app.data.daos.schedule.TimeOneTimeScheduleDao
import com.healthtrack.app.data.daos.schedule.TriggeredOneTimeScheduleDao
import com.healthtrack.app.data.daos.schedule.TriggeredScheduleDao
import com.healthtrack.app.data.daos.HistoricalEventsDao
import com.healthtrack.app.data.daos.InterventionDao
import com.healthtrack.app.data.models.HistoricalEvent
import com.healthtrack.app.data.models.Intervention
import com.healthtrack.app.data.models.schedule.IntervalSchedule
import com.healthtrack.app.data.models.schedule.TimeOneTimeSchedule
import com.healthtrack.app.data.models.schedule.TriggeredOneTimeSchedule
import com.healthtrack.app.data.models.schedule.TriggeredSchedule

@Database(
    entities = [Intervention::class, IntervalSchedule::class, TriggeredSchedule::class, TriggeredOneTimeSchedule::class, TimeOneTimeSchedule::class, HistoricalEvent::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class HealthTrackDatabase : RoomDatabase() {
    abstract fun interventionDao(): InterventionDao
    abstract fun intervalScheduleDao(): IntervalScheduleDao
    abstract fun triggeredScheduleDao(): TriggeredScheduleDao
    abstract fun triggeredOneTimeScheduleDao(): TriggeredOneTimeScheduleDao
    abstract fun timeOneTimeScheduleDao(): TimeOneTimeScheduleDao
    abstract fun eventsDao(): HistoricalEventsDao
}