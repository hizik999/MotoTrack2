package com.example.mototrack2java.domain.model

data class TripSettings(
    val destination: String = "",
    val isTripActive: Boolean = false,
    val mode: TripMode = TripMode.CAR,
    val voiceEnabled: Boolean = true,
    val notificationEnabled: Boolean = true,
    val currentMotoId: Long = -1L,
    val nearbyMotoCount: Int = 0,
    val location: AppLocation = AppLocation()
)
