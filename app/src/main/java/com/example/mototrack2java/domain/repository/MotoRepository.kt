package com.example.mototrack2java.domain.repository

import com.example.mototrack2java.domain.model.AppLocation
import com.example.mototrack2java.domain.model.Moto

interface MotoRepository {
    suspend fun refreshNearbyMotos(userLocation: AppLocation): List<Moto>
    suspend fun addMoto(location: AppLocation): Long
    suspend fun updateMoto(id: Long, location: AppLocation)
    suspend fun deleteMoto(id: Long)
    suspend fun getCachedMotos(): List<Moto>
}
