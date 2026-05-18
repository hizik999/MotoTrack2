package com.example.mototrack2java.domain.usecase

import com.example.mototrack2java.domain.model.AppLocation
import com.example.mototrack2java.domain.model.TripMode
import com.example.mototrack2java.domain.model.TripSettings
import com.example.mototrack2java.testutil.FakeMotoRepository
import com.example.mototrack2java.testutil.FakeNotificationController
import com.example.mototrack2java.testutil.FakeTripPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StartTripUseCaseTest {

    private val motoRepository = FakeMotoRepository(addedMotoId = 42L)
    private val preferencesRepository = FakeTripPreferencesRepository()
    private val notificationController = FakeNotificationController()
    private val useCase = StartTripUseCase(
        motoRepository = motoRepository,
        preferencesRepository = preferencesRepository,
        notificationController = notificationController,
        validateTripSettings = ValidateTripSettingsUseCase()
    )

    @Test
    fun invalidSettingsDoNotStartTrip() = runTest {
        val started = useCase(TripSettings(destination = " "))

        assertFalse(started)
        assertFalse(preferencesRepository.currentSettings.isTripActive)
        assertNull(motoRepository.addedLocation)
        assertTrue(notificationController.shownModes.isEmpty())
    }

    @Test
    fun motoTripRegistersMotoAndShowsNotification() = runTest {
        val location = AppLocation(55.75f, 37.61f)
        val started = useCase(
            TripSettings(
                destination = "Moscow",
                mode = TripMode.MOTO,
                currentMotoId = -1L,
                location = location
            )
        )

        assertTrue(started)
        assertEquals(location, motoRepository.addedLocation)
        assertEquals(42L, preferencesRepository.currentSettings.currentMotoId)
        assertTrue(preferencesRepository.currentSettings.isTripActive)
        assertEquals(listOf(TripMode.MOTO), notificationController.shownModes)
    }

    @Test
    fun carTripDoesNotRegisterMoto() = runTest {
        val started = useCase(
            TripSettings(
                destination = "Moscow",
                mode = TripMode.CAR,
                currentMotoId = -1L
            )
        )

        assertTrue(started)
        assertNull(motoRepository.addedLocation)
        assertEquals(-1L, preferencesRepository.currentSettings.currentMotoId)
        assertTrue(preferencesRepository.currentSettings.isTripActive)
    }

    @Test
    fun existingMotoTripDoesNotRegisterDuplicateMoto() = runTest {
        val settings = TripSettings(
            destination = "Moscow",
            mode = TripMode.MOTO,
            currentMotoId = 15L
        )
        val preferencesRepository = FakeTripPreferencesRepository(settings)
        val useCase = StartTripUseCase(
            motoRepository = motoRepository,
            preferencesRepository = preferencesRepository,
            notificationController = notificationController,
            validateTripSettings = ValidateTripSettingsUseCase()
        )

        val started = useCase(settings)

        assertTrue(started)
        assertNull(motoRepository.addedLocation)
        assertEquals(15L, preferencesRepository.currentSettings.currentMotoId)
        assertTrue(preferencesRepository.currentSettings.isTripActive)
    }

    @Test
    fun disabledNotificationsCancelExistingNotification() = runTest {
        val started = useCase(
            TripSettings(
                destination = "Moscow",
                mode = TripMode.CAR,
                notificationEnabled = false
            )
        )

        assertTrue(started)
        assertTrue(preferencesRepository.currentSettings.isTripActive)
        assertTrue(notificationController.shownModes.isEmpty())
        assertEquals(1, notificationController.cancelCount)
    }
}
