package com.example.mototrack2java.domain.usecase

import com.example.mototrack2java.domain.model.TripSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateTripSettingsUseCaseTest {

    private val useCase = ValidateTripSettingsUseCase()

    @Test
    fun blankDestinationIsInvalid() {
        assertFalse(useCase(TripSettings(destination = "  ")))
    }

    @Test
    fun filledDestinationIsValid() {
        assertTrue(useCase(TripSettings(destination = "Москва, Тверская 1")))
    }
}
