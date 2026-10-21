package com.example.motorep_mobile_app.data.entity

import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

data class DetalleParts(
    val producto: String,
    val marca: String,
    val moto: String
)

//funcion para dividir el detalle, en nombre, marca y moto
fun Producto.partesDetalle(): DetalleParts {
    val partes = detalle.split(" - ", limit = 3).map { it.trim() }
    return DetalleParts(
        producto = partes.getOrElse(0) { detalle },
        marca = partes.getOrElse(1) { "—" },
        moto = partes.getOrElse(2) { "—" }
    )
}

fun Producto.margen(): BigDecimal =
    (precioVenta.toBigDecimalOrNull() ?: BigDecimal.ZERO) -
            (precioCosto.toBigDecimalOrNull() ?: BigDecimal.ZERO)

fun Producto.valorStock(): BigDecimal =
    (precioVenta.toBigDecimalOrNull() ?: BigDecimal.ZERO) * BigDecimal(cantidad)

fun Producto.fechaFormateada(): String = try {
    OffsetDateTime.parse(fechaRegistro)
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
} catch (e: Exception) { fechaRegistro }
