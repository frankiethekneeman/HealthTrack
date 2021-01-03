package com.healthtrack.app.data

import androidx.room.TypeConverter
import com.healthtrack.app.data.models.Trigger
import java.time.Duration
import java.time.Instant
import java.time.LocalTime

@Suppress("unused") // These are used by the Room DB to convert to/from database representations
class Converters {
    @TypeConverter
    fun durationToLong(d: Duration): Long {
        return d.toNanos()
    }

    @TypeConverter
    fun fromLong(nanos: Long): Duration {
        return Duration.ofNanos(nanos)
    }

    @TypeConverter
    fun triggerToName(t: Trigger): String {
        return t.name
    }

    @TypeConverter
    fun fromName(name: String): Trigger {
        return Trigger.valueOf(name)
    }

    @TypeConverter
    fun localTimeToNanos(t: LocalTime): Long {
        return t.toNanoOfDay()
    }

    @TypeConverter
    fun toLocalTime(nanos: Long): LocalTime {
        return LocalTime.ofNanoOfDay(nanos)
    }

    @TypeConverter
    fun instantToNanos(t: Instant): Long {
        return t.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(millis: Long): Instant {
        return Instant.ofEpochMilli(millis)
    }
}