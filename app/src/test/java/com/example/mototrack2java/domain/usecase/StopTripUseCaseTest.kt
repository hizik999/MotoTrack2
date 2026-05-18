package com.example.mototrack2java.domain.usecase

import com.example.mototrack2java.domain.model.TripMode
import com.example.mototrack2java.domain.model.TripSettings
import com.example.mototrack2java.testutil.FakeMotoRepository
import com.example.mototrack2java.testutil.FakeNotificationController
import com.example.mototrack2java.testutil.FakeTripPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StopTripUseCaseTest {

    @Test
    fun motoTripDeletesMotoClearsTripAndCancelsNotification() = runTest {
        val motoRepository = FakeMotoRepository()
        val preferencesRepository = FakeTripPreferencesRepository()
        val notificationController = FakeNotificationController()
        val useCase = StopTripUseCase(motoRepository, preferencesRepository, notificationController)

        useCase(
            TripSettings(
                destination = "Moscow",
                isTripActive = true,
                mode = TripMode.MOTO,
                currentMotoId = 77L,
                nearbyMotoCount = 3
            )
        )

        assertEquals(77L, motoRepository.deletedMotoId)
        assertFalse(preferencesRepository.currentSettings.isTripActive)
        assertEquals("", preferencesRepository.currentSettings.destination)
        assertEquals(-1L, preferencesRepository.currentSettings.currentMotoId)
        assertEquals(0, preferencesRepository.currentSettings.nearbyMotoCount)
        assertEquals(TripMode.CAR, preferencesRepository.currentSettings.mode)
        assertEquals(1, notificationController.cancelCount)
    }

    @Test
    fun deleteFailureStillClearsLocalTripState() = runTest {
        val motoRepository = FakeMotoRepository().apply { throwOnDelete = true }
        val preferencesRepository = FakeTripPreferencesRepository()
        val notificationController = FakeNotificationController()
        val useCase = StopTripUseCase(motoRepository, preferencesRepository, notificationController)

        useCase(
            TripSettings(
                destination = "Moscow",
                isTripActive = true,
                mode = TripMode.MOTO,
                currentMotoId = 77L
            )
        )

        assertFalse(preferencesRepository.currentSettings.isTripActive)
        assertEquals(TripMode.CAR, preferencesRepository.currentSettings.mode)
        assertEquals(1, notificationController.cancelCount)
    }

    @Test
    fun carTripDoesNotDeleteMotoButStillClearsTrip() = runTest {
        val motoRepository = FakeMotoRepository()
        val preferencesRepository = FakeTripPreferencesRepository()
        val notificationController = FakeNotificationController()
        val useCase = StopTripUseCase(motoRepository, preferencesRepository, notificationController)

        useCase(
            TripSettings(
                destination = "Moscow",
                isTripActive = true,
                mode = TripMode.CAR,
                currentMotoId = 77L,
                nearbyMotoCount = 3
            )
        )

        assertEquals(null, motoRepository.deletedMotoId)
        assertFalse(preferencesRepository.currentSettings.isTripActive)
        assertEquals(TripMode.CAR, preferencesRepository.currentSettings.mode)
        assertEquals(1, notificationController.cancelCount)
    }
}
