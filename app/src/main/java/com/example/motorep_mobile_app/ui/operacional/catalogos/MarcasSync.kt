package com.example.motorep_mobile_app.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import com.example.motorep_mobile_app.data.database.Motorep_Database
import com.example.motorep_mobile_app.data.remote.dto.Marca_dto
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MarcasSyncWorker(
    context: Context,
    params:  WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Obtener la base de datos local y el servicio API.
            val database = Motorep_Database.getDatabase(applicationContext)
            val marcasDao = database.marcasDao()
            val api = RetrofitClient.create(applicationContext)

            // 1. Sincronizar marcas nuevas y editadas.
            val pendientes = marcasDao.obtenerPendientes()

            for (marca in pendientes) {

                val respuesta = if (marca.serverId == null) {

                    // Registrar una marca nueva en la API.
                    api.guardarMarca(
                        Marca_dto(
                            nombre = marca.nombre,
                            tipoId = marca.TipoId
                        )
                    )

                } else {

                    // Actualizar una marca que ya existe en la API.
                    api.actualizarMarca(
                        marca.serverId,
                        Marca_dto(
                            nombre = marca.nombre,
                            tipoId = marca.TipoId
                        )
                    )
                }

                // Guardar el ID remoto y marcar como sincronizada.
                val idServidor = respuesta.id ?: marca.serverId
                ?: throw Exception("La API no devolvió el ID de la marca")

                marcasDao.marcarComoSincronizada(
                    marca.id,
                    idServidor
                )
            }

            // 2. Procesar marcas pendientes de eliminación.
            val pendientesEliminar = marcasDao.obtenerPendientesEliminar()

            for (marca in pendientesEliminar) {

                if (marca.serverId != null) {
                    api.eliminarMarca(marca.serverId)
                }

                // Eliminar de Room solamente después de confirmar el proceso.
                marcasDao.eliminarLocal(marca.id)
            }

            Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}

// Programa la sincronización para ejecutarse cuando el dispositivo tenga conexión a internet.
object MarcasSyncScheduler {

    fun programar(context: Context) {

        val restricciones = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val solicitud = OneTimeWorkRequestBuilder<MarcasSyncWorker>()
            .setConstraints(restricciones)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "sincronizacion_marcas",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            solicitud
        )
    }
}
