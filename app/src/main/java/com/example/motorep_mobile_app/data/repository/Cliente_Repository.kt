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

}