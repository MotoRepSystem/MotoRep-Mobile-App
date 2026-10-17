package com.example.motorep_mobile_app.data.remote.api
import com.example.motorep_mobile_app.data.remote.dto.Cliente_dto
import com.example.motorep_mobile_app.data.remote.dto.LoginRequest
import com.example.motorep_mobile_app.data.remote.dto.LoginResponse
import retrofit2.http.*

interface ApiService {
    @GET("catalogos/clientes/")
    suspend fun obtenerCliente(): List<Cliente_dto>

    @POST("catalogos/clientes/")
    suspend fun guardarCliente(@Body cliente: Cliente_dto): Cliente_dto

    @PUT("catalogos/clientes/{id}/")
    suspend fun actualizarCliente(@Path("id") id: Int, @Body cliente: Cliente_dto): Cliente_dto

    @DELETE("catalogos/clientes/{id}/")
    suspend fun eliminarCliente(@Path("id") id: Int)

    @POST("api/token/")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
