package com.example.motorep_mobile_app.data.repository

import com.google.gson.JsonParseException
import com.example.motorep_mobile_app.data.incidencias.Incidencia
import com.example.motorep_mobile_app.data.incidencias.IncidenciaPagina
import com.example.motorep_mobile_app.data.remote.api.ApiService
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

class IncidenciaRepository(
    private val api: ApiService
) {

    suspend fun obtenerIncidencias(
        page: Int = 1,
        limit: Int = 20,
        prioridad: Int? = null,
        tipoIncidencia: String? = null
    ): Result<IncidenciaPagina> = llamar {
        api.getIncidencias(page, limit, prioridad, tipoIncidencia)
    }

    private suspend fun <T> llamar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: CancellationException) {
            throw e // nunca tragues la cancelación de una corrutina
        } catch (e: HttpException) {
            Result.failure(Exception(mensajeHttp(e.code()), e))
        } catch (e: IOException) {
            Result.failure(Exception("No hay conexión con el servidor.", e))
        } catch (e: JsonParseException) {
            Result.failure(Exception("La respuesta del servidor no tiene el formato esperado.", e))
        } catch (e: Exception) {
            Result.failure(Exception("Ocurrió un error inesperado.", e))
        }

    private fun mensajeHttp(codigo: Int): String = when (codigo) {
        401 -> "Tu sesión expiró. Inicia sesión de nuevo."
        403 -> "No tienes permiso para ver las incidencias."
        404 -> "La incidencia no existe."
        in 500..599 -> "El servidor tuvo un problema. Intenta más tarde."
        else -> "Error de comunicación (código $codigo)."
    }
}