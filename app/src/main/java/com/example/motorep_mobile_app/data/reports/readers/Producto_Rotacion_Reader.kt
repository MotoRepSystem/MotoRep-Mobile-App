package com.example.motorep_mobile_app.data.reports.readers


import android.content.Context
import com.example.motorep_mobile_app.data.reports.dataclass.ProductoRotacion
import org.json.JSONObject

object Productorotacion_Reader {

    fun leerProductos(
        context: Context
    ): List<ProductoRotacion> {

        val texto = context.assets
            .open("ejemplo_kpi_productos_rotacion.json")
            .bufferedReader()
            .use { it.readText() }

        val json = JSONObject(texto)
        val datos = json.getJSONArray("datos")

        val productos = mutableListOf<ProductoRotacion>()

        for (i in 0 until datos.length()) {
            val registro = datos.getJSONObject(i)

            productos.add(
                ProductoRotacion(
                    producto = registro.getString("producto"),
                    unidadesVendidas =
                        registro.getInt("unidades_vendidas")
                )
            )
        }

        return productos
    }
}
