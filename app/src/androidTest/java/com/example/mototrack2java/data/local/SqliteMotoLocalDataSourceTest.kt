package com.example.mototrack2java.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.mototrack2java.core.config.AppConfig
import com.example.mototrack2java.domain.model.Moto
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SqliteMotoLocalDataSourceTest {

    private lateinit var context: Context
    private lateinit var dataSource: SqliteMotoLocalDataSource

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(AppConfig.Database.NAME)
        dataSource = SqliteMotoLocalDataSource(context)
    }

    @After
    fun tearDown() {
        dataSource.close()
        context.deleteDatabase(AppConfig.Database.NAME)
    }

    @Test
    fun replaceAllStoresOnlyLatestMotos() {
        val firstBatch = listOf(Moto(1L, 55.75f, 37.61f), Moto(2L, 55.76f, 37.62f))
        val secondBatch = listOf(Moto(3L, 55.77f, 37.63f))

        dataSource.replaceAll(firstBatch)
        assertEquals(firstBatch, dataSource.getAll())

        dataSource.replaceAll(secondBatch)
        assertEquals(secondBatch, dataSource.getAll())
    }

    @Test
    fun clearRemovesAllCachedMotos() {
        dataSource.replaceAll(listOf(Moto(1L, 55.75f, 37.61f)))

        dataSource.clear()

        assertEquals(emptyList<Moto>(), dataSource.getAll())
    }

    @Test
    fun replaceAllWithEmptyListClearsPreviousMotos() {
        dataSource.replaceAll(listOf(Moto(1L, 55.75f, 37.61f)))

        dataSource.replaceAll(emptyList())

        assertEquals(emptyList<Moto>(), dataSource.getAll())
    }
}
