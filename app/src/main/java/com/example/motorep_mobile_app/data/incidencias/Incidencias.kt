package com.example.motorep_mobile_app.data.incidencias

import com.google.gson.annotations.SerializedName

data class IncidenciaPagina(
    val total: Int,
    val page: Int,
    val limit: Int,
    @SerializedName("total_paginas") val totalPaginas: Int,
    val resultados: List<Incidencia>
)

data class Incidencia(
    @SerializedName("_id") val id: String,
    @SerializedName("fecha_registro") val fechaRegistro: String,
    @SerializedName("tipo_incidencia") val tipoIncidencia: String,
    val origen: Origen?,
    @SerializedName("detalle_incidencia") val detalle: Detalle?,
    val gestion: Gestion?,
    val evidencia: String?
)

data class Origen(
    val tipo: String?,
    @SerializedName("cliente_id") val clienteId: Int?,
    @SerializedName("empleado_id") val empleadoId: Int?,
    @SerializedName("proveedor_id") val proveedorId: Int?,
    val referencia: String?
) {
    fun texto(): String = when (tipo) {
        "cliente" -> "Cliente #$clienteId"
        "empleado" -> "Empleado #$empleadoId"
        "proveedor" -> "Proveedor #$proveedorId"
        "interno" -> "Interno: ${referencia.orEmpty()}"
        else -> "Desconocido"
    }
}

data class Detalle(
    @SerializedName("categoria_problema") val categoria: String?,
    val descripcion: String?,
    @SerializedName("monto_involucrado") val monto: String?
)

data class Gestion(
    @SerializedName("empleado_id") val empleadoId: Int?,
    val prioridad: Int?
)