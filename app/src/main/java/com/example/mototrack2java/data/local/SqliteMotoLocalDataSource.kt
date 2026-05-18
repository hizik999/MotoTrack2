package com.example.mototrack2java.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.mototrack2java.core.config.AppConfig
import com.example.mototrack2java.domain.model.Moto
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SqliteMotoLocalDataSource @Inject constructor(
    @ApplicationContext context: Context
) : SQLiteOpenHelper(
    context,
    AppConfig.Database.NAME,
    null,
    AppConfig.Database.SCHEMA
), MotoLocalDataSource {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ${AppConfig.Database.TABLE_MOTO} (
                ${AppConfig.Database.COLUMN_ID} INTEGER PRIMARY KEY,
                ${AppConfig.Database.COLUMN_LAT} REAL NOT NULL,
                ${AppConfig.Database.COLUMN_LON} REAL NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${AppConfig.Database.TABLE_MOTO}")
        onCreate(db)
    }

    override fun replaceAll(motos: List<Moto>) {
        writableDatabase.use { db ->
            db.beginTransaction()
            try {
                db.delete(AppConfig.Database.TABLE_MOTO, null, null)
                motos.forEach { moto ->
                    db.insert(AppConfig.Database.TABLE_MOTO, null, moto.toContentValues())
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    override fun getAll(): List<Moto> {
        readableDatabase.use { db ->
            db.rawQuery(
                "SELECT ${AppConfig.Database.COLUMN_ID}, ${AppConfig.Database.COLUMN_LAT}, " +
                    "${AppConfig.Database.COLUMN_LON} FROM ${AppConfig.Database.TABLE_MOTO}",
                null
            ).use { cursor ->
                val result = mutableListOf<Moto>()
                while (cursor.moveToNext()) {
                    result += Moto(
                        id = cursor.getLong(0),
                        lat = cursor.getFloat(1),
                        lon = cursor.getFloat(2)
                    )
                }
                return result
            }
        }
    }

    override fun clear() {
        writableDatabase.use { db ->
            db.delete(AppConfig.Database.TABLE_MOTO, null, null)
        }
    }

    private fun Moto.toContentValues(): ContentValues = ContentValues().apply {
        put(AppConfig.Database.COLUMN_ID, id)
        put(AppConfig.Database.COLUMN_LAT, lat)
        put(AppConfig.Database.COLUMN_LON, lon)
    }
}
