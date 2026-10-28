package com.example.motorep_mobile_app.data.reports.readers

import android.content.Context
import com.example.motorep_mobile_app.data.reports.dataclass.VentaMetodoPago
import org.json.JSONObject

object Ventametodopago_Reader {

    fun leerVentas(
        context: Context
    ): List<VentaMetodoPago> {

        val texto = context.assets
            .open("ejemplo_kpi_metodo_pago.json")
            .bufferedReader()
            .use { it.readText() }

        val json = JSONObject(texto)
        val datos = json.getJSONArray("datos")

        val ventas = mutableListOf<VentaMetodoPago>()

        for (i in 0 until datos.length()) {
            val registro = datos.getJSONObject(i)

            ventas.add(
                VentaMetodoPago(
                    metodoPago = registro.getString("metodo_pago"),
                    totalVentas = registro.getDouble("total_ventas")
                )
            )
        }

        return ventas
    }
}
