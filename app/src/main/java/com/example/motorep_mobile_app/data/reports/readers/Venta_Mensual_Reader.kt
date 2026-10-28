package com.example.motorep_mobile_app.data.reports.readers

import android.content.Context
import com.example.motorep_mobile_app.data.reports.dataclass.VentaMensual
import org.json.JSONObject

object Ventamensual_Reader {

    fun leerVentas(context: Context): List<VentaMensual> {
        val jsonTexto = context.assets
            .open("ejemplo_kpi_ventas_mes.json")
            .bufferedReader()
            .use { it.readText() }

        val json = JSONObject(jsonTexto)
        val datosJson = json.getJSONArray("datos")

        val ventas = mutableListOf<VentaMensual>()

        for (i in 0 until datosJson.length()) {
            val registro = datosJson.getJSONObject(i)

            ventas.add(
                VentaMensual(
                    mes = registro.getString("mes"),
                    totalVentas = registro.getDouble("total_ventas")
                )
            )
        }

        return ventas
    }
}
