package com.example.mototrack2java.domain.service

import com.example.mototrack2java.core.config.AppConfig
import com.example.mototrack2java.domain.model.AppLocation
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class DistanceCalculator {
    fun distanceMeters(first: AppLocation, second: AppLocation): Double {
        val lat1 = Math.toRadians(first.lat.toDouble())
        val lat2 = Math.toRadians(second.lat.toDouble())
        val lon1 = Math.toRadians(first.lon.toDouble())
        val lon2 = Math.toRadians(second.lon.toDouble())
        val delta = lon1 - lon2

        val y = sqrt(
            (cos(lat2) * sin(delta)).pow(2) +
                (cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(delta)).pow(2)
        )
        val x = sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(delta)

        return atan2(y, x) * AppConfig.Geo.EARTH_RADIUS_METERS
    }
}
