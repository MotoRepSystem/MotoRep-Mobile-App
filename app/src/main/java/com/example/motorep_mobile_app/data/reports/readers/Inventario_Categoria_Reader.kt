package com.example.motorep_mobile_app.data.reports.readers

import android.content.Context
import com.example.motorep_mobile_app.data.reports.dataclass.InventarioCategoria
import org.json.JSONObject

object Inventariocategoria_Reader {

    fun leerInventario(
        context: Context
    ): List<InventarioCategoria> {

        val jsonTexto = context.assets
            .open("ejemplo_kpi_inventario_categoria.json")
            .bufferedReader()
            .use { it.readText() }

        val json = JSONObject(jsonTexto)
        val datosJson = json.getJSONArray("datos")

        val inventario = mutableListOf<InventarioCategoria>()

        for (i in 0 until datosJson.length()) {
            val registro = datosJson.getJSONObject(i)

            inventario.add(
                InventarioCategoria(
                    categoria = registro.getString("categoria"),
                    cantidad = registro.getInt("cantidad")
                )
            )
        }

        return inventario
    }
}
