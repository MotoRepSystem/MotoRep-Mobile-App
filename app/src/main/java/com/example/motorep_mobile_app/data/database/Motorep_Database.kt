
package com.example.motorep_mobile_app.data.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.motorep_mobile_app.data.dao.Cliente_Dao
import com.example.motorep_mobile_app.data.dao.Empleado_Dao
import com.example.motorep_mobile_app.data.dao.Producto_Dao
import com.example.motorep_mobile_app.data.dao.Proveedor_Dao
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.data.entity.Empleado
import com.example.motorep_mobile_app.data.entity.Producto
import com.example.motorep_mobile_app.data.entity.Proveedor

@Database(
    entities = [Cliente::class, Empleado::class, Producto::class, Proveedor::class],
    version = 10
)
abstract class Motorep_Database :
    RoomDatabase() {
    abstract fun clienteDao():
            Cliente_Dao
    abstract fun empleadoDao():
            Empleado_Dao
    abstract fun productoDao():
            Producto_Dao
    abstract fun proveedorDao():
            Proveedor_Dao



    companion object {
        @Volatile
        private var INSTANCE:
                Motorep_Database? = null

        // Migración de la versión 1 a la 2: agrega las columnas nuevas
        // sin borrar los datos existentes.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cliente ADD COLUMN sincronizado INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE cliente ADD COLUMN pendienteEliminar INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE cliente ADD COLUMN serverId INTEGER")
                db.execSQL("ALTER TABLE cliente ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE cliente SET syncId = lower(hex(randomblob(16)))")

                db.execSQL("ALTER TABLE empleado ADD COLUMN sincronizado INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE empleado ADD COLUMN pendienteEliminar INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE empleado ADD COLUMN serverId INTEGER")
                db.execSQL("ALTER TABLE empleado ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE empleado SET syncId = lower(hex(randomblob(16)))")

            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS proveedor (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        syncId TEXT NOT NULL,
                        serverId INTEGER,
                        nombre TEXT NOT NULL,
                        telefono TEXT NOT NULL,
                        sincronizado INTEGER NOT NULL,
                        pendienteEliminar INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {   // X = versión actual, Y = X + 1
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS productos (
                id INTEGER NOT NULL,
                cantidad INTEGER NOT NULL,
                preciocosto TEXT NOT NULL,
                precioventa TEXT NOT NULL,
                detalle-producto TEXT NOT NULL,
                PRIMARY KEY(id)
            )
        """.trimIndent())
            }
        }

        fun getDatabase(
            context: Context
        ): Motorep_Database {
            return INSTANCE ?: synchronized(this){
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        Motorep_Database::class.java,
                        "motorepdatabase"
                    )
                        .addMigrations(MIGRATION_1_2)
                        .addMigrations(MIGRATION_9_10)
                        .fallbackToDestructiveMigration(true)
                        .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
