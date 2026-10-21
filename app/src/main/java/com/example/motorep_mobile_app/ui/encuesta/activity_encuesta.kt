
package com.example.motorep_mobile_app.ui.encuesta

// Importaciones para la pantalla y sus controles.
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Importaciones de la API Retrofit existente.
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.conexion.RetrofitClient
import com.example.motorep_mobile_app.conexion.EncuestaRequest
import com.example.motorep_mobile_app.conexion.EncuestaCrearResponse
import com.example.motorep_mobile_app.conexion.SatisfaccionServicioRequest
import com.example.motorep_mobile_app.conexion.PreferenciasUsoRequest

import org.json.JSONArray

class EncuestaActivity : AppCompatActivity() {

    // Contenedores de las secciones del formulario, revisión y confirmación.
    private lateinit var contenedorPreguntas: LinearLayout
    private lateinit var contenedorRevision: LinearLayout
    private lateinit var contenedorConfirmacion: LinearLayout

    // Selector de facturas asociadas al cliente.
    private lateinit var spinnerFacturas: Spinner

    // Guarda el identificador de la factura seleccionada.
    private var facturaIdSeleccionada: Int = -1

    // Identificador del cliente recibido desde la pantalla anterior.
    private var clienteId: Int = -1

    // Botones para navegar por las etapas de la encuesta.
    private lateinit var btnRevisar: Button
    private lateinit var btnEditar: Button
    private lateinit var btnEnviar: Button
    private lateinit var btnFinalizar: Button

    // Grupos de respuestas de selección única.
    private lateinit var radioExperiencia: RadioGroup
    private lateinit var radioAtencion: RadioGroup
    private lateinit var radioSatisfaccionGeneral: RadioGroup
    private lateinit var radioFacilidadProducto: RadioGroup
    private lateinit var radioVariedadProductos: RadioGroup
    private lateinit var radioMarcaPreferida: RadioGroup

    // Casillas para seleccionar los productos frecuentes.
    private lateinit var checkBujias: CheckBox
    private lateinit var checkPastillas: CheckBox
    private lateinit var checkAceite: CheckBox
    private lateinit var checkFiltros: CheckBox
    private lateinit var checkOtrosProductos: CheckBox

    // Campos de texto para respuestas adicionales.
    private lateinit var txtOtraMarca: EditText
    private lateinit var txtOtrosProductos: EditText
    private lateinit var txtComentario: EditText
    private lateinit var txtComentarioMejora: EditText

    // Se ejecuta al abrir la pantalla de la encuesta.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carga el diseño XML de la encuesta.
        setContentView(R.layout.activity_encuesta)

        // Recupera el identificador del cliente.
        clienteId = intent.getIntExtra("cliente_id", -1)

        // Inicializa los controles de la pantalla.
        inicializarVistas()

        // Carga las facturas disponibles para el cliente.
        cargarFacturas()

        // Configura los botones y las opciones condicionales.
        configurarEventos()
    }

    // Relaciona las variables con los controles definidos en el XML.
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

        // Inicializa el selector de facturas.
        spinnerFacturas = findViewById(R.id.spinnerFacturas)
    }

    // Carga las facturas y conserva el ID de la factura seleccionada.
    private fun cargarFacturas() {
        val facturaIds = intent.getIntegerArrayListExtra("factura_ids")
        val facturaLabels = intent.getStringArrayListExtra("factura_labels")

        // Comprueba que existan facturas y que las listas coincidan.
        if (facturaIds.isNullOrEmpty() ||
            facturaLabels.isNullOrEmpty() ||
            facturaIds.size != facturaLabels.size
        ) {
            Toast.makeText(
                this,
                "No se encontraron facturas para este cliente",
                Toast.LENGTH_LONG
            ).show()

            // Impide continuar si no hay una factura disponible.
            btnRevisar.isEnabled = false
            btnEnviar.isEnabled = false
            return
        }

        // Prepara el adaptador que mostrará las facturas.
        val adaptador = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            facturaLabels
        )

        // Define el diseño de las opciones desplegables.
        adaptador.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        // Asigna las facturas al Spinner.
        spinnerFacturas.adapter = adaptador

        // Guarda el identificador de la primera factura.
        facturaIdSeleccionada = facturaIds[0]

        // Actualiza el identificador cuando cambia la selección.
        spinnerFacturas.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    facturaIdSeleccionada = facturaIds[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    facturaIdSeleccionada = -1
                }
            }
    }

    // Configura los botones y los campos que aparecen condicionalmente.
    private fun configurarEventos() {

        // Muestra la revisión de las respuestas.
        btnRevisar.setOnClickListener {
            mostrarRevision()
        }

        // Regresa al formulario sin borrar las respuestas.
        btnEditar.setOnClickListener {
            mostrarEncuesta()
        }

        // Envía las respuestas a la API.
        btnEnviar.setOnClickListener {
            enviarEncuesta()
        }

        // Finaliza el recorrido de la encuesta.
        btnFinalizar.setOnClickListener {
            finish()
        }

        // Muestra el campo de texto si se selecciona "Otra".
        radioMarcaPreferida.setOnCheckedChangeListener { group, checkedId ->
            val seleccion =
                group.findViewById<RadioButton>(checkedId)
            val esOtra = seleccion?.text?.toString() == "Otra"

            txtOtraMarca.visibility =
                if (esOtra) View.VISIBLE else View.GONE
        }

        // Muestra el campo adicional si se selecciona "Otros".
        checkOtrosProductos.setOnCheckedChangeListener { _, marcado ->
            txtOtrosProductos.visibility =
                if (marcado) View.VISIBLE else View.GONE
        }
    }

    // Devuelve el texto de la opción seleccionada.
    private fun obtenerRespuestaRadio(group: RadioGroup): String {
        val idSeleccionado = group.checkedRadioButtonId

        if (idSeleccionado == -1) {
            return "Sin respuesta"
        }

        val radioButton =
            group.findViewById<RadioButton>(idSeleccionado)

        return radioButton?.text?.toString() ?: "Sin respuesta"
    }

    // Devuelve un número entero del 1 al 5 para las respuestas de escala.
    private fun obtenerValorRadio(group: RadioGroup): Int {
        val idSeleccionado = group.checkedRadioButtonId

        // Devuelve 0 si no hay respuesta.
        if (idSeleccionado == -1) {
            return 0
        }

        val radioButton =
            group.findViewById<RadioButton>(idSeleccionado)
        val texto = radioButton?.text?.toString() ?: return 0

        // Si el texto contiene un número, utiliza ese valor.
        val numero = Regex("""\d+""")
            .find(texto)
            ?.value
            ?.toIntOrNull()

        if (numero != null) {
            return if (numero in 1..5) numero else 0
        }

        // Convierte las opciones de texto conocidas a una escala.
        return when (texto.trim().lowercase()) {
            "muy mala",
            "muy insatisfecho",
            "muy difícil",
            "muy dificil",
            "muy baja" -> 1

            "mala",
            "insatisfecho",
            "difícil",
            "dificil",
            "baja" -> 2

            "regular",
            "neutral" -> 3

            "buena",
            "satisfecho",
            "fácil",
            "facil",
            "alta" -> 4

            "muy buena",
            "muy satisfecho",
            "muy fácil",
            "muy facil",
            "muy alta" -> 5

            // Utiliza la posición si el texto no coincide.
            else -> {
                val posicion = group.indexOfChild(radioButton)
                if (posicion in 0..4) posicion + 1 else 0
            }
        }
    }

    // Comprueba que todas las preguntas de escala estén respondidas.
    private fun escalasCompletas(): Boolean {
        return radioSatisfaccionGeneral.checkedRadioButtonId != -1 &&
                radioAtencion.checkedRadioButtonId != -1 &&
                radioExperiencia.checkedRadioButtonId != -1 &&
                radioFacilidadProducto.checkedRadioButtonId != -1 &&
                radioVariedadProductos.checkedRadioButtonId != -1
    }

    // Obtiene la marca preferida y contempla la opción "Otra".
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

    // Reúne los productos marcados en un arreglo JSON.
    private fun obtenerProductosLista(): JSONArray {
        val lista = JSONArray()

        if (checkBujias.isChecked) lista.put("Bujías")
        if (checkPastillas.isChecked) lista.put("Pastillas de freno")
        if (checkAceite.isChecked) lista.put("Aceite")
        if (checkFiltros.isChecked) lista.put("Filtros")

        // Agrega el nombre de otros productos, si corresponde.
        if (checkOtrosProductos.isChecked) {
            val otros = txtOtrosProductos.text.toString().trim()

            lista.put(
                if (otros.isNotEmpty()) "Otros: $otros" else "Otros"
            )
        }

        return lista
    }

    // Devuelve el texto escrito o un mensaje predeterminado.
    private fun obtenerTexto(
        campo: EditText,
        textoVacio: String
    ): String {
        val texto = campo.text.toString().trim()
        return if (texto.isEmpty()) textoVacio else texto
    }

    // Muestra las respuestas para que el cliente las revise.
    private fun mostrarRevision() {
        val experiencia = obtenerRespuestaRadio(radioExperiencia)
        val atencion = obtenerRespuestaRadio(radioAtencion)
        val satisfaccion =
            obtenerRespuestaRadio(radioSatisfaccionGeneral)
        val facilidad = obtenerRespuestaRadio(radioFacilidadProducto)
        val variedad = obtenerRespuestaRadio(radioVariedadProductos)
        val marca = obtenerMarcaPreferida()

        val productos = obtenerProductosLista()

        val comentario = obtenerTexto(
            txtComentario,
            "Sin comentario"
        )
        val mejora = obtenerTexto(
            txtComentarioMejora,
            "Sin sugerencias"
        )

        // Muestra las respuestas en los TextView de revisión.
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

        findViewById<TextView>(R.id.txtRevisionMarca).text = marca

        findViewById<TextView>(R.id.txtRevisionProductos).text =
            productos.toString()

        findViewById<TextView>(R.id.txtRevisionComentario).text =
            comentario

        findViewById<TextView>(R.id.txtRevisionMejora).text =
            mejora

        // Oculta el formulario y muestra la revisión.
        contenedorPreguntas.visibility = View.GONE
        contenedorRevision.visibility = View.VISIBLE
        contenedorConfirmacion.visibility = View.GONE

        // Regresa al principio de la pantalla.
        findViewById<android.widget.ScrollView>(
            R.id.scrollEncuesta
        ).post {
            findViewById<android.widget.ScrollView>(
                R.id.scrollEncuesta
            ).smoothScrollTo(0, 0)
        }
    }

    // Regresa al formulario conservando las selecciones.
    private fun mostrarEncuesta() {
        contenedorPreguntas.visibility = View.VISIBLE
        contenedorRevision.visibility = View.GONE
        contenedorConfirmacion.visibility = View.GONE

        // Desplaza la pantalla hasta el principio.
        findViewById<android.widget.ScrollView>(
            R.id.scrollEncuesta
        ).smoothScrollTo(0, 0)
    }

    // Muestra la confirmación después de guardar correctamente.
    private fun mostrarConfirmacion() {
        contenedorPreguntas.visibility = View.GONE
        contenedorRevision.visibility = View.GONE
        contenedorConfirmacion.visibility = View.VISIBLE

        // Muestra la confirmación desde el principio.
        findViewById<android.widget.ScrollView>(
            R.id.scrollEncuesta
        ).smoothScrollTo(0, 0)
    }

    // Valida, construye y envía la encuesta mediante Retrofit.
    private fun enviarEncuesta() {

        // Comprueba que existan el cliente y la factura.
        if (clienteId == -1 || facturaIdSeleccionada == -1) {
            Toast.makeText(
                this,
                "Faltan datos del cliente o la factura",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Impide enviar la encuesta si falta alguna valoración.
        if (!escalasCompletas()) {
            Toast.makeText(
                this,
                "Responde todas las preguntas de valoración",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Convierte las respuestas de escala a números.
        val satisfaccion = obtenerValorRadio(radioSatisfaccionGeneral)
        val atencion = obtenerValorRadio(radioAtencion)
        val experiencia = obtenerValorRadio(radioExperiencia)
        val facilidad = obtenerValorRadio(radioFacilidadProducto)
        val variedad = obtenerValorRadio(radioVariedadProductos)

        // Comprueba que todos los valores estén entre 1 y 5.
        if (listOf(
                satisfaccion,
                atencion,
                experiencia,
                facilidad,
                variedad
            ).any { it !in 1..5 }
        ) {
            Toast.makeText(
                this,
                "Verifica las respuestas de valoración",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Convierte la lista JSON de productos a una lista de Kotlin.
        val productosJson = obtenerProductosLista()
        val productos = List(productosJson.length()) { indice ->
            productosJson.getString(indice)
        }

        // Construye las respuestas de satisfacción.
        val datosSatisfaccion = SatisfaccionServicioRequest(
            satisfaccion_general = satisfaccion,
            atencion_personal = atencion,
            experiencia_compra = experiencia
        )

        // Construye las preferencias de uso.
        val datosPreferencias = PreferenciasUsoRequest(
            facilidad_encontrar_producto = facilidad,
            variedad_productos = variedad,
            marca_preferida = obtenerMarcaPreferida()
        )

        // Construye el objeto completo que recibirá la API.
        val datosEncuesta = EncuestaRequest(
            cliente_id = clienteId,
            factura_id = facturaIdSeleccionada,
            satisfaccion_servicio = datosSatisfaccion,
            preferencias_uso = datosPreferencias,
            productos_frecuentes = productos,
            comentario_servicio = obtenerTexto(
                txtComentario,
                "Sin comentario"
            ),
            sugerencia = obtenerTexto(
                txtComentarioMejora,
                "Sin sugerencias"
            )
        )

        // Desactiva el botón para evitar envíos duplicados.
        btnEnviar.isEnabled = false

        // Envía la encuesta mediante la API centralizada.
        RetrofitClient.clienteEncuestaApi.crearEncuesta(datosEncuesta)
            .enqueue(object :
                retrofit2.Callback<EncuestaCrearResponse> {

                // Gestiona los errores de conexión.
                override fun onFailure(
                    call: retrofit2.Call<EncuestaCrearResponse>,
                    t: Throwable
                ) {
                    Log.e(
                        "ENCUESTA",
                        "Error al enviar la encuesta",
                        t
                    )

                    runOnUiThread {
                        btnEnviar.isEnabled = true

                        Toast.makeText(
                            this@EncuestaActivity,
                            "No se pudo enviar. Revisa la conexión.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                // Gestiona la respuesta de la API.
                override fun onResponse(
                    call: retrofit2.Call<EncuestaCrearResponse>,
                    response: retrofit2.Response<EncuestaCrearResponse>
                ) {
                    Log.d(
                        "ENCUESTA",
                        "Código: ${response.code()}, respuesta: ${response.body()}"
                    )

                    runOnUiThread {
                        btnEnviar.isEnabled = true

                        if (response.isSuccessful) {
                            // Muestra la confirmación si el servidor responde con éxito.
                            mostrarConfirmacion()
                        } else {
                            // Registra el error devuelto por Django.
                            val error = response.errorBody()?.string()

                            Log.e(
                                "ENCUESTA",
                                "Error del servidor: $error"
                            )

                            Toast.makeText(
                                this@EncuestaActivity,
                                "Error del servidor (${response.code()})",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            })
    }
}
