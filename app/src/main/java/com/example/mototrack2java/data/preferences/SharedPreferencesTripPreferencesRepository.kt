package com.example.mototrack2java.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.mototrack2java.core.config.AppConfig
import com.example.mototrack2java.domain.model.AppLocation
import com.example.mototrack2java.domain.model.TripMode
import com.example.mototrack2java.domain.model.TripSettings
import com.example.mototrack2java.domain.repository.TripPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SharedPreferencesTripPreferencesRepository @Inject constructor(
    @ApplicationContext context: Context
) : TripPreferencesRepository {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(AppConfig.Preferences.NAME, Context.MODE_PRIVATE)
    private val config = AppConfig.Preferences
    private val state = MutableStateFlow(preferences.readSettings())

    override val settings: Flow<TripSettings> = state.asStateFlow()

    override suspend fun setDestination(destination: String) {
        preferences.editAndPublish { putString(config.KEY_DESTINATION, destination) }
    }

    override suspend fun setTripActive(isActive: Boolean) {
        preferences.editAndPublish { putBoolean(config.KEY_TRIP_ACTIVE, isActive) }
    }

    override suspend fun setMode(mode: TripMode) {
        preferences.editAndPublish { putString(config.KEY_MODE, mode.name) }
    }

    override suspend fun setVoiceEnabled(enabled: Boolean) {
        preferences.editAndPublish { putBoolean(config.KEY_VOICE_ENABLED, enabled) }
    }

    override suspend fun setNotificationEnabled(enabled: Boolean) {
        preferences.editAndPublish { putBoolean(config.KEY_NOTIFICATION_ENABLED, enabled) }
    }

    override suspend fun setCurrentMotoId(id: Long) {
        preferences.editAndPublish { putLong(config.KEY_MOTO_ID, id) }
    }

    override suspend fun setNearbyMotoCount(count: Int) {
        preferences.editAndPublish { putInt(config.KEY_MOTO_COUNT, count) }
    }

    override suspend fun setLocation(location: AppLocation) {
        preferences.editAndPublish {
            putFloat(config.KEY_LAT, location.lat)
            putFloat(config.KEY_LON, location.lon)
        }
    }

    override suspend fun clearTrip() {
        preferences.editAndPublish {
            putString(config.KEY_DESTINATION, "")
            putBoolean(config.KEY_TRIP_ACTIVE, false)
            putLong(config.KEY_MOTO_ID, -1L)
            putInt(config.KEY_MOTO_COUNT, 0)
        }
    }

    private fun SharedPreferences.editAndPublish(block: SharedPreferences.Editor.() -> Unit) {
        edit().apply(block).apply()
        state.value = readSettings()
    }

    private fun SharedPreferences.readSettings(): TripSettings {
        val modeName = getString(config.KEY_MODE, TripMode.CAR.name) ?: TripMode.CAR.name
        return TripSettings(
            destination = getString(config.KEY_DESTINATION, "") ?: "",
            isTripActive = getBoolean(config.KEY_TRIP_ACTIVE, false),
            mode = runCatching { TripMode.valueOf(modeName) }.getOrDefault(TripMode.CAR),
            voiceEnabled = getBoolean(config.KEY_VOICE_ENABLED, true),
            notificationEnabled = getBoolean(config.KEY_NOTIFICATION_ENABLED, true),
            currentMotoId = getLong(config.KEY_MOTO_ID, -1L),
            nearbyMotoCount = getInt(config.KEY_MOTO_COUNT, 0),
            location = AppLocation(
                lat = getFloat(config.KEY_LAT, 0f),
                lon = getFloat(config.KEY_LON, 0f)
            )
        )
    }
}
