package com.example.motorep_mobile_app.ui.encuesta

// Representa una pregunta de la encuesta.
data class Pregunta(
    val id: String? = null,

    // Texto que verá el cliente.
    val texto: String = "",

    // Tipos: opcion_unica, opcion_multiple o texto.
    val tipo: String = "",

    // Campo de MongoDB al que corresponde la respuesta.
    val campoDestino: String? = null,

    // Opciones disponibles para responder.
    val opciones: List<Opcion> = emptyList()
) {

    // Representa una opción de respuesta.
    data class Opcion(
        val texto: String = "",
        val valor: Int = 0
    )

    // Comprueba que existan opciones con valores numéricos positivos.
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
