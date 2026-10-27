
package com.example.motorep_mobile_app.ui.reportes

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.data.reports.dataclass.ProductoRotacion
import com.example.motorep_mobile_app.data.reports.dataclass.VentaMensual
import com.example.motorep_mobile_app.data.reports.dataclass.VentaMetodoPago
import com.example.motorep_mobile_app.data.reports.readers.Productorotacion_Reader
import com.example.motorep_mobile_app.data.reports.readers.Ventamensual_Reader
import com.example.motorep_mobile_app.data.reports.readers.Ventametodopago_Reader
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class activity_tendencias : AppCompatActivity() {


    private lateinit var chartVentasMes: LineChart

    private lateinit var chartMetodoPago: PieChart

    private lateinit var chartProductosRotacion: HorizontalBarChart



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tendencias)

        // Configurar menú lateral y permisos por rol.
        MenuLateral(this).configurar()


        chartVentasMes = findViewById(R.id.chartVentasMes)

        val ventasMensuales =
            Ventamensual_Reader.leerVentas(this)

        configurarGraficoVentasMes(ventasMensuales)




        chartMetodoPago = findViewById(R.id.chartMetodoPago)

        val ventasMetodoPago =
            Ventametodopago_Reader.leerVentas(this)

        configurarGraficoMetodoPago(ventasMetodoPago)



        chartProductosRotacion =
            findViewById(R.id.chartProductosRotacion)

        val productosRotacion =
            Productorotacion_Reader.leerProductos(this)

        configurarGraficoRotacion(productosRotacion)


    }

    private fun configurarGraficoVentasMes(
    ventas: List<VentaMensual>
    ) {
        val etiquetas = ventas.map { it.mes }

        val entradas = ventas.mapIndexed { indice, venta ->
            Entry(
                indice.toFloat(),
                venta.totalVentas.toFloat()
            )
        }

        val conjunto = LineDataSet(
            entradas,
            "Ventas mensuales (NIO)"
        ).apply {
            color = Color.rgb(218, 180, 90)
            setCircleColor(Color.rgb(218, 180, 90))
            lineWidth = 2.5f
            circleRadius = 5f
            valueTextSize = 10f
            setDrawValues(true)
        }

        chartVentasMes.data = LineData(conjunto)

        chartVentasMes.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(etiquetas)
            granularity = 1f
            setDrawGridLines(false)
            labelRotationAngle = -25f
        }

        chartVentasMes.axisRight.isEnabled = false
        chartVentasMes.axisLeft.axisMinimum = 0f
        chartVentasMes.description.isEnabled = false
        chartVentasMes.legend.isEnabled = true
        chartVentasMes.setNoDataText("No hay datos de ventas")
        chartVentasMes.invalidate()
    }





    private fun configurarGraficoMetodoPago(
        ventas: List<VentaMetodoPago>
    ) {
        val entradas = ventas.map { venta ->
            PieEntry(
                venta.totalVentas.toFloat(),
                venta.metodoPago
            )
        }

        val conjunto = PieDataSet(
            entradas,
            "Ventas por método de pago"
        ).apply {
            colors = listOf(
                android.graphics.Color.rgb(218, 180, 90),
                android.graphics.Color.rgb(70, 130, 180),
                android.graphics.Color.rgb(80, 160, 120)
            )
            valueTextSize = 12f
            valueTextColor = android.graphics.Color.BLACK
        }

        chartMetodoPago.data = PieData(conjunto)

        chartMetodoPago.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            centerText = "Ventas"
            setEntryLabelTextSize(12f)
            setUsePercentValues(true)
            setNoDataText("No hay datos de ventas")
            legend.isEnabled = true
            invalidate()
        }
    }




    private fun configurarGraficoRotacion(
        productos: List<ProductoRotacion>
    ) {
        val productosOrdenados = productos.sortedBy {
            it.unidadesVendidas
        }

        val etiquetas = productosOrdenados.map {
            it.producto
        }

        val entradas = productosOrdenados.mapIndexed { indice, item ->
            BarEntry(
                indice.toFloat(),
                item.unidadesVendidas.toFloat()
            )
        }

        val conjunto = BarDataSet(
            entradas,
            "Unidades vendidas"
        ).apply {
            color = android.graphics.Color.rgb(218, 180, 90)
            valueTextSize = 10f
            setDrawValues(true)
        }

        chartProductosRotacion.data = BarData(conjunto)

        chartProductosRotacion.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(etiquetas)
            granularity = 1f
            setDrawGridLines(false)
        }

        chartProductosRotacion.axisRight.isEnabled = false
        chartProductosRotacion.axisLeft.axisMinimum = 0f
        chartProductosRotacion.description.isEnabled = false
        chartProductosRotacion.legend.isEnabled = true
        chartProductosRotacion.setFitBars(true)
        chartProductosRotacion.setNoDataText(
            "No hay datos de productos"
        )
        chartProductosRotacion.invalidate()
    }

}
