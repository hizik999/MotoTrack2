package com.example.mototrack2java.di

import com.example.mototrack2java.data.local.MotoLocalDataSource
import com.example.mototrack2java.data.local.SqliteMotoLocalDataSource
import com.example.mototrack2java.data.location.FusedLocationRepository
import com.example.mototrack2java.data.notification.AndroidNotificationController
import com.example.mototrack2java.data.preferences.SharedPreferencesTripPreferencesRepository
import com.example.mototrack2java.data.remote.MotoRemoteDataSource
import com.example.mototrack2java.data.remote.VolleyMotoRemoteDataSource
import com.example.mototrack2java.data.repository.DefaultMotoRepository
import com.example.mototrack2java.data.sound.AndroidSoundPlayer
import com.example.mototrack2java.domain.repository.LocationRepository
import com.example.mototrack2java.domain.repository.MotoRepository
import com.example.mototrack2java.domain.repository.TripPreferencesRepository
import com.example.mototrack2java.domain.service.DistanceCalculator
import com.example.mototrack2java.domain.service.NotificationController
import com.example.mototrack2java.domain.service.SoundPlayer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindMotoLocalDataSource(implementation: SqliteMotoLocalDataSource): MotoLocalDataSource

    @Binds
    abstract fun bindMotoRemoteDataSource(implementation: VolleyMotoRemoteDataSource): MotoRemoteDataSource

    @Binds
    abstract fun bindMotoRepository(implementation: DefaultMotoRepository): MotoRepository

    @Binds
    abstract fun bindTripPreferencesRepository(
        implementation: SharedPreferencesTripPreferencesRepository
    ): TripPreferencesRepository

    @Binds
    abstract fun bindLocationRepository(implementation: FusedLocationRepository): LocationRepository

    @Binds
    abstract fun bindNotificationController(implementation: AndroidNotificationController): NotificationController

    @Binds
    abstract fun bindSoundPlayer(implementation: AndroidSoundPlayer): SoundPlayer

    companion object {
        @Provides
        fun provideDistanceCalculator(): DistanceCalculator = DistanceCalculator()
    }
}
