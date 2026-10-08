package com.example.motorep_mobile_app

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class EncuestaActivity : AppCompatActivity() {

    // Contenedor donde se mostrarán las preguntas
    private lateinit var contenedorPreguntas: LinearLayout

    // Contenedor utilizado para mostrar la revisión de respuestas
    private lateinit var contenedorRevision: LinearLayout

    // Botones principales
    private lateinit var btnRevisar: Button
    private lateinit var btnEditar: Button
    private lateinit var btnEnviar: Button

    // Controles de las preguntas
    private lateinit var radioExperiencia: RadioGroup
    private lateinit var radioAtencion: RadioGroup

    private lateinit var checkBujias: CheckBox
    private lateinit var checkPastillas: CheckBox
    private lateinit var checkAceite: CheckBox
    private lateinit var checkFiltros: CheckBox

    private lateinit var txtComentario: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cargamos el diseño XML de la encuesta
        setContentView(R.layout.activityencuesta)

        // Inicializamos los componentes de la pantalla
        inicializarVistas()

        // Configuramos los botones
        configurarEventos()
    }

    /**
     * Inicializa todos los elementos visuales de la pantalla.
     */
    private fun inicializarVistas() {

        contenedorPreguntas = findViewById(R.id.contenedorPreguntas)
        contenedorRevision = findViewById(R.id.contenedorRevision)

        btnRevisar = findViewById(R.id.btnRevisar)
        btnEditar = findViewById(R.id.btnEditar)
        btnEnviar = findViewById(R.id.btnEnviar)

        radioExperiencia = findViewById(R.id.radioExperiencia)
        radioAtencion = findViewById(R.id.radioAtencion)

        checkBujias = findViewById(R.id.checkBujias)
        checkPastillas = findViewById(R.id.checkPastillas)
        checkAceite = findViewById(R.id.checkAceite)
        checkFiltros = findViewById(R.id.checkFiltros)

        txtComentario = findViewById(R.id.txtComentario)
    }

    /**
     * Configura la navegación de los botones.
     */
    private fun configurarEventos() {

        // Botón para revisar las respuestas
        btnRevisar.setOnClickListener {
            mostrarRevision()
        }

        // Botón para regresar a editar las respuestas
        btnEditar.setOnClickListener {
            mostrarEncuesta()
        }

        // Botón de envío.
        // Por ahora no envía información a ningún servidor.
        btnEnviar.setOnClickListener {
            mostrarConfirmacion()
        }
    }

    /**
     * Obtiene el texto seleccionado en un RadioGroup.
     */
    private fun obtenerRespuestaRadio(group: RadioGroup): String {

        val idSeleccionado = group.checkedRadioButtonId

        if (idSeleccionado == -1) {
            return "Sin respuesta"
        }

        val radioButton = findViewById<RadioButton>(idSeleccionado)

        return radioButton.text.toString()
    }

    /**
     * Obtiene los productos seleccionados mediante CheckBox.
     */
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

        return if (productos.isEmpty()) {
            "Sin selección"
        } else {
            productos.joinToString(", ")
        }
    }

    /**
     * Muestra la pantalla/sección de revisión.
     */
    private fun mostrarRevision() {

        // Obtenemos las respuestas seleccionadas
        val experiencia = obtenerRespuestaRadio(radioExperiencia)
        val atencion = obtenerRespuestaRadio(radioAtencion)
        val productos = obtenerProductosSeleccionados()

        val comentario = if (txtComentario.text.toString().trim().isEmpty()) {
            "Sin comentario"
        } else {
            txtComentario.text.toString().trim()
        }

        // Mostramos las respuestas en la sección de revisión
        findViewById<TextView>(R.id.txtRevisionExperiencia).text =
            experiencia

        findViewById<TextView>(R.id.txtRevisionAtencion).text =
            atencion

        findViewById<TextView>(R.id.txtRevisionProductos).text =
            productos

        findViewById<TextView>(R.id.txtRevisionComentario).text =
            comentario

        // Ocultamos las preguntas
        contenedorPreguntas.visibility = View.GONE

        // Mostramos la revisión
        contenedorRevision.visibility = View.VISIBLE
    }

    /**
     * Regresa desde la revisión a la encuesta.
     */
    private fun mostrarEncuesta() {

        // Mostramos nuevamente las preguntas
        contenedorPreguntas.visibility = View.VISIBLE

        // Ocultamos la revisión
        contenedorRevision.visibility = View.GONE
    }

    /**
     * Muestra la confirmación final.
     *
     * Esta parte es solamente visual.
     * No se envía información a MongoDB ni a una API.
     */
    private fun mostrarConfirmacion() {

        Toast.makeText(
            this,
            "Encuesta enviada correctamente",
            Toast.LENGTH_LONG
        ).show()

        // Después de mostrar el mensaje podemos cerrar esta pantalla.
        finish()
    }
}