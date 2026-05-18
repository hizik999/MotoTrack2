package com.example.mototrack2java.domain.usecase

import com.example.mototrack2java.domain.model.TripMode
import com.example.mototrack2java.domain.model.TripSettings
import com.example.mototrack2java.domain.repository.MotoRepository
import com.example.mototrack2java.domain.repository.TripPreferencesRepository
import com.example.mototrack2java.domain.service.NotificationController
import javax.inject.Inject

class StartTripUseCase @Inject constructor(
    private val motoRepository: MotoRepository,
    private val preferencesRepository: TripPreferencesRepository,
    private val notificationController: NotificationController,
    private val validateTripSettings: ValidateTripSettingsUseCase
) {
    suspend operator fun invoke(settings: TripSettings): Boolean {
        if (!validateTripSettings(settings)) return false

        if (settings.mode == TripMode.MOTO && settings.currentMotoId == -1L) {
            preferencesRepository.setCurrentMotoId(motoRepository.addMoto(settings.location))
        }
        preferencesRepository.setTripActive(true)

        if (settings.notificationEnabled) {
            notificationController.showTripNotification(settings.mode)
        } else {
            notificationController.cancelTripNotification()
        }
        return true
    }
}
