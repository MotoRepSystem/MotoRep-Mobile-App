package com.example.motorep_mobile_app.conexion

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Configuración de la conexión entre Android y Django
object RetrofitClient {

    // Dirección del servidor Django en la computadora
    private const val BASE_URL = "https://motorepweb-api-h3ahb3c6hgfebpa6.centralus-01.azurewebsites.net/"

    // Crear y configurar Retrofit
    val retrofit: Retrofit by lazy {

        Retrofit.Builder()

            // Establecer la dirección del servidor
            .baseUrl(BASE_URL)

            // Convertir las respuestas JSON a objetos Kotlin
            .addConverterFactory(GsonConverterFactory.create())

            // Construir la conexión
            .build()
    }

    // Crear la instancia de la API de clientes
    val clienteEncuestaApi: ClienteEncuestaApi by lazy {

        retrofit.create(ClienteEncuestaApi::class.java)
    }
}