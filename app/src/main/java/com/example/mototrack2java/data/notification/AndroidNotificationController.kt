package com.example.mototrack2java.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mototrack2java.MainActivity
import com.example.mototrack2java.R
import com.example.mototrack2java.core.config.AppConfig
import com.example.mototrack2java.domain.model.TripMode
import com.example.mototrack2java.domain.service.NotificationController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidNotificationController @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationController {

    init {
        createChannel()
    }

    override fun showTripNotification(mode: TripMode) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            AppConfig.Notifications.REQUEST_CODE_MAIN_ACTIVITY,
            Intent(context, MainActivity::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        val title = when (mode) {
            TripMode.CAR -> context.getString(R.string.notification_title_car)
            TripMode.MOTO -> context.getString(R.string.notification_title_moto)
        }
        val description = when (mode) {
            TripMode.CAR -> context.getString(R.string.notification_desc_car)
            TripMode.MOTO -> context.getString(R.string.notification_desc_moto)
        }

        val notification = NotificationCompat.Builder(context, AppConfig.Notifications.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_location_on_24)
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.notification_big_text))
            )
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context)
            .notify(AppConfig.Notifications.TRIP_NOTIFICATION_ID, notification)
    }

    override fun cancelTripNotification() {
        NotificationManagerCompat.from(context)
            .cancel(AppConfig.Notifications.TRIP_NOTIFICATION_ID)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            AppConfig.Notifications.CHANNEL_ID,
            AppConfig.Notifications.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
