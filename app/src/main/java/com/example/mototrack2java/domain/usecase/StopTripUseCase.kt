package com.example.mototrack2java.domain.usecase

import com.example.mototrack2java.domain.model.TripMode
import com.example.mototrack2java.domain.model.TripSettings
import com.example.mototrack2java.domain.repository.MotoRepository
import com.example.mototrack2java.domain.repository.TripPreferencesRepository
import com.example.mototrack2java.domain.service.NotificationController
import javax.inject.Inject

class StopTripUseCase @Inject constructor(
    private val motoRepository: MotoRepository,
    private val preferencesRepository: TripPreferencesRepository,
    private val notificationController: NotificationController
) {
    suspend operator fun invoke(settings: TripSettings) {
        if (settings.mode == TripMode.MOTO && settings.currentMotoId != -1L) {
            runCatching { motoRepository.deleteMoto(settings.currentMotoId) }
        }
        preferencesRepository.clearTrip()
        preferencesRepository.setMode(TripMode.CAR)
        notificationController.cancelTripNotification()
    }
}
