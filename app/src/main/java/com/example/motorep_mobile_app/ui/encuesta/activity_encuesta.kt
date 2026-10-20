// Paquete donde se encuentra la pantalla de encuesta.
package com.example.motorep_mobile_app.ui.encuesta

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.motorep_mobile_app.R
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class EncuestaActivity : AppCompatActivity() {

    // Contenedores de las tres secciones de la pantalla.
    private lateinit var contenedorPreguntas: LinearLayout
    private lateinit var contenedorRevision: LinearLayout
    private lateinit var contenedorConfirmacion: LinearLayout
    // Selector de facturas del cliente.
    private lateinit var spinnerFacturas: Spinner
    // Identificador de la factura que seleccionó el cliente.
    private var facturaIdSeleccionada: Int = -1

    // Identificador del cliente que inició sesión en la encuesta.
    private var clienteId: Int = -1

    // Botones de navegación.
    private lateinit var btnRevisar: Button
    private lateinit var btnEditar: Button
    private lateinit var btnEnviar: Button
    private lateinit var btnFinalizar: Button

    // Grupos de opciones de respuesta única.
    private lateinit var radioExperiencia: RadioGroup
    private lateinit var radioAtencion: RadioGroup
    private lateinit var radioSatisfaccionGeneral: RadioGroup
    private lateinit var radioFacilidadProducto: RadioGroup
    private lateinit var radioVariedadProductos: RadioGroup
    private lateinit var radioMarcaPreferida: RadioGroup

    // Casillas para seleccionar varios productos.
    private lateinit var checkBujias: CheckBox
    private lateinit var checkPastillas: CheckBox
    private lateinit var checkAceite: CheckBox
    private lateinit var checkFiltros: CheckBox
    private lateinit var checkOtrosProductos: CheckBox

    // Campos de texto de la encuesta.
    private lateinit var txtOtraMarca: EditText
    private lateinit var txtOtrosProductos: EditText
    private lateinit var txtComentario: EditText
    private lateinit var txtComentarioMejora: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carga el diseño XML de la encuesta.
        setContentView(R.layout.activity_encuesta)

        clienteId = intent.getIntExtra("cliente_id", -1)

        // Busca e inicializa los controles del XML.
        inicializarVistas()

        // Carga las facturas recibidas desde la pantalla anterior.
        cargarFacturas()

        // Configura los botones y las opciones condicionales.
        configurarEventos()
    }

    // Inicializa los componentes visuales mediante sus identificadores.
    private fun inicializarVistas() {

        contenedorPreguntas = findViewById(R.id.contenedorPreguntas)
        contenedorRevision = findViewById(R.id.contenedorRevision)
        contenedorConfirmacion = findViewById(R.id.contenedorConfirmacion)

        btnRevisar = findViewById(R.id.btnRevisar)
        btnEditar = findViewById(R.id.btnEditar)
        btnEnviar = findViewById(R.id.btnEnviar)
        btnFinalizar = findViewById(R.id.btnFinalizar)

        radioExperiencia = findViewById(R.id.radioExperiencia)
        radioAtencion = findViewById(R.id.radioAtencion)
        radioSatisfaccionGeneral =
            findViewById(R.id.radioSatisfaccionGeneral)
        radioFacilidadProducto =
            findViewById(R.id.radioFacilidadProducto)
        radioVariedadProductos =
            findViewById(R.id.radioVariedadProductos)
        radioMarcaPreferida =
            findViewById(R.id.radioMarcaPreferida)

        checkBujias = findViewById(R.id.checkBujias)
        checkPastillas = findViewById(R.id.checkPastillas)
        checkAceite = findViewById(R.id.checkAceite)
        checkFiltros = findViewById(R.id.checkFiltros)
        checkOtrosProductos = findViewById(R.id.checkOtrosProductos)

        txtOtraMarca = findViewById(R.id.txtOtraMarca)
        txtOtrosProductos = findViewById(R.id.txtOtrosProductos)
        txtComentario = findViewById(R.id.txtComentario)
        txtComentarioMejora = findViewById(R.id.txtComentarioMejora)

        // Busca el Spinner de facturas definido en el XML.
        spinnerFacturas = findViewById(R.id.spinnerFacturas)
    }

    // Carga las facturas que recibió la pantalla desde el login de encuesta.
    private fun cargarFacturas() {

        // Recupera los identificadores de las facturas.
        val facturaIds = intent.getIntegerArrayListExtra("factura_ids")

        // Recupera las etiquetas que se mostrarán en el Spinner.
        val facturaLabels = intent.getStringArrayListExtra("factura_labels")

        // Comprueba que existan facturas y que ambas listas coincidan.
        if (facturaIds.isNullOrEmpty() ||
            facturaLabels.isNullOrEmpty() ||
            facturaIds.size != facturaLabels.size
        ) {
            Toast.makeText(
                this,
                "No se encontraron facturas para este cliente",
                Toast.LENGTH_LONG
            ).show()

            // No permite seleccionar una factura inexistente.
            btnRevisar.isEnabled = false
            return
        }

        // Crea el adaptador que mostrará las facturas en el Spinner.
        val adaptador = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            facturaLabels
        )

        // Define el diseño de las opciones desplegables.
        adaptador.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        // Asigna el adaptador al Spinner.
        spinnerFacturas.adapter = adaptador

        // Guarda el ID de la primera factura seleccionada.
        facturaIdSeleccionada = facturaIds[0]

        // Actualiza el ID cada vez que el cliente elija otra factura.
        spinnerFacturas.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    facturaIdSeleccionada = facturaIds[position]
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>
                ) {
                    facturaIdSeleccionada = -1
                }
            }
    }

    // Configura los eventos de navegación y los campos opcionales.
    private fun configurarEventos() {

        // Abre la revisión con las respuestas actuales.
        btnRevisar.setOnClickListener {
            mostrarRevision()
        }

        // Regresa al formulario sin borrar las respuestas.
        btnEditar.setOnClickListener {
            mostrarEncuesta()
        }

        // Envía la encuesta a la API y confirma solo si se guardó.
        btnEnviar.setOnClickListener {
            enviarEncuesta()
        }

        // Finaliza el recorrido de la encuesta.
        btnFinalizar.setOnClickListener {
            finish()
        }

        // La marca escrita solo se muestra cuando se selecciona "Otra".
        radioMarcaPreferida.setOnCheckedChangeListener { group, checkedId ->
            val seleccion = group.findViewById<RadioButton>(checkedId)
            val esOtra = seleccion?.text?.toString() == "Otra"

            txtOtraMarca.visibility =
                if (esOtra) View.VISIBLE else View.GONE
        }

        // El campo adicional se muestra cuando se selecciona "Otros".
        checkOtrosProductos.setOnCheckedChangeListener { _, marcado ->
            txtOtrosProductos.visibility =
                if (marcado) View.VISIBLE else View.GONE
        }
    }

    // Devuelve el texto seleccionado en un grupo de opciones.
    private fun obtenerRespuestaRadio(group: RadioGroup): String {

        val idSeleccionado = group.checkedRadioButtonId

        if (idSeleccionado == -1) {
            return "Sin respuesta"
        }

        val radioButton =
            group.findViewById<RadioButton>(idSeleccionado)

        return radioButton?.text?.toString() ?: "Sin respuesta"
    }

    // Obtiene la marca seleccionada y contempla la opción "Otra".
    private fun obtenerMarcaPreferida(): String {

        val marca = obtenerRespuestaRadio(radioMarcaPreferida)

        if (marca == "Otra") {
            val otra = txtOtraMarca.text.toString().trim()

            return if (otra.isNotEmpty()) {
                "Otra: $otra"
            } else {
                "Otra"
            }
        }

        return marca
    }

    // Reúne los productos marcados por el cliente.
    private fun obtenerProductosSeleccionados(): String {

        val productos = mutableListOf<String>()

        if (checkBujias.isChecked) {
            productos.add("Bujías")
        }

        if (checkPastillas.isChecked) {
            productos.add("Pastillas de freno")
        }

        if (checkAceite.isChecked) {
            productos.add("Aceite")
        }

        if (checkFiltros.isChecked) {
            productos.add("Filtros")
        }

        if (checkOtrosProductos.isChecked) {
            val otros = txtOtrosProductos.text.toString().trim()

            productos.add(
                if (otros.isNotEmpty()) "Otros: $otros" else "Otros"
            )
        }

        return if (productos.isEmpty()) {
            "Sin selección"
        } else {
            productos.joinToString(", ")
        }
    }

    // Devuelve el texto escrito o un mensaje cuando el campo está vacío.
    private fun obtenerTexto(
        campo: EditText,
        textoVacio: String
    ): String {

        val texto = campo.text.toString().trim()

        return if (texto.isEmpty()) textoVacio else texto
    }

    // Muestra todas las respuestas en la sección de revisión.
    private fun mostrarRevision() {

        // Obtiene las respuestas seleccionadas.
        val experiencia = obtenerRespuestaRadio(radioExperiencia)
        val atencion = obtenerRespuestaRadio(radioAtencion)
        val satisfaccion =
            obtenerRespuestaRadio(radioSatisfaccionGeneral)
        val facilidad = obtenerRespuestaRadio(radioFacilidadProducto)
        val variedad = obtenerRespuestaRadio(radioVariedadProductos)
        val marca = obtenerMarcaPreferida()
        val productos = obtenerProductosSeleccionados()

        val comentario = obtenerTexto(
            txtComentario,
            "Sin comentario"
        )

        val mejora = obtenerTexto(
            txtComentarioMejora,
            "Sin sugerencias"
        )

        // Presenta cada respuesta en su TextView correspondiente.
        findViewById<TextView>(R.id.txtRevisionExperiencia).text =
            experiencia

        findViewById<TextView>(R.id.txtRevisionAtencion).text =
            atencion

        findViewById<TextView>(
            R.id.txtRevisionSatisfaccionGeneral
        ).text = satisfaccion

        findViewById<TextView>(R.id.txtRevisionFacilidad).text =
            facilidad

        findViewById<TextView>(R.id.txtRevisionVariedad).text =
            variedad

        findViewById<TextView>(R.id.txtRevisionMarca).text =
            marca

        findViewById<TextView>(R.id.txtRevisionProductos).text =
            productos

        findViewById<TextView>(R.id.txtRevisionComentario).text =
            comentario

        findViewById<TextView>(R.id.txtRevisionMejora).text =
            mejora

        // Oculta el formulario y muestra la revisión.
        contenedorPreguntas.visibility = View.GONE
        contenedorRevision.visibility = View.VISIBLE
        contenedorConfirmacion.visibility = View.GONE

        // Regresa el desplazamiento al principio de la pantalla.
        findViewById<android.widget.ScrollView>(
            R.id.scrollEncuesta
        ).post {
            findViewById<android.widget.ScrollView>(
                R.id.scrollEncuesta
            ).smoothScrollTo(0, 0)
        }
    }

    // Regresa al formulario conservando todas las selecciones.
    private fun mostrarEncuesta() {

        contenedorPreguntas.visibility = View.VISIBLE
        contenedorRevision.visibility = View.GONE
        contenedorConfirmacion.visibility = View.GONE

        // Desplaza el formulario hasta el principio.
        findViewById<android.widget.ScrollView>(
            R.id.scrollEncuesta
        ).smoothScrollTo(0, 0)
    }

    // muestra la confirmacion
    private fun mostrarConfirmacion() {

        contenedorPreguntas.visibility = View.GONE
        contenedorRevision.visibility = View.GONE
        contenedorConfirmacion.visibility = View.VISIBLE

        // Muestra la confirmación desde el principio.
        findViewById<android.widget.ScrollView>(
            R.id.scrollEncuesta
        ).smoothScrollTo(0, 0)
    }

    //Url
    private val urlEncuestas = "http://127.0.0.1:8000/encuestas/crear/"

    // Lista de productos para guardarla como arreglo en Mongo.
    private fun obtenerProductosLista(): JSONArray {
        val lista = JSONArray()
        if (checkBujias.isChecked) lista.put("Bujías")
        if (checkPastillas.isChecked) lista.put("Pastillas de freno")
        if (checkAceite.isChecked) lista.put("Aceite")
        if (checkFiltros.isChecked) lista.put("Filtros")
        if (checkOtrosProductos.isChecked) {
            val otros = txtOtrosProductos.text.toString().trim()
            lista.put(if (otros.isNotEmpty()) "Otros: $otros" else "Otros")
        }
        return lista
    }

    // Arma el JSON con la estructura de la colección y lo envía a la API.
    private fun enviarEncuesta() {

        if (clienteId == -1 || facturaIdSeleccionada == -1) {
            Toast.makeText(this, "Faltan datos del cliente o la factura", Toast.LENGTH_LONG).show()
            return
        }

        val json = JSONObject().apply {
            put("cliente_id", clienteId)
            put("factura_id", facturaIdSeleccionada)

            put("satisfaccion_servicio", JSONObject().apply {
                put("satisfaccion_general", obtenerRespuestaRadio(radioSatisfaccionGeneral))
                put("atencion_personal", obtenerRespuestaRadio(radioAtencion))
                put("experiencia_compra", obtenerRespuestaRadio(radioExperiencia))
            })

            put("preferencias_uso", JSONObject().apply {
                put("facilidad_encontrar_producto", obtenerRespuestaRadio(radioFacilidadProducto))
                put("variedad_productos", obtenerRespuestaRadio(radioVariedadProductos))
                put("marca_preferida", obtenerMarcaPreferida())
            })

            put("productos_frecuentes", obtenerProductosLista())
            put("comentario_servicio", obtenerTexto(txtComentario, "Sin comentario"))
            put("sugerencia",obtenerTexto(txtComentarioMejora, "Sin sugerencias"))
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(urlEncuestas).post(body).build()

        btnEnviar.isEnabled = false  // evita doble envío

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("ENCUESTA", "Error de conexión", e)
                runOnUiThread {
                    btnEnviar.isEnabled = true
                    Toast.makeText(
                        this@EncuestaActivity,
                        "No se pudo enviar. Revisa la conexión.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val cuerpo = response.body?.string()
                Log.d("ENCUESTA", "Código: ${response.code} $cuerpo")
                runOnUiThread {
                    btnEnviar.isEnabled = true
                    if (response.isSuccessful) {
                        mostrarConfirmacion()
                    } else {
                        Toast.makeText(
                            this@EncuestaActivity,
                            "Error del servidor (${response.code})",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }
}
