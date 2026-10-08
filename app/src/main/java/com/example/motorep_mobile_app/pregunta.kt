package com.example.motorep_mobile_app

data class Pregunta(
    val id: String? = null,

    val texto: String = "",

    // Puede ser: "opcion_unica", "opcion_multiple" o "texto"
    val tipo: String = "",

    // Ejemplo:
    // "satisfaccion_servicio.experiencia_compra"
    val campoDestino: String? = null,

    val opciones: List<Opcion> = emptyList()
) {

    /**
     * Representa una opción de respuesta.
     */
    data class Opcion(
        val texto: String = "",
        val valor: Int = 0
    )

    /**
     * Determina si todas las opciones tienen
     * un valor numérico válido.
     */
    fun esNumerica(): Boolean {

        if (opciones.isEmpty()) {
            return false
        }

        for (opcion in opciones) {
            if (opcion.valor <= 0) {
                return false
            }
        }

        return true
    }
}