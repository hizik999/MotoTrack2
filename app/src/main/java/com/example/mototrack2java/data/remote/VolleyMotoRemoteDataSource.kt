package com.example.mototrack2java.data.remote

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.mototrack2java.core.config.AppConfig
import com.example.mototrack2java.domain.model.AppLocation
import com.example.mototrack2java.domain.model.Moto
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class VolleyMotoRemoteDataSource @Inject constructor(
    @ApplicationContext context: Context
) : MotoRemoteDataSource {

    private val queue: RequestQueue = Volley.newRequestQueue(context)

    override suspend fun getMotos(): List<Moto> = suspendCancellableCoroutine { continuation ->
        val request = JsonArrayRequest(
            Request.Method.GET,
            motoUrl(),
            null,
            { response ->
                val motos = buildList {
                    for (index in 0 until response.length()) {
                        val item = response.getJSONObject(index)
                        add(item.toMoto())
                    }
                }
                continuation.resume(motos)
            },
            { error -> continuation.resumeWithException(error) }
        )
        continuation.invokeOnCancellation { request.cancel() }
        queue.add(request)
    }

    override suspend fun addMoto(location: AppLocation): Long = stringRequest(
        method = Request.Method.POST,
        url = motoUrl(),
        body = JSONObject()
            .put(AppConfig.JsonFields.LAT, location.lat)
            .put(AppConfig.JsonFields.LON, location.lon)
            .toString()
    ).let { response -> JSONObject(response).getLong(AppConfig.JsonFields.ID) }

    override suspend fun updateMoto(id: Long, location: AppLocation) {
        stringRequest(
            method = Request.Method.PUT,
            url = motoUrl(id),
            body = JSONObject()
                .put(AppConfig.JsonFields.LAT, location.lat)
                .put(AppConfig.JsonFields.LON, location.lon)
                .toString()
        )
    }

    override suspend fun deleteMoto(id: Long) {
        stringRequest(method = Request.Method.DELETE, url = motoUrl(id))
    }

    private suspend fun stringRequest(
        method: Int,
        url: String,
        body: String? = null
    ): String = suspendCancellableCoroutine { continuation ->
        val request = object : StringRequest(
            method,
            url,
            { response -> continuation.resume(response) },
            { error -> continuation.resumeWithException(error) }
        ) {
            override fun getBody(): ByteArray? = body?.toByteArray(Charsets.UTF_8)

            override fun getBodyContentType(): String = AppConfig.Network.CONTENT_TYPE_JSON
        }
        continuation.invokeOnCancellation { request.cancel() }
        queue.add(request)
    }

    private fun JSONObject.toMoto(): Moto = Moto(
        id = getLong(AppConfig.JsonFields.ID),
        lat = getDouble(AppConfig.JsonFields.LAT).toFloat(),
        lon = getDouble(AppConfig.JsonFields.LON).toFloat()
    )

    private fun motoUrl(id: Long? = null): String =
        buildString {
            append(AppConfig.Network.baseUrl)
            append('/')
            append(AppConfig.Network.MOTO_ENDPOINT)
            if (id != null) {
                append('/')
                append(id)
            }
        }
}
