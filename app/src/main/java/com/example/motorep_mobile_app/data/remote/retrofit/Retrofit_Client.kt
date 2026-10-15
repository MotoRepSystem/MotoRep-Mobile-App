package com.example.motorep_mobile_app.data.remote.retrofit
import android.content.Context
import com.example.motorep_mobile_app.data.remote.api.ApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://motorepweb-api-h3ahb3c6hgfebpa6.centralus-01.azurewebsites.net/"

    fun create(context: Context): ApiService {

        // Crea el cliente HTTP que utilizará Retrofit.
        val client = OkHttpClient.Builder()
            .addInterceptor(
                AuthInterceptor(context)
            )
            .build()
        // Configura Retrofit con el cliente HTTP.
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}