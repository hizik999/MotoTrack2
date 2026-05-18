package com.example.mototrack2java.domain.repository

import com.example.mototrack2java.domain.model.AppLocation

interface LocationRepository {
    suspend fun getCurrentLocation(): AppLocation?
}
