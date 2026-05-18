package com.example.mototrack2java.domain.usecase

import com.example.mototrack2java.domain.model.AppLocation
import com.example.mototrack2java.domain.model.Moto
import com.example.mototrack2java.domain.model.TripMode
import com.example.mototrack2java.domain.model.TripSettings
import com.example.mototrack2java.testutil.FakeMotoRepository
import com.example.mototrack2java.testutil.FakeSoundPlayer
import com.example.mototrack2java.testutil.FakeTripPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncTripUseCaseTest {

    @Test
    fun inactiveTripDoesNothing() = runTest {
        val motoRepository = FakeMotoRepository(nearbyMotos = listOf(Moto(1L, 1f, 1f)))
        val preferencesRepository = FakeTripPreferencesRepository()
        val soundPlayer = FakeSoundPlayer()
        val useCase = SyncTripUseCase(motoRepository, preferencesRepository, soundPlayer)

        useCase(TripSettings(isTripActive = false))

        assertNull(motoRepository.refreshLocation)
        assertEquals(0, soundPlayer.playCount)
        assertEquals(0, preferencesRepository.currentSettings.nearbyMotoCount)
    }

    @Test
    fun carTripRefreshesNearbyMotosAndPlaysSoundWhenCountIncreases() = runTest {
        val location = AppLocation(55.75f, 37.61f)
        val motoRepository = FakeMotoRepository(
            nearbyMotos = listOf(Moto(1L, 55.75f, 37.61f), Moto(2L, 55.76f, 37.62f))
        )
        val preferencesRepository = FakeTripPreferencesRepository()
        val soundPlayer = FakeSoundPlayer()
        val useCase = SyncTripUseCase(motoRepository, preferencesRepository, soundPlayer)

        useCase(
            TripSettings(
                isTripActive = true,
                mode = TripMode.CAR,
                voiceEnabled = true,
                nearbyMotoCount = 1,
                location = location
            )
        )

        assertEquals(location, motoRepository.refreshLocation)
        assertEquals(2, preferencesRepository.currentSettings.nearbyMotoCount)
        assertEquals(1, soundPlayer.playCount)
    }

    @Test
    fun carTripDoesNotPlaySoundWhenVoiceDisabled() = runTest {
        val motoRepository = FakeMotoRepository(nearbyMotos = listOf(Moto(1L, 55.75f, 37.61f)))
        val preferencesRepository = FakeTripPreferencesRepository()
        val soundPlayer = FakeSoundPlayer()
        val useCase = SyncTripUseCase(motoRepository, preferencesRepository, soundPlayer)

        useCase(
            TripSettings(
                isTripActive = true,
                mode = TripMode.CAR,
                voiceEnabled = false,
                nearbyMotoCount = 0
            )
        )

        assertEquals(1, preferencesRepository.currentSettings.nearbyMotoCount)
        assertEquals(0, soundPlayer.playCount)
    }

    @Test
    fun carTripDoesNotPlaySoundWhenNearbyCountDoesNotIncrease() = runTest {
        val motoRepository = FakeMotoRepository(nearbyMotos = listOf(Moto(1L, 55.75f, 37.61f)))
        val preferencesRepository = FakeTripPreferencesRepository()
        val soundPlayer = FakeSoundPlayer()
        val useCase = SyncTripUseCase(motoRepository, preferencesRepository, soundPlayer)

        useCase(
            TripSettings(
                isTripActive = true,
                mode = TripMode.CAR,
                voiceEnabled = true,
                nearbyMotoCount = 1
            )
        )

        assertEquals(1, preferencesRepository.currentSettings.nearbyMotoCount)
        assertEquals(0, soundPlayer.playCount)
    }

    @Test
    fun motoTripUpdatesExistingMotoLocation() = runTest {
        val location = AppLocation(55.75f, 37.61f)
        val motoRepository = FakeMotoRepository()
        val preferencesRepository = FakeTripPreferencesRepository()
        val soundPlayer = FakeSoundPlayer()
        val useCase = SyncTripUseCase(motoRepository, preferencesRepository, soundPlayer)

        useCase(
            TripSettings(
                isTripActive = true,
                mode = TripMode.MOTO,
                currentMotoId = 12L,
                location = location
            )
        )

        assertEquals(12L to location, motoRepository.updatedMoto)
    }

    @Test
    fun motoTripWithoutRegisteredIdDoesNotUpdateRemoteLocation() = runTest {
        val motoRepository = FakeMotoRepository()
        val preferencesRepository = FakeTripPreferencesRepository()
        val soundPlayer = FakeSoundPlayer()
        val useCase = SyncTripUseCase(motoRepository, preferencesRepository, soundPlayer)

        useCase(
            TripSettings(
                isTripActive = true,
                mode = TripMode.MOTO,
                currentMotoId = -1L
            )
        )

        assertNull(motoRepository.updatedMoto)
    }
}
