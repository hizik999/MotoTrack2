package com.example.mototrack2java.data.repository

import com.example.mototrack2java.data.local.MotoLocalDataSource
import com.example.mototrack2java.data.remote.MotoRemoteDataSource
import com.example.mototrack2java.domain.model.AppLocation
import com.example.mototrack2java.domain.model.Moto
import com.example.mototrack2java.domain.service.DistanceCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultMotoRepositoryTest {

    @Test
    fun refreshNearbyMotosFiltersRemoteDataAndCachesNearbyItems() = runTest {
        val nearbyMoto = Moto(id = 1L, lat = 55.7513f, lon = 37.6184f)
        val farMoto = Moto(id = 2L, lat = 59.9343f, lon = 30.3351f)
        val remote = FakeRemoteDataSource(remoteMotos = listOf(nearbyMoto, farMoto))
        val local = FakeLocalDataSource()
        val repository = DefaultMotoRepository(remote, local, DistanceCalculator())

        val result = repository.refreshNearbyMotos(AppLocation(55.751244f, 37.618423f))

        assertEquals(listOf(nearbyMoto), result)
        assertEquals(listOf(nearbyMoto), local.motos)
    }

    @Test
    fun addUpdateAndDeleteDelegateToRemoteAndClearLocalCache() = runTest {
        val location = AppLocation(55.75f, 37.61f)
        val remote = FakeRemoteDataSource(addedId = 9L)
        val local = FakeLocalDataSource(initialMotos = listOf(Moto(1L, 1f, 1f)))
        val repository = DefaultMotoRepository(remote, local, DistanceCalculator())

        val id = repository.addMoto(location)
        repository.updateMoto(id, location)
        repository.deleteMoto(id)

        assertEquals(9L, id)
        assertEquals(location, remote.addedLocation)
        assertEquals(9L to location, remote.updatedMoto)
        assertEquals(9L, remote.deletedMotoId)
        assertTrue(local.motos.isEmpty())
    }

    @Test
    fun getCachedMotosDelegatesToLocalDataSource() = runTest {
        val cached = listOf(Moto(5L, 55.75f, 37.61f))
        val remote = FakeRemoteDataSource()
        val local = FakeLocalDataSource(initialMotos = cached)
        val repository = DefaultMotoRepository(remote, local, DistanceCalculator())

        assertEquals(cached, repository.getCachedMotos())
    }

    @Test
    fun deleteRemoteFailureKeepsLocalCache() = runTest {
        val cached = listOf(Moto(5L, 55.75f, 37.61f))
        val remote = FakeRemoteDataSource().apply { throwOnDelete = true }
        val local = FakeLocalDataSource(initialMotos = cached)
        val repository = DefaultMotoRepository(remote, local, DistanceCalculator())

        runCatching { repository.deleteMoto(5L) }

        assertEquals(cached, local.motos)
    }

    private class FakeRemoteDataSource(
        private val remoteMotos: List<Moto> = emptyList(),
        private val addedId: Long = 1L
    ) : MotoRemoteDataSource {

        var addedLocation: AppLocation? = null
        var updatedMoto: Pair<Long, AppLocation>? = null
        var deletedMotoId: Long? = null
        var throwOnDelete: Boolean = false

        override suspend fun getMotos(): List<Moto> = remoteMotos

        override suspend fun addMoto(location: AppLocation): Long {
            addedLocation = location
            return addedId
        }

        override suspend fun updateMoto(id: Long, location: AppLocation) {
            updatedMoto = id to location
        }

        override suspend fun deleteMoto(id: Long) {
            deletedMotoId = id
            if (throwOnDelete) error("delete failed")
        }
    }

    private class FakeLocalDataSource(
        initialMotos: List<Moto> = emptyList()
    ) : MotoLocalDataSource {

        var motos: List<Moto> = initialMotos

        override fun replaceAll(motos: List<Moto>) {
            this.motos = motos
        }

        override fun getAll(): List<Moto> = motos

        override fun clear() {
            motos = emptyList()
        }
    }
}
