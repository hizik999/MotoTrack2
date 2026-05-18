package com.example.mototrack2java.domain.service

import com.example.mototrack2java.domain.model.TripMode

interface NotificationController {
    fun showTripNotification(mode: TripMode)
    fun cancelTripNotification()
}
