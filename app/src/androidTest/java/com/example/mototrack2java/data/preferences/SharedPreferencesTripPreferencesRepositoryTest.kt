package com.example.mototrack2java.data.preferences

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.mototrack2java.core.config.AppConfig
import com.example.mototrack2java.domain.model.AppLocation
import com.example.mototrack2java.domain.model.TripMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SharedPreferencesTripPreferencesRepositoryTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        clearPreferences()
    }

    @After
    fun tearDown() {
        clearPreferences()
    }

    @Test
    fun writesAndReadsTripSettings() = runBlocking {
        val repository = SharedPreferencesTripPreferencesRepository(context)
        val location = AppLocation(55.75f, 37.61f)

        repository.setDestination("Moscow")
        repository.setTripActive(true)
        repository.setMode(TripMode.MOTO)
        repository.setVoiceEnabled(false)
        repository.setNotificationEnabled(false)
        repository.setCurrentMotoId(15L)
        repository.setNearbyMotoCount(4)
        repository.setLocation(location)

        val settings = repository.settings.first()

        assertEquals("Moscow", settings.destination)
        assertTrue(settings.isTripActive)
        assertEquals(TripMode.MOTO, settings.mode)
        assertFalse(settings.voiceEnabled)
        assertFalse(settings.notificationEnabled)
        assertEquals(15L, settings.currentMotoId)
        assertEquals(4, settings.nearbyMotoCount)
        assertEquals(location, settings.location)
    }

    @Test
    fun emptyPreferencesExposeDefaultSettings() = runBlocking {
        val repository = SharedPreferencesTripPreferencesRepository(context)

        val settings = repository.settings.first()

        assertEquals("", settings.destination)
        assertFalse(settings.isTripActive)
        assertEquals(TripMode.CAR, settings.mode)
        assertTrue(settings.voiceEnabled)
        assertTrue(settings.notificationEnabled)
        assertEquals(-1L, settings.currentMotoId)
        assertEquals(0, settings.nearbyMotoCount)
        assertEquals(AppLocation(), settings.location)
    }

    @Test
    fun corruptedModeFallsBackToCar() = runBlocking {
        context.getSharedPreferences(AppConfig.Preferences.NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(AppConfig.Preferences.KEY_MODE, "BROKEN_MODE")
            .commit()

        val repository = SharedPreferencesTripPreferencesRepository(context)

        assertEquals(TripMode.CAR, repository.settings.first().mode)
    }

    @Test
    fun clearTripKeepsModeAndDisablesActiveRouteData() = runBlocking {
        val repository = SharedPreferencesTripPreferencesRepository(context)

        repository.setDestination("Moscow")
        repository.setTripActive(true)
        repository.setMode(TripMode.MOTO)
        repository.setCurrentMotoId(15L)
        repository.setNearbyMotoCount(4)
        repository.clearTrip()

        val settings = repository.settings.first()

        assertEquals("", settings.destination)
        assertFalse(settings.isTripActive)
        assertEquals(TripMode.MOTO, settings.mode)
        assertEquals(-1L, settings.currentMotoId)
        assertEquals(0, settings.nearbyMotoCount)
    }

    private fun clearPreferences() {
        context.getSharedPreferences(AppConfig.Preferences.NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }
}
