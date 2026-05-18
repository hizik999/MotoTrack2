package com.example.mototrack2java.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mototrack2java.R
import com.example.mototrack2java.core.config.AppConfig
import com.example.mototrack2java.domain.model.TripMode
import com.example.mototrack2java.domain.repository.LocationRepository
import com.example.mototrack2java.domain.repository.MotoRepository
import com.example.mototrack2java.domain.repository.TripPreferencesRepository
import com.example.mototrack2java.domain.usecase.StartTripUseCase
import com.example.mototrack2java.domain.usecase.StopTripUseCase
import com.example.mototrack2java.domain.usecase.SyncTripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: TripPreferencesRepository,
    private val motoRepository: MotoRepository,
    private val locationRepository: LocationRepository,
    private val startTrip: StartTripUseCase,
    private val stopTrip: StopTripUseCase,
    private val syncTrip: SyncTripUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.settings.collect { settings ->
                val buttonTextRes = if (settings.isTripActive) R.string.cancel_trip else R.string.start_trip
                _uiState.update { it.copy(settings = settings, startButtonTextRes = buttonTextRes) }
            }
        }
        viewModelScope.launch {
            while (isActive) {
                refreshLocation()
                val settings = _uiState.value.settings
                runCatching { syncTrip(settings) }
                    .onSuccess { refreshCachedMotos() }
                    .onFailure { showError(R.string.no_connection_message) }
                delay(AppConfig.Trip.SYNC_INTERVAL_MS)
            }
        }
    }

    fun refreshLocationFromPermissionResult() {
        viewModelScope.launch { refreshLocation() }
    }

    fun onDestinationChanged(destination: String) {
        viewModelScope.launch {
            preferencesRepository.setDestination(destination)
        }
    }

    fun onModeChanged(mode: TripMode) {
        viewModelScope.launch {
            preferencesRepository.setMode(mode)
        }
    }

    fun onVoiceChanged(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setVoiceEnabled(enabled)
        }
    }

    fun onNotificationChanged(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationEnabled(enabled)
        }
    }

    fun onNavigationAction() {
        _uiState.update { state ->
            state.copy(
                currentScreen = if (state.currentScreen == MainScreen.MAP) {
                    MainScreen.SETTINGS
                } else {
                    MainScreen.MAP
                }
            )
        }
    }

    fun onStartStopTripClicked() {
        viewModelScope.launch {
            val settings = _uiState.value.settings
            if (settings.isTripActive) {
                stopTrip(settings)
                _uiState.update { it.copy(currentScreen = MainScreen.MAP) }
            } else {
                val started = runCatching { startTrip(settings) }
                    .onFailure { showError(R.string.start_trip_error) }
                    .getOrDefault(false)
                if (started) {
                    _uiState.update {
                        it.copy(currentScreen = MainScreen.MAP, errorMessageRes = null)
                    }
                } else {
                    _uiState.update {
                        it.copy(startButtonTextRes = R.string.incomplete_settings_error)
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessageRes = null) }
    }

    private suspend fun refreshLocation() {
        locationRepository.getCurrentLocation()?.let { location ->
            preferencesRepository.setLocation(location)
        }
    }

    private suspend fun refreshCachedMotos() {
        _uiState.update { it.copy(motos = motoRepository.getCachedMotos()) }
    }

    private fun showError(@StringRes messageRes: Int) {
        _uiState.update { it.copy(errorMessageRes = messageRes) }
    }
}
