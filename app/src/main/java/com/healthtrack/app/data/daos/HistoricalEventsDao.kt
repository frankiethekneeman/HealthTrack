package com.healthtrack.app.data.daos

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.healthtrack.app.data.models.HistoricalEvent
import com.healthtrack.app.data.models.Trigger
import com.healthtrack.app.data.views.Day
import java.time.Instant

@Dao
interface HistoricalEventsDao {
    @Query("SELECT * FROM HistoricalEvent WHERE event_type = :trigger ORDER BY occurrence DESC LIMIT 1")
    fun getLastInstance(trigger: Trigger): LiveData<HistoricalEvent?>
    @Query("SELECT * FROM HistoricalEvent WHERE occurrence >= :cutoff ORDER BY occurrence DESC")
    fun getSince(cutoff: Instant): LiveData<List<HistoricalEvent>>
    @Query("SELECT * FROM HistoricalEvent LIMIT 10000")
    fun getAll(): LiveData<List<HistoricalEvent>>
    @Insert
    fun insert(event: HistoricalEvent): Long

    fun getSinceLastBedtime(): LiveData<List<HistoricalEvent>> {
        return Transformations.switchMap(getLastInstance(Trigger.BED_TIME)) {
            last -> last?.let{getSince(it.occurrence)} ?: getAll()
        }
    }

    fun getViewOfToday(fallBackStartOfDay: Instant): LiveData<Day> {
        return Transformations.switchMap(getLastInstance(Trigger.BED_TIME)) { lastBedTime ->
            Transformations.map(lastBedTime?.let{ getSince(it.occurrence) } ?: getAll()) { events ->
                Day(lastBedTime?.occurrence ?: fallBackStartOfDay, events)
            }
        }
    }
}
