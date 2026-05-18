package com.example.mototrack2java.domain.usecase

import com.example.mototrack2java.domain.model.TripSettings
import javax.inject.Inject

class ValidateTripSettingsUseCase @Inject constructor() {
    operator fun invoke(settings: TripSettings): Boolean = settings.destination.isNotBlank()
}
