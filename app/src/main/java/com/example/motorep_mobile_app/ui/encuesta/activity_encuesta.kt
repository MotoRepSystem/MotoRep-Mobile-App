
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

class EncuestaActivity : AppCompatActivity() {

    // Contenedores de las tres secciones de la pantalla.
    private lateinit var contenedorPreguntas: LinearLayout
    private lateinit var contenedorRevision: LinearLayout
    private lateinit var contenedorConfirmacion: LinearLayout

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

        // Busca e inicializa los controles del XML.
        inicializarVistas()

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

        // Muestra la confirmación visual, sin conexión con el servidor.
        btnEnviar.setOnClickListener {
            mostrarConfirmacion()
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

    // Presenta la pantalla final de confirmación.
    // Esta acción es solamente visual: no guarda datos en MongoDB.
    private fun mostrarConfirmacion() {

        contenedorPreguntas.visibility = View.GONE
        contenedorRevision.visibility = View.GONE
        contenedorConfirmacion.visibility = View.VISIBLE

        // Muestra la confirmación desde el principio.
        findViewById<android.widget.ScrollView>(
            R.id.scrollEncuesta
        ).smoothScrollTo(0, 0)
    }
}
