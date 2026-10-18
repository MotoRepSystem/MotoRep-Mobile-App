package com.example.motorep_mobile_app.data.repository
import com.example.motorep_mobile_app.data.dao.Cliente_Dao  // Importamos la clase ClienteDao
import com.example.motorep_mobile_app.data.entity.Cliente   // Importamos el modelo Cliente

class Cliente_Repository(   // Creamos una clase
    private val dao: Cliente_Dao   // Hacemos una instancia a la clase ClienteDao y lo guardamos en una variable
) {

    val listaCliente = dao.ObtenerTodos() // Llamamos a la función ObtenerTodo del dao.
    suspend fun insertar(   // Creamos una función insertar para posteriormente llamarla
        cliente: Cliente   // Hacemos la instancia con Cliente
    ){
        dao.Insertar(cliente)   // Accedemos a la función insertar en ClienteDao
    }
    suspend fun actualizar(   // Creamos una función actualizar para posteriormente llamarla
        cliente: Cliente
    ){
        dao.Actualizar(cliente)   // Accedemos a la función actualizar en ProductoDao
    }
    suspend fun eliminar(   // Creamos una función eliminar para posteriormente llamarla
        cliente: Cliente   // Hacemos la instancia con Cliente
    ){
        dao.Eliminar(cliente)   // Accedemos a la función eliminar en ClienteDao
    }

    // Nuevas funciones para sincronización diferida (offline-first)
    // Obtiene todos los productos que fueron creados o modificados mientras el dispositivo estaba sin conexión a Internet, y que por lo tanto todavía no se han enviado a la API (SQL Server). Se usan para reintentar la sincronización cuando vuelve la conexión.
    suspend fun obtenerPendientesCrear(): List<Cliente> {
        return dao.obtenerPendientesCrear()
    }

    // Obtiene todos los productos que el usuario eliminó mientras no había  conexión. En vez de borrarlos de Room de inmediato, quedan marcados como "pendientes de eliminar" hasta poder confirmar el borrado en la API.
    suspend fun obtenerPendientesEliminar(): List<Cliente> {
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

    suspend fun sincronizarDesdeAzure(cliente: Cliente) {

        val clienteExistente = cliente.serverId?.let {
            dao.obtenerPorServerId(it)
        } ?: dao.obtenerPorSyncId(cliente.syncId)
            ?: cliente.cedula.takeIf { it.isNotBlank() }?.let {
                dao.obtenerPendientePorCedula(it)
            }

        if (clienteExistente != null) {

            val clienteActualizado = if (clienteExistente.sincronizado) {
                cliente.copy(
                    id = clienteExistente.id,
                    syncId = clienteExistente.syncId,
                    sincronizado = true,
                    pendienteEliminar = false
                )
            } else {
                clienteExistente.copy(
                    serverId = clienteExistente.serverId ?: cliente.serverId
                )
            }
            dao.Actualizar(clienteActualizado)

        } else {

            dao.Insertar(cliente)
        }
    }

    suspend fun eliminarClientesQueYaNoExistenEnAzure(
        clientesAzure: List<Cliente>
    ) {

        val clientesLocales = dao.obtenerClientesSincronizados()

        if (clientesAzure.any { it.serverId == null }) return

        val serverIdsAzure = clientesAzure.mapNotNull { it.serverId }.toSet()

        for (clienteLocal in clientesLocales) {

            if (clienteLocal.serverId !in serverIdsAzure) {

                dao.eliminarPorId(clienteLocal.id)
            }
        }
    }
}
