package com.example.motorep_mobile_app.data.remote.api
import com.example.motorep_mobile_app.data.entity.Empleado
import com.example.motorep_mobile_app.data.remote.dto.Cliente_dto
import com.example.motorep_mobile_app.data.remote.dto.Empleado_dto
import com.example.motorep_mobile_app.data.remote.dto.LoginRequest
import com.example.motorep_mobile_app.data.remote.dto.LoginResponse
import com.google.gson.JsonElement
import okhttp3.Response
import retrofit2.http.*

interface ApiService {
    @GET("catalogos/clientes")
    suspend fun obtenerCliente(): List<Cliente_dto>

    @POST("catalogos/clientes")
    suspend fun guardarCliente(@Body cliente: Cliente_dto): Cliente_dto

    @PATCH("catalogos/clientes/{id}")
    suspend fun actualizarCliente(@Path("id") id: Int, @Body cliente: Cliente_dto)

    @DELETE("catalogos/clientes/{id}")
    suspend fun eliminarCliente(@Path("id") id: Int)

    @POST("api/token/")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    //Apis de
    @GET("catalogos/empleados")
    suspend fun obtenerEmpleado(): List<Empleado_dto>

    @POST("catalogos/empleados/")
    suspend fun guardarEmpleado(@Body empleado: Empleado_dto): Empleado_dto

    @PATCH("catalogos/empleados/{id}")
    suspend fun actualizarEmpleado(@Path("id") id: Int, @Body empleado: Empleado_dto)

    @DELETE("catalogos/empleados/{id}")
    suspend fun eliminarEmpleado(@Path("id") id: Int)
}
