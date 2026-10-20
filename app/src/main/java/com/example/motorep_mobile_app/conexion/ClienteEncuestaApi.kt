package com.example.motorep_mobile_app.conexion

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Datos que Android enviará a Django.
// El backend recibe el nombre completo en un solo campo.
data class BuscarClienteRequest(
    val nombre_completo: String
)

// Información de cada factura que Django devuelve.
data class FacturaEncuesta(
    val factura_id: Int,
    val numero_factura: Int,
    val fecha: String,
    val total: String
)

// Respuesta que Django devuelve al buscar el cliente.
data class BuscarClienteResponse(
    val cliente_id: Int,
    val nombres: String,
    val apellidos: String,
    val facturas: List<FacturaEncuesta>
)

// Respuestas relacionadas con la satisfacción del servicio.
data class SatisfaccionServicioRequest(
    val satisfaccion_general: String,
    val atencion_personal: String,
    val experiencia_compra: String
)

// Respuestas relacionadas con las preferencias del cliente.
data class PreferenciasUsoRequest(
    val facilidad_encontrar_producto: String,
    val variedad_productos: String,
    val marca_preferida: String
)

// Datos completos que Android enviará a Django.
data class EncuestaRequest(
    val cliente_id: Int,
    val factura_id: Int,
    val satisfaccion_servicio: SatisfaccionServicioRequest,
    val preferencias_uso: PreferenciasUsoRequest,
    val productos_frecuentes: List<String>,
    val comentario_servicio: String,
    val sugerencia: String
)

// Respuesta que Django devuelve después de guardar.
data class EncuestaCrearResponse(
    val mensaje: String,
    val encuesta_id: String
)

// Definir la operación para buscar al cliente.
interface ClienteEncuestaApi {

    @POST("catalogos/clientes/encuesta/buscar")
    fun buscarCliente(
        @Body datos: BuscarClienteRequest
    ): Call<BuscarClienteResponse>

    // Registrar una encuesta en Django.
    @POST("encuestas/crear/")
    fun crearEncuesta(
        @Body datos: EncuestaRequest
    ): Call<EncuestaCrearResponse>
}
