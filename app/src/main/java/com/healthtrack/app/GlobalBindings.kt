package com.healthtrack.app

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import java.time.Clock

@Module
@InstallIn(ApplicationComponent::class)
class GlobalBindings {

    @Provides
    fun clock(): Clock {
        return Clock.systemDefaultZone()
    }
}