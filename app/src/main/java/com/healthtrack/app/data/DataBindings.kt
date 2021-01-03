package com.healthtrack.app.data

import android.content.Context
import androidx.room.Room
import com.healthtrack.app.data.daos.HistoricalEventsDao
import com.healthtrack.app.data.daos.InterventionDao
import com.healthtrack.app.data.daos.schedule.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class DataBindings {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): HealthTrackDatabase {
        return Room.databaseBuilder(
            context,
            HealthTrackDatabase::class.java,
            "health-track"
        ).build()
    }

    @Provides
    fun interventionDao(db: HealthTrackDatabase): InterventionDao {
        return db.interventionDao()
    }

    @Provides
    fun intervalScheduleDao(db: HealthTrackDatabase): IntervalScheduleDao {
        return db.intervalScheduleDao()
    }

    @Provides
    fun timeOneTimeScheduleDao(db: HealthTrackDatabase): TimeOneTimeScheduleDao {
        return db.timeOneTimeScheduleDao()
    }

    @Provides
    fun triggeredOneTimeScheduleDao(db: HealthTrackDatabase): TriggeredOneTimeScheduleDao {
        return db.triggeredOneTimeScheduleDao()
    }

    @Provides
    fun triggeredScheduleDao(db: HealthTrackDatabase): TriggeredScheduleDao {
        return db.triggeredScheduleDao()
    }

    @Provides
    fun eventsDao(db: HealthTrackDatabase): HistoricalEventsDao {
        return db.eventsDao()
    }

    @Provides
    fun scheduleDaos(
        intervalScheduleDao: IntervalScheduleDao,
        triggeredScheduleDao: TriggeredScheduleDao,
        timeOneTimeScheduleDao: TimeOneTimeScheduleDao,
        triggeredOneTimeScheduleDao: TriggeredOneTimeScheduleDao
    ): Set<ScheduleDao<*>> {
        return setOf(
            intervalScheduleDao,
            triggeredScheduleDao,
            timeOneTimeScheduleDao,
            triggeredOneTimeScheduleDao
        )
    }
}