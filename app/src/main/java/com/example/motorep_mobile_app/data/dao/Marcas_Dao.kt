package com.example.motorep_mobile_app.data.dao

// Permite observar los cambios de la base de datos en tiempo real.
import androidx.lifecycle.LiveData

// Anotaciones y herramientas de Room.
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

// Entidad local de marcas.
import com.example.motorep_mobile_app.data.entity.Marcas

// Define las operaciones para trabajar con la tabla marcas.
@Dao
interface Marcas_Dao {

    // Obtiene las marcas que no están pendientes de eliminación.
    // LiveData permite actualizar la información automáticamente en la pantalla.
    @Query("SELECT * FROM marcas WHERE pendienteEliminar = 0 ORDER BY nombre ASC")
    fun obtenerMarcas(): LiveData<List<Marcas>>

    // Obtiene las marcas que todavía no se han sincronizado con el servidor.
    @Query("SELECT * FROM marcas WHERE sincronizado = 0 AND pendienteEliminar = 0")
    suspend fun obtenerPendientes(): List<Marcas>

    // Obtiene las marcas cuya eliminación todavía está pendiente de sincronizar.
    @Query("SELECT * FROM marcas WHERE pendienteEliminar = 1")
    suspend fun obtenerPendientesEliminar(): List<Marcas>

    // Obtiene las marcas sincronizadas que tienen un identificador del servidor.
    @Query("""
        SELECT * FROM marcas
        WHERE sincronizado = 1
        AND pendienteEliminar = 0
        AND serverId IS NOT NULL
    """)
    suspend fun obtenerMarcasSincronizadas(): List<Marcas>

    // Inserta una marca en la base de datos local.
    // Si ya existe el mismo ID, reemplaza el registro.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(marca: Marcas)

    // Inserta varias marcas en una sola operación.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(marcas: List<Marcas>)

    // Actualiza una marca existente.
    @Update
    suspend fun actualizar(marca: Marcas)

    // Busca una marca por su identificador local.
    @Query("SELECT * FROM marcas WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Marcas?

    // Busca una marca utilizando el ID que tiene en el servidor.
    @Query("SELECT * FROM marcas WHERE serverId = :serverId LIMIT 1")
    suspend fun buscarPorServerId(serverId: Int): Marcas?

    // Marca una marca como sincronizada y guarda su ID del servidor.
    @Query("""
        UPDATE marcas
        SET sincronizado = 1,
            pendienteEliminar = 0,
            serverId = :serverId
        WHERE id = :id
    """)
    suspend fun marcarComoSincronizada(id: Int, serverId: Int)

    // Elimina una marca únicamente de la base de datos local.
    @Query("DELETE FROM marcas WHERE id = :id")
    suspend fun eliminarLocal(id: Int)

    // Elimina un registro local utilizando el ID del servidor.
    @Query("DELETE FROM marcas WHERE serverId = :serverId")
    suspend fun eliminarPorServerId(serverId: Int)
}
