package com.example.motorep_mobile_app.data.repository

import com.example.motorep_mobile_app.data.dao.Cliente_Dao
import com.example.motorep_mobile_app.data.dao.Proveedor_Dao
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.data.entity.Proveedor
import kotlin.collections.contains

class Proveedor_Repository(   // Creamos una clase
    private val dao: Proveedor_Dao   // Hacemos una instancia a la clase ClienteDao y lo guardamos en una variable
) {

    val listaProveedor = dao.ObtenerTodos() // Llamamos a la función ObtenerTodo del dao.
    suspend fun insertar(   // Creamos una función insertar para posteriormente llamarla
        proveedor: Proveedor   // Hacemos la instancia con Cliente
    ){
        dao.Insertar(proveedor)   // Accedemos a la función insertar en ClienteDao
    }
    suspend fun actualizar(   // Creamos una función actualizar para posteriormente llamarla
        proveedor: Proveedor
    ){
        dao.Actualizar(proveedor)   // Accedemos a la función actualizar en ProductoDao
    }
    suspend fun eliminar(   // Creamos una función eliminar para posteriormente llamarla
        proveedor: Proveedor   // Hacemos la instancia con Cliente
    ){
        dao.Eliminar(proveedor)   // Accedemos a la función eliminar en ClienteDao
    }

    // Nuevas funciones para sincronización diferida (offline-first)
    // Obtiene todos los productos que fueron creados o modificados mientras el dispositivo estaba sin conexión a Internet, y que por lo tanto todavía no se han enviado a la API (SQL Server). Se usan para reintentar la sincronización cuando vuelve la conexión.
    suspend fun obtenerPendientesCrear(): List<Proveedor> {
        return dao.obtenerPendientesCrear()
    }

    // Obtiene todos los productos que el usuario eliminó mientras no había  conexión. En vez de borrarlos de Room de inmediato, quedan marcados como "pendientes de eliminar" hasta poder confirmar el borrado en la API.
    suspend fun obtenerPendientesEliminar(): List<Proveedor> {
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

    suspend fun sincronizarDesdeAzure(proveedor: Proveedor) {

        val proveedorExistente = proveedor.serverId?.let {
            dao.obtenerPorServerId(it)
        } ?: dao.obtenerPorSyncId(proveedor.syncId)

        if (proveedorExistente != null) {

            val proveedorActualizado = if (proveedorExistente.sincronizado) {
                proveedor.copy(
                    id = proveedorExistente.id,
                    syncId = proveedorExistente.syncId,
                    sincronizado = true,
                    pendienteEliminar = false
                )
            } else {
                proveedorExistente.copy(
                    serverId = proveedorExistente.serverId ?: proveedor.serverId
                )
            }
            dao.Actualizar(proveedorActualizado)

        } else {

            dao.Insertar(proveedor)
        }
    }

    suspend fun eliminarProveedoresQueYaNoExistenEnAzure(
        proveedoresAzure: List<Proveedor>
    ) {

        val proveedoresLocales = dao.obtenerProveedoresSincronizados()

        if (proveedoresAzure.any { it.serverId == null }) return

        val serverIdsAzure = proveedoresAzure.mapNotNull { it.serverId }.toSet()

        for (proveedorLocal in proveedoresLocales) {

            if (proveedorLocal.serverId !in serverIdsAzure) {

                dao.eliminarPorId(proveedorLocal.id)
            }
        }
    }
}