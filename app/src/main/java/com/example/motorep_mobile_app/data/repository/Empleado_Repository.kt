package com.example.motorep_mobile_app.data.repository
import com.example.motorep_mobile_app.data.dao.Empleado_Dao
import com.example.motorep_mobile_app.data.entity.Empleado

class Empleado_Repository(   // Creamos una clase
    private val dao: Empleado_Dao   // Hacemos una instancia a la clase EmpleadoDao y lo guardamos en una variable
) {

    val listaEmpleado = dao.ObtenerTodos() // Llamamos a la función ObtenerTodo del dao.
    suspend fun insertar(   // Creamos una función insertar para posteriormente llamarla
        empleado: Empleado   // Hacemos la instancia con Empleado
    ){
        dao.Insertar(empleado = empleado)   // Accedemos a la función insertar en EmpleadoDao
    }
    suspend fun actualizar(   // Creamos una función actualizar para posteriormente llamarla
        empleado: Empleado
    ){
        dao.Actualizar(empleado)   // Accedemos a la función actualizar en EmpleadoDao
    }
    suspend fun eliminar(   // Creamos una función eliminar para posteriormente llamarla
        empleado: Empleado   // Hacemos la instancia con Empleado
    ){
        dao.Eliminar(empleado)   // Accedemos a la función eliminar en EmpleadoDao
    }

    // Nuevas funciones para sincronización diferida (offline-first)
    // Obtiene todos los productos que fueron creados o modificados mientras el dispositivo estaba sin conexión a Internet, y que por lo tanto todavía no se han enviado a la API (SQL Server). Se usan para reintentar la sincronización cuando vuelve la conexión.
    suspend fun obtenerPendientesCrear(): List<Empleado> {
        return dao.obtenerPendientesCrear()
    }

    // Obtiene todos los productos que el usuario eliminó mientras no había  conexión. En vez de borrarlos de Room de inmediato, quedan marcados como "pendientes de eliminar" hasta poder confirmar el borrado en la API.
    suspend fun obtenerPendientesEliminar(): List<Empleado> {
        return dao.obtenerPendientesEliminar()
    }

    // Marca un producto como sincronizado exitosamente con la API. Se llama después de que el reintento de creación/actualización remota tuvo éxito, para que ese producto deje de aparecer en la lista de pendientes.
    suspend fun marcarComoSincronizado(syncId: String, serverId: Int) {
        dao.marcarComoSincronizado(syncId, serverId)
    }

    // Elimina definitivamente un producto de la base de datos local (Room). Se llama después de confirmar que el DELETE remoto (hacia la API) se completó con éxito, cerrando el ciclo de eliminación diferida.
    suspend fun eliminarPorId(id: Int) {
        dao.eliminarPorId(id)
    }

    suspend fun sincronizarDesdeAzure(empleado: Empleado) {

        val empleadoExistente = empleado.serverId?.let {
            dao.obtenerPorServerId(it)
        } ?: dao.obtenerPorSyncId(empleado.syncId)
        ?: empleado.cedula.takeIf { it.isNotBlank() }?.let {
            dao.obtenerPendientePorCedula(it)
        }

        if (empleadoExistente != null) {

            val empleadoActualizado = if (empleadoExistente.sincronizado) {
                empleado.copy(
                    id = empleadoExistente.id,
                    syncId = empleadoExistente.syncId,
                    sincronizado = true,
                    pendienteEliminar = false
                )
            } else {
                empleadoExistente.copy(
                    serverId = empleadoExistente.serverId ?: empleado.serverId
                )
            }
            dao.Actualizar(empleadoActualizado)

        } else {

            dao.Insertar(empleado)
        }
    }

    suspend fun eliminarEmpleadosQueYaNoExistenEnAzure(
        empleadosAzure: List<Empleado>
    ) {

        val empleadosLocales = dao.obtenerEmpleadosSincronizados()

        if (empleadosAzure.any { it.serverId == null }) return

        val serverIdsAzure = empleadosAzure.mapNotNull { it.serverId }.toSet()

        for (empleadoLocal in empleadosLocales) {

            if (empleadoLocal.serverId !in serverIdsAzure) {

                dao.eliminarPorId(empleadoLocal.id)
            }
        }
    }
}
