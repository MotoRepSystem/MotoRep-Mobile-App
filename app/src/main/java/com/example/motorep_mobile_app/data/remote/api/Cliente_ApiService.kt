package com.example.motorep_mobile_app.data.remote.api
import com.example.motorep_mobile_app.data.remote.dto.Cliente_dto
import com.example.motorep_mobile_app.data.remote.dto.Empleado_dto
import com.example.motorep_mobile_app.data.remote.dto.LoginRequest
import com.example.motorep_mobile_app.data.remote.dto.LoginResponse
import com.example.motorep_mobile_app.data.remote.dto.Producto_dto
import com.example.motorep_mobile_app.data.remote.dto.Proveedor_dto
import com.example.motorep_mobile_app.data.remote.dto.Marca_dto
import com.example.motorep_mobile_app.data.remote.dto.TipoMarca_dto
import retrofit2.http.*

interface ApiService {
    @GET("catalogos/clientes")
    suspend fun obtenerCliente(): List<Cliente_dto>

    @POST("catalogos/clientes/")
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

    //Endpoints de productos
    @GET("movimiento/productos/inventarioLote/")
    suspend fun obtenerProductos(): List<Producto_dto>

    //Proveedores
    @GET("catalogos/proveedor")
    suspend fun obtenerProveedores(): List<Proveedor_dto>
    @POST("catalogos/proveedor/")
    suspend fun guardarProveedor(@Body proveedor: Proveedor_dto): Proveedor_dto
    @PATCH("catalogos/proveedor/{id}")
    suspend fun actualizarProveedor(@Path("id") id: Int, @Body proveedor: Proveedor_dto)
    @DELETE("catalogos/proveedor/{id}")
    suspend fun eliminarProveedor(@Path("id") id: Int)

    //Endpoints de marca
    // Consulta los tipos de marcas registrados.
    @GET("catalogos/tipo/")
    suspend fun obtenerTiposMarca(): List<TipoMarca_dto>

    // Consulta las marcas activas.
    @GET("movimiento/marcas/")
    suspend fun obtenerMarcas(): List<Marca_dto>

    // Registra una marca nueva.
    @POST("movimiento/marcas/")
    suspend fun guardarMarca(@Body marca: Marca_dto): Marca_dto

    // Actualiza una marca existente.
    @PATCH("movimiento/marcas/{id}")
    suspend fun actualizarMarca(@Path("id") id: Int, @Body marca: Marca_dto): Marca_dto

    // Elimina lógicamente una marca.
    @DELETE("movimiento/marcas/{id}")
    suspend fun eliminarMarca(@Path("id") id: Int)

}
