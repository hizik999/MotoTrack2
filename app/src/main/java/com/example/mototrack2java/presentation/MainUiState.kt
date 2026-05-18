package com.example.mototrack2java.presentation

import androidx.annotation.StringRes
import com.example.mototrack2java.R
import com.example.mototrack2java.domain.model.Moto
import com.example.mototrack2java.domain.model.TripSettings

data class MainUiState(
    val settings: TripSettings = TripSettings(),
    val motos: List<Moto> = emptyList(),
    val currentScreen: MainScreen = MainScreen.MAP,
    @StringRes val startButtonTextRes: Int = R.string.start_trip,
    @StringRes val errorMessageRes: Int? = null
)

enum class MainScreen {
    MAP,
    SETTINGS
}
