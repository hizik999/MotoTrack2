package com.example.mototrack2java.presentation

import com.example.mototrack2java.R
import com.example.mototrack2java.domain.model.AppLocation
import com.example.mototrack2java.domain.model.Moto
import com.example.mototrack2java.domain.model.TripMode
import com.example.mototrack2java.domain.model.TripSettings
import com.example.mototrack2java.domain.usecase.StartTripUseCase
import com.example.mototrack2java.domain.usecase.StopTripUseCase
import com.example.mototrack2java.domain.usecase.SyncTripUseCase
import com.example.mototrack2java.domain.usecase.ValidateTripSettingsUseCase
import com.example.mototrack2java.testutil.FakeLocationRepository
import com.example.mototrack2java.testutil.FakeMotoRepository
import com.example.mototrack2java.testutil.FakeNotificationController
import com.example.mototrack2java.testutil.FakeSoundPlayer
import com.example.mototrack2java.testutil.FakeTripPreferencesRepository
import com.example.mototrack2java.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initPublishesStoredSettingsAndCachedMotos() {
        val cachedMoto = Moto(7L, 55.75f, 37.61f)
        val subject = createSubject(
            initialSettings = TripSettings(
                destination = "Moscow",
                isTripActive = true,
                mode = TripMode.MOTO,
                currentMotoId = 7L
            ),
            motoRepository = FakeMotoRepository().apply { cachedMotos = listOf(cachedMoto) }
        )

        runQueuedTasks()

        assertEquals("Moscow", subject.viewModel.uiState.value.settings.destination)
        assertEquals(TripMode.MOTO, subject.viewModel.uiState.value.settings.mode)
        assertEquals(R.string.cancel_trip, subject.viewModel.uiState.value.startButtonTextRes)
        assertEquals(listOf(cachedMoto), subject.viewModel.uiState.value.motos)
    }

    @Test
    fun destinationModeVoiceAndNotificationActionsPersistSettings() {
        val subject = createSubject()
        runQueuedTasks()

        subject.viewModel.onDestinationChanged("Saint Petersburg")
        subject.viewModel.onModeChanged(TripMode.MOTO)
        subject.viewModel.onVoiceChanged(false)
        subject.viewModel.onNotificationChanged(false)
        runQueuedTasks()

        val settings = subject.preferencesRepository.currentSettings
        assertEquals("Saint Petersburg", settings.destination)
        assertEquals(TripMode.MOTO, settings.mode)
        assertFalse(settings.voiceEnabled)
        assertFalse(settings.notificationEnabled)
    }

    @Test
    fun navigationActionTogglesMapAndSettingsScreens() {
        val subject = createSubject()
        runQueuedTasks()

        assertEquals(MainScreen.MAP, subject.viewModel.uiState.value.currentScreen)

        subject.viewModel.onNavigationAction()
        assertEquals(MainScreen.SETTINGS, subject.viewModel.uiState.value.currentScreen)

        subject.viewModel.onNavigationAction()
        assertEquals(MainScreen.MAP, subject.viewModel.uiState.value.currentScreen)
    }

    @Test
    fun refreshLocationFromPermissionResultStoresLatestLocation() {
        val locationRepository = FakeLocationRepository()
        val subject = createSubject(locationRepository = locationRepository)
        runQueuedTasks()

        val location = AppLocation(55.75f, 37.61f)
        locationRepository.currentLocation = location
        subject.viewModel.refreshLocationFromPermissionResult()
        runQueuedTasks()

        assertEquals(location, subject.preferencesRepository.currentSettings.location)
        assertTrue(locationRepository.requestCount >= 2)
    }

    @Test
    fun invalidStartShowsIncompleteSettingsAndDoesNotActivateTrip() {
        val subject = createSubject()
        runQueuedTasks()

        subject.viewModel.onStartStopTripClicked()
        runQueuedTasks()

        assertFalse(subject.preferencesRepository.currentSettings.isTripActive)
        assertEquals(R.string.incomplete_settings_error, subject.viewModel.uiState.value.startButtonTextRes)
        assertTrue(subject.notificationController.shownModes.isEmpty())
    }

    @Test
    fun validStartActivatesTripShowsNotificationAndReturnsToMap() {
        val subject = createSubject(
            initialSettings = TripSettings(destination = "Moscow", mode = TripMode.CAR)
        )
        runQueuedTasks()

        subject.viewModel.onNavigationAction()
        subject.viewModel.onStartStopTripClicked()
        runQueuedTasks()

        assertTrue(subject.preferencesRepository.currentSettings.isTripActive)
        assertEquals(MainScreen.MAP, subject.viewModel.uiState.value.currentScreen)
        assertNull(subject.viewModel.uiState.value.errorMessageRes)
        assertEquals(listOf(TripMode.CAR), subject.notificationController.shownModes)
    }

    @Test
    fun activeTripClickStopsTripClearsStateAndCancelsNotification() {
        val subject = createSubject(
            initialSettings = TripSettings(
                destination = "Moscow",
                isTripActive = true,
                mode = TripMode.MOTO,
                currentMotoId = 11L,
                nearbyMotoCount = 2
            )
        )
        runQueuedTasks()

        subject.viewModel.onStartStopTripClicked()
        runQueuedTasks()

        val settings = subject.preferencesRepository.currentSettings
        assertFalse(settings.isTripActive)
        assertEquals("", settings.destination)
        assertEquals(TripMode.CAR, settings.mode)
        assertEquals(-1L, settings.currentMotoId)
        assertEquals(0, settings.nearbyMotoCount)
        assertEquals(1, subject.notificationController.cancelCount)
        assertEquals(11L, subject.motoRepository.deletedMotoId)
        assertEquals(MainScreen.MAP, subject.viewModel.uiState.value.currentScreen)
    }

    @Test
    fun syncFailureShowsNoConnectionError() {
        val subject = createSubject(
            initialSettings = TripSettings(destination = "Moscow", isTripActive = true),
            motoRepository = FakeMotoRepository().apply { throwOnRefresh = true }
        )

        runQueuedTasks()

        assertEquals(R.string.no_connection_message, subject.viewModel.uiState.value.errorMessageRes)
    }

    private fun runQueuedTasks() {
        mainDispatcherRule.dispatcher.scheduler.runCurrent()
    }

    private fun createSubject(
        initialSettings: TripSettings = TripSettings(),
        preferencesRepository: FakeTripPreferencesRepository = FakeTripPreferencesRepository(initialSettings),
        motoRepository: FakeMotoRepository = FakeMotoRepository(),
        locationRepository: FakeLocationRepository = FakeLocationRepository(),
        notificationController: FakeNotificationController = FakeNotificationController(),
        soundPlayer: FakeSoundPlayer = FakeSoundPlayer()
    ): Subject {
        val viewModel = MainViewModel(
            preferencesRepository = preferencesRepository,
            motoRepository = motoRepository,
            locationRepository = locationRepository,
            startTrip = StartTripUseCase(
                motoRepository = motoRepository,
                preferencesRepository = preferencesRepository,
                notificationController = notificationController,
                validateTripSettings = ValidateTripSettingsUseCase()
            ),
            stopTrip = StopTripUseCase(
                motoRepository = motoRepository,
                preferencesRepository = preferencesRepository,
                notificationController = notificationController
            ),
            syncTrip = SyncTripUseCase(
                motoRepository = motoRepository,
                preferencesRepository = preferencesRepository,
                soundPlayer = soundPlayer
            )
        )
        return Subject(
            viewModel = viewModel,
            preferencesRepository = preferencesRepository,
            motoRepository = motoRepository,
            locationRepository = locationRepository,
            notificationController = notificationController,
            soundPlayer = soundPlayer
        )
    }

    private data class Subject(
        val viewModel: MainViewModel,
        val preferencesRepository: FakeTripPreferencesRepository,
        val motoRepository: FakeMotoRepository,
        val locationRepository: FakeLocationRepository,
        val notificationController: FakeNotificationController,
        val soundPlayer: FakeSoundPlayer
    )
}
