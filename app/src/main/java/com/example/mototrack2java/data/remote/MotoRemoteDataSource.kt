package com.example.mototrack2java.data.remote

import com.example.mototrack2java.domain.model.AppLocation
import com.example.mototrack2java.domain.model.Moto

interface MotoRemoteDataSource {
    suspend fun getMotos(): List<Moto>
    suspend fun addMoto(location: AppLocation): Long
    suspend fun updateMoto(id: Long, location: AppLocation)
    suspend fun deleteMoto(id: Long)
}
