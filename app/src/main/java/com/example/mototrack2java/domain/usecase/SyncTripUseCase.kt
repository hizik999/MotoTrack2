package com.example.mototrack2java.domain.usecase

import com.example.mototrack2java.domain.model.TripMode
import com.example.mototrack2java.domain.model.TripSettings
import com.example.mototrack2java.domain.repository.MotoRepository
import com.example.mototrack2java.domain.repository.TripPreferencesRepository
import com.example.mototrack2java.domain.service.SoundPlayer
import javax.inject.Inject

class SyncTripUseCase @Inject constructor(
    private val motoRepository: MotoRepository,
    private val preferencesRepository: TripPreferencesRepository,
    private val soundPlayer: SoundPlayer
) {
    suspend operator fun invoke(settings: TripSettings) {
        if (!settings.isTripActive) return

        when (settings.mode) {
            TripMode.CAR -> {
                val motos = motoRepository.refreshNearbyMotos(settings.location)
                if (motos.size > settings.nearbyMotoCount && settings.voiceEnabled) {
                    soundPlayer.playMotoDetected()
                }
                preferencesRepository.setNearbyMotoCount(motos.size)
            }
            TripMode.MOTO -> {
                if (settings.currentMotoId != -1L) {
                    motoRepository.updateMoto(settings.currentMotoId, settings.location)
                }
            }
        }
    }
}
