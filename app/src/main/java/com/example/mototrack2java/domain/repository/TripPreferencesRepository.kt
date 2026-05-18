package com.example.mototrack2java.domain.repository

import com.example.mototrack2java.domain.model.AppLocation
import com.example.mototrack2java.domain.model.TripMode
import com.example.mototrack2java.domain.model.TripSettings
import kotlinx.coroutines.flow.Flow

interface TripPreferencesRepository {
    val settings: Flow<TripSettings>

    suspend fun setDestination(destination: String)
    suspend fun setTripActive(isActive: Boolean)
    suspend fun setMode(mode: TripMode)
    suspend fun setVoiceEnabled(enabled: Boolean)
    suspend fun setNotificationEnabled(enabled: Boolean)
    suspend fun setCurrentMotoId(id: Long)
    suspend fun setNearbyMotoCount(count: Int)
    suspend fun setLocation(location: AppLocation)
    suspend fun clearTrip()
}
