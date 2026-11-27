package com.example.rinconsur.repository.remote

import com.example.rinconsur.repository.remote.api.UsuariosApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Crea un cliente Retrofit por microservicio. */
object ApiClient {

    private val okHttp: OkHttpClient by lazy {
        // Muestra cada petición y respuesta en Logcat (filtro: okhttp).
        // Cambiar a Level.NONE antes de generar el APK final.
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS) // Render puede tardar ~1 min en despertar
            .build()
    }

    private fun crear(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val usuariosApi: UsuariosApi by lazy {
        crear(ApiConfig.USUARIOS_URL).create(UsuariosApi::class.java)
    }
}