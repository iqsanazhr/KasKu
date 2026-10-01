package com.example.kasku.data.remote.currency

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Data Model untuk Kurs Mata Uang Dunia via REST API
 * Memenuhi kriteria standar materi ke-5: Networking & API (Retrofit 2 + Coroutines asinkron).
 */
@Serializable
data class CurrencyResponse(
    @SerialName("result") val result: String = "",
    @SerialName("base_code") val baseCode: String = "",
    @SerialName("time_last_update_utc") val lastUpdateUtc: String = "",
    @SerialName("rates") val rates: Map<String, Double> = emptyMap()
)

/**
 * Interface Retrofit untuk REST API Kurs Mata Uang (open.er-api.com)
 */
interface CurrencyApiService {

    @GET("v6/latest/{base}")
    suspend fun getExchangeRates(
        @Path("base") base: String = "USD"
    ): CurrencyResponse

    companion object {
        private const val BASE_URL = "https://open.er-api.com/"

        fun create(): CurrencyApiService {
            val json = kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            }
            val contentType = "application/json".toMediaType()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
                .create(CurrencyApiService::class.java)
        }
    }
}
