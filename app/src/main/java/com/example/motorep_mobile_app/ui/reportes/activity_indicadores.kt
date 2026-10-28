
package com.example.motorep_mobile_app.ui.reportes

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.ui.common.MenuLateral
import android.util.Log
import com.example.motorep_mobile_app.data.reports.dataclass.InventarioCategoria
import com.example.motorep_mobile_app.data.reports.dataclass.VentaSatisfaccion
import com.example.motorep_mobile_app.data.reports.readers.Inventariocategoria_Reader
import com.example.motorep_mobile_app.data.reports.readers.Ventasatisfaccion_Reader

//importamos el repo y los recursos chart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class activity_indicadores : AppCompatActivity() {

    //declaramos variable de barchart
    private lateinit var chartVentas: BarChart


    private lateinit var chartInventarioCategoria: BarChart




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_indicadores)

        // Configurar el menú lateral y los permisos según el rol.
        MenuLateral(this).configurar()

        //buscamos por id el gráfico
        chartVentas = findViewById(R.id.chartVentas)
        //ejecutamos la fucion
        try {
            val ventas = Ventasatisfaccion_Reader.leerVentas(this)

            configurarGrafico(ventas)

            Log.d(
                "KPI_VENTAS",
                "Gráfico cargado con ${ventas.size} registros"
            )

        } catch (e: Exception) {
            Log.e(
                "KPI_VENTAS",
                "No se pudieron cargar las ventas",
                e
            )
        }




        chartInventarioCategoria =
            findViewById(R.id.chartInventarioCategoria)

        val inventario =
            Inventariocategoria_Reader.leerInventario(this)

        configurarGraficoInventario(inventario)

    }

    private fun configurarGrafico(
        ventas: List<VentaSatisfaccion>
    ) {
        // 1. Crear las etiquetas del eje X.
        val etiquetas = ventas.map {
            it.nivelSatisfaccion
        }

        // 2. Convertir los registros a BarEntry.
        val entradas = ventas.mapIndexed { indice, venta ->
            BarEntry(
                indice.toFloat(),
                venta.totalVentas.toFloat()
            )
        }

        // 3. Crear la serie de datos.
        val conjunto = BarDataSet(
            entradas,
            "Ventas (NIO)"
        ).apply {
            color = Color.rgb(218, 180, 90)
            valueTextColor = Color.DKGRAY
            valueTextSize = 10f
            setDrawValues(true)
        }

        // 4. Entregar los datos al gráfico.
        chartVentas.data = BarData(conjunto)

        // 5. Configurar los ejes.
        chartVentas.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(etiquetas)
            granularity = 1f
            setDrawGridLines(false)
            labelRotationAngle = -15f
        }

        chartVentas.axisRight.isEnabled = false
        chartVentas.axisLeft.axisMinimum = 0f

        // 6. Ajustar la apariencia.
        chartVentas.description.isEnabled = false
        chartVentas.legend.isEnabled = true
        chartVentas.setFitBars(true)
        chartVentas.setNoDataText("No hay datos disponibles")

        // 7. Redibujar.
        chartVentas.invalidate()
    }








    private fun configurarGraficoInventario(
        inventario: List<InventarioCategoria>
    ) {
        val etiquetas = inventario.map { it.categoria }

        val entradas = inventario.mapIndexed { indice, item ->
            BarEntry(
                indice.toFloat(),
                item.cantidad.toFloat()
            )
        }

        val conjunto = BarDataSet(
            entradas,
            "Unidades en inventario"
        ).apply {
            color = android.graphics.Color.rgb(218, 180, 90)
            valueTextSize = 10f
            setDrawValues(true)
        }

        chartInventarioCategoria.data = BarData(conjunto)

        chartInventarioCategoria.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(etiquetas)
            granularity = 1f
            setDrawGridLines(false)
            labelRotationAngle = -25f
        }

        chartInventarioCategoria.axisRight.isEnabled = false
        chartInventarioCategoria.axisLeft.axisMinimum = 0f
        chartInventarioCategoria.description.isEnabled = false
        chartInventarioCategoria.legend.isEnabled = true
        chartInventarioCategoria.setFitBars(true)
        chartInventarioCategoria.setNoDataText(
            "No hay datos de inventario"
        )
        chartInventarioCategoria.invalidate()
    }


}
