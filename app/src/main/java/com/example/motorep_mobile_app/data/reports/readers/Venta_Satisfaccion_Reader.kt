package com.example.motorep_mobile_app.data.reports.readers

import android.content.Context
import com.example.motorep_mobile_app.data.reports.dataclass.VentaSatisfaccion
import org.json.JSONObject

//esta funcion tiene como objetivo leer el json proveniente con lo datos, luego almacenar los datos en la clase Ventas satisfaccion
object Ventasatisfaccion_Reader {

    fun leerVentas(context: Context): List<VentaSatisfaccion> {

        val jsonTexto = context.assets
            .open("ejemplo_kpi_ventas_satisfaccion.json")
            .bufferedReader()
            .use { it.readText() }

        val jsonObjeto = JSONObject(jsonTexto)
        val jsonDatos = jsonObjeto.getJSONArray("datos")

        val ventas = mutableListOf<VentaSatisfaccion>()

        for (i in 0 until jsonDatos.length()) {

            val registro = jsonDatos.getJSONObject(i)

            val nivel = registro.getString(
                "nivel_satisfaccion"
            )

            val total = registro.getDouble(
                "total_ventas"
            )

            ventas.add(
                VentaSatisfaccion(
                    nivelSatisfaccion = nivel,
                    totalVentas = total
                )
            )
        }

        return ventas
    }
}
