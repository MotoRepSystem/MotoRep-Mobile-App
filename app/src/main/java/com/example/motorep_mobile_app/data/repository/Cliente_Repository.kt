package com.example.motorep_mobile_app.data.repository
import com.example.motorep_mobile_app.data.dao.ClienteDao
import com.example.motorep_mobile_app.data.entity.Cliente

class Cliente_Repository(private val dao: ClienteDao) {
    val listaClientes = dao.ObtenerTodos()
    suspend fun insertarCliente(cliente: Cliente) {
        dao.Insertar(cliente)
    }
    suspend fun actualizarCliente(cliente: Cliente) {
        dao.Actualizar(cliente)
    }
    suspend fun eliminarCliente(cliente: Cliente) {
        dao.Eliminar(cliente)
    }

    // Elimina definitivamente un cliente de la base de datos local (Room). Se llama después de confirmar que el DELETE remoto (hacia la API) se completó con éxito, cerrando el ciclo de eliminación diferida.
    suspend fun eliminarPorId(id: Int) {
        dao.eliminarPorId(id)
    }
    // Nuevas funciones para sincronización diferida (offline-first)
    // Obtiene todos los clientes que fueron creados o modificados mientras el dispositivo estaba sin conexión a Internet, y que por lo tanto todavía no se han enviado a la API (SQL Server). Se usan para reintentar la sincronización cuando vuelve la conexión.
    suspend fun obtenerPendientesCrear(): List<Cliente> {
        return dao.obtenerPendientesCrear()
    }
    // Obtiene todos los clientes que el usuario eliminó mientras no había  conexión. En vez de borrarlos de Room de inmediato, quedan marcados como "pendientes de eliminar" hasta poder confirmar el borrado en la API.
    suspend fun obtenerPendientesEliminar(): List<Cliente> {
        return dao.obtenerPendientesEliminar()
    }
    // Marca un cliente como sincronizado exitosamente con la API. Se llama después de que el reintento de creación/actualización remota tuvo éxito, para que ese cliente deje de aparecer en la lista de pendientes.
    suspend fun marcarComoSincronizado(syncId: String, serverId: Int) {
        dao.marcarComoSincronizado(syncId, serverId)
    }
    suspend fun sincronizarDesdeAzure(cliente: Cliente) {
        val clienteExistente = if (cliente.serverId != null && cliente.serverId != 0) {
            dao.obtenerPorServerId(cliente.serverId) ?: dao.obtenerPorSyncId(cliente.syncId)
        } else {
            dao.obtenerPorSyncId(cliente.syncId)
        }

        if (clienteExistente != null) {
            // Mantener el syncId local si ya existía para no perderlo aunque Azure devuelva syncId = null
            val syncIdPreservado = if (clienteExistente.syncId.isNotBlank()) {
                clienteExistente.syncId
            } else {
                cliente.syncId
            }

            val clienteActualizado =
                cliente.copy(
                    id = clienteExistente.id,
                    syncId = syncIdPreservado,
                    sincronizado = true,
                    pendienteEliminar = false
                )
            dao.Actualizar(clienteActualizado)
        } else { 
            dao.Insertar(cliente) 
        }
    }

    suspend fun eliminarClientesQueYaNoExistenEnAzure(
        clientesAzure: List<Cliente>
    ) {
        val clientesLocales = dao.obtenerClientesSincronizados()
        val syncIdsAzure = clientesAzure.map { it.syncId }.toSet()
        val serverIdsAzure = clientesAzure.mapNotNull { it.serverId }.toSet()

        for (clienteLocal in clientesLocales) {
            val existeEnAzure = clienteLocal.syncId in syncIdsAzure ||
                    (clienteLocal.serverId != null && clienteLocal.serverId in serverIdsAzure)

            if (!existeEnAzure) { 
                dao.eliminarPorId(clienteLocal.id) 
            }
        }
    }

}