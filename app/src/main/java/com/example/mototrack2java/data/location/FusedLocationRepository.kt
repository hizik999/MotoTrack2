package com.example.mototrack2java.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.mototrack2java.domain.model.AppLocation
import com.example.mototrack2java.domain.repository.LocationRepository
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Singleton
class FusedLocationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationRepository {

    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    override suspend fun getCurrentLocation(): AppLocation? {
        val hasPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return null

        return suspendCancellableCoroutine { continuation ->
            client.lastLocation
                .addOnSuccessListener { location ->
                    continuation.resume(location?.let { AppLocation(it.latitude.toFloat(), it.longitude.toFloat()) })
                }
                .addOnFailureListener { continuation.resume(null) }
        }
    }
}
