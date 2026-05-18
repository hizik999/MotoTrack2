package com.example.mototrack2java.testutil

import com.example.mototrack2java.domain.model.AppLocation
import com.example.mototrack2java.domain.model.Moto
import com.example.mototrack2java.domain.model.TripMode
import com.example.mototrack2java.domain.model.TripSettings
import com.example.mototrack2java.domain.repository.LocationRepository
import com.example.mototrack2java.domain.repository.MotoRepository
import com.example.mototrack2java.domain.repository.TripPreferencesRepository
import com.example.mototrack2java.domain.service.NotificationController
import com.example.mototrack2java.domain.service.SoundPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTripPreferencesRepository(
    initialSettings: TripSettings = TripSettings()
) : TripPreferencesRepository {

    private val state = MutableStateFlow(initialSettings)
    override val settings: Flow<TripSettings> = state.asStateFlow()

    val currentSettings: TripSettings get() = state.value

    override suspend fun setDestination(destination: String) {
        state.value = state.value.copy(destination = destination)
    }

    override suspend fun setTripActive(isActive: Boolean) {
        state.value = state.value.copy(isTripActive = isActive)
    }

    override suspend fun setMode(mode: TripMode) {
        state.value = state.value.copy(mode = mode)
    }

    override suspend fun setVoiceEnabled(enabled: Boolean) {
        state.value = state.value.copy(voiceEnabled = enabled)
    }

    override suspend fun setNotificationEnabled(enabled: Boolean) {
        state.value = state.value.copy(notificationEnabled = enabled)
    }

    override suspend fun setCurrentMotoId(id: Long) {
        state.value = state.value.copy(currentMotoId = id)
    }

    override suspend fun setNearbyMotoCount(count: Int) {
        state.value = state.value.copy(nearbyMotoCount = count)
    }

    override suspend fun setLocation(location: AppLocation) {
        state.value = state.value.copy(location = location)
    }

    override suspend fun clearTrip() {
        state.value = state.value.copy(
            destination = "",
            isTripActive = false,
            currentMotoId = -1L,
            nearbyMotoCount = 0
        )
    }
}

class FakeMotoRepository(
    var nearbyMotos: List<Moto> = emptyList(),
    var addedMotoId: Long = 100L
) : MotoRepository {

    var refreshLocation: AppLocation? = null
    var addedLocation: AppLocation? = null
    var updatedMoto: Pair<Long, AppLocation>? = null
    var deletedMotoId: Long? = null
    var cachedMotos: List<Moto> = emptyList()
    var throwOnRefresh: Boolean = false
    var throwOnDelete: Boolean = false

    override suspend fun refreshNearbyMotos(userLocation: AppLocation): List<Moto> {
        refreshLocation = userLocation
        if (throwOnRefresh) error("refresh failed")
        return nearbyMotos
    }

    override suspend fun addMoto(location: AppLocation): Long {
        addedLocation = location
        return addedMotoId
    }

    override suspend fun updateMoto(id: Long, location: AppLocation) {
        updatedMoto = id to location
    }

    override suspend fun deleteMoto(id: Long) {
        deletedMotoId = id
        if (throwOnDelete) error("delete failed")
    }

    override suspend fun getCachedMotos(): List<Moto> = cachedMotos
}

class FakeLocationRepository(
    var currentLocation: AppLocation? = null
) : LocationRepository {

    var requestCount = 0

    override suspend fun getCurrentLocation(): AppLocation? {
        requestCount += 1
        return currentLocation
    }
}

class FakeNotificationController : NotificationController {
    val shownModes = mutableListOf<TripMode>()
    var cancelCount = 0

    override fun showTripNotification(mode: TripMode) {
        shownModes += mode
    }

    override fun cancelTripNotification() {
        cancelCount += 1
    }
}

class FakeSoundPlayer : SoundPlayer {
    var playCount = 0

    override fun playMotoDetected() {
        playCount += 1
    }
}
