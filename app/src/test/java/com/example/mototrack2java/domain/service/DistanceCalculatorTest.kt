package com.example.mototrack2java.domain.service

import com.example.mototrack2java.domain.model.AppLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceCalculatorTest {

    private val calculator = DistanceCalculator()

    @Test
    fun samePointHasZeroDistance() {
        val point = AppLocation(55.751244f, 37.618423f)

        assertEquals(0.0, calculator.distanceMeters(point, point), 0.01)
    }

    @Test
    fun closeMoscowPointsAreWithinOneKilometer() {
        val first = AppLocation(55.751244f, 37.618423f)
        val second = AppLocation(55.755864f, 37.617698f)

        assertTrue(calculator.distanceMeters(first, second) < 1_000)
    }

    @Test
    fun distanceIsSymmetric() {
        val first = AppLocation(55.751244f, 37.618423f)
        val second = AppLocation(59.9343f, 30.3351f)

        assertEquals(
            calculator.distanceMeters(first, second),
            calculator.distanceMeters(second, first),
            0.01
        )
    }
}
