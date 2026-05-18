package com.example.mototrack2java.data.repository

import com.example.mototrack2java.core.config.AppConfig
import com.example.mototrack2java.data.local.MotoLocalDataSource
import com.example.mototrack2java.data.remote.MotoRemoteDataSource
import com.example.mototrack2java.domain.model.AppLocation
import com.example.mototrack2java.domain.model.Moto
import com.example.mototrack2java.domain.repository.MotoRepository
import com.example.mototrack2java.domain.service.DistanceCalculator
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class DefaultMotoRepository @Inject constructor(
    private val remoteDataSource: MotoRemoteDataSource,
    private val localDataSource: MotoLocalDataSource,
    private val distanceCalculator: DistanceCalculator
) : MotoRepository {

    override suspend fun refreshNearbyMotos(userLocation: AppLocation): List<Moto> = withContext(Dispatchers.IO) {
        val nearby = remoteDataSource.getMotos()
            .filter { moto ->
                distanceCalculator.distanceMeters(
                    userLocation,
                    AppLocation(moto.lat, moto.lon)
                ) < AppConfig.Trip.NEARBY_RADIUS_METERS
            }
        localDataSource.replaceAll(nearby)
        nearby
    }

    override suspend fun addMoto(location: AppLocation): Long = withContext(Dispatchers.IO) {
        remoteDataSource.addMoto(location)
    }

    override suspend fun updateMoto(id: Long, location: AppLocation) = withContext(Dispatchers.IO) {
        remoteDataSource.updateMoto(id, location)
    }

    override suspend fun deleteMoto(id: Long) = withContext(Dispatchers.IO) {
        remoteDataSource.deleteMoto(id)
        localDataSource.clear()
    }

    override suspend fun getCachedMotos(): List<Moto> = withContext(Dispatchers.IO) {
        localDataSource.getAll()
    }
}
