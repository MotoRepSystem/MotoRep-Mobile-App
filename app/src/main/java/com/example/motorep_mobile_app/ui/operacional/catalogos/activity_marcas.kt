package com.example.motorep_mobile_app.ui.operacional.catalogos

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.data.remote.dto.Marca_dto
import com.example.motorep_mobile_app.data.remote.dto.TipoMarca_dto
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.ui.common.MenuLateral
import kotlinx.coroutines.launch

class activity_marcas : AppCompatActivity() {

    // Listas que almacenan los datos recibidos desde Django.
    private var listaMarcas: List<Marca_dto> = emptyList()
    private var listaTipos: List<TipoMarca_dto> = emptyList()

    // Adaptador de la lista y selector del filtro.
    private lateinit var adapter: MarcaAdapter
    private lateinit var spinnerTipo: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_marcas)

        // Configura el menú lateral.
        MenuLateral(this).configurar()

        // Configura la lista de marcas.
        val recycler = findViewById<RecyclerView>(R.id.recyclerMarcas)
        recycler.layoutManager = LinearLayoutManager(this)

        // Conecta las acciones de editar y eliminar con sus funciones.
        adapter = MarcaAdapter(
            emptyList(),
            onEditar = { marca ->
                mostrarFormulario(marca)
            },
            onEliminar = { marca ->
                confirmarEliminacion(marca)
            }
        )
        recycler.adapter = adapter

        // Obtiene el selector del filtro por tipo.
        spinnerTipo = findViewById(R.id.spinnerTipoMarca)

        // Abre el formulario para registrar una marca.
        findViewById<FloatingActionButton>(
            R.id.FloatingActionButtonAgregar
        ).setOnClickListener {
            mostrarFormulario()
        }

        // Consulta los datos de la API.
        cargarDatos()
    }

    // Consulta los tipos y las marcas de Django.
    private fun cargarDatos() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.create(this@activity_marcas)

                listaTipos = api.obtenerTiposMarca()
                listaMarcas = api.obtenerMarcas()

                configurarFiltro()
                aplicarFiltro()

            } catch (e: Exception) {
                Toast.makeText(
                    this@activity_marcas,
                    "No se pudieron cargar las marcas. Verifica tu conexión o sesión.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Configura las opciones disponibles para filtrar las marcas.
    private fun configurarFiltro() {
        val opciones = listOf("Todos los tipos") +
                listaTipos.map { it.tipoMarca }

        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opciones
        )

        spinnerAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerTipo.adapter = spinnerAdapter

        spinnerTipo.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    aplicarFiltro()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // No se requiere ninguna acción.
                }
            }
    }

    // Filtra usando el nombre del tipo que devuelve Django.
    private fun aplicarFiltro() {
        val posicion = spinnerTipo.selectedItemPosition

        val marcasFiltradas = if (posicion <= 0) {
            // Muestra todas las marcas.
            listaMarcas
        } else {
            // Obtiene el tipo seleccionado.
            val tipoSeleccionado = listaTipos.getOrNull(posicion - 1)

            // Django devuelve tipo_nombre, no TipoId, en la consulta GET.
            listaMarcas.filter {
                it.tipoNombre == tipoSeleccionado?.tipoMarca
            }
        }

        // Actualiza la lista que se muestra en pantalla.
        adapter.actualizarMarcas(marcasFiltradas)
    }

    // Utiliza el mismo formulario para registrar y editar marcas.
    private fun mostrarFormulario(marca: Marca_dto? = null) {

        if (listaTipos.isEmpty()) {
            Toast.makeText(
                this,
                "No hay tipos de marca disponibles.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Contenedor del formulario.
        val contenedor = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
        }

        // Campo para introducir el nombre de la marca.
        val campoNombre = EditText(this).apply {
            hint = "Nombre de la marca"
            setSingleLine(true)
            setText(marca?.nombre ?: "")
        }

        // Selector de tipos disponibles.
        val selectorTipo = Spinner(this)
        val opcionesTipo = listaTipos.map { it.tipoMarca }

        val adaptadorTipos = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opcionesTipo
        )

        adaptadorTipos.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        selectorTipo.adapter = adaptadorTipos

        // Selecciona el tipo actual por nombre al editar.
        val indiceTipo = listaTipos.indexOfFirst {
            it.tipoMarca == marca?.tipoNombre
        }

        if (indiceTipo >= 0) {
            selectorTipo.setSelection(indiceTipo)
        }

        // Agrega los campos al formulario.
        contenedor.addView(campoNombre)
        contenedor.addView(selectorTipo)

        // Crea el diálogo de registro o edición.
        val dialogo = AlertDialog.Builder(this)
            .setTitle(
                if (marca == null) "Registrar marca"
                else "Editar marca"
            )
            .setView(contenedor)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar", null)
            .create()

        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val nombre = campoNombre.text.toString().trim()

                    val tipoSeleccionado = listaTipos.getOrNull(
                        selectorTipo.selectedItemPosition
                    )

                    // Valida que el nombre no esté vacío.
                    if (nombre.isBlank()) {
                        campoNombre.error =
                            "Ingresa el nombre de la marca"
                        return@setOnClickListener
                    }

                    // Valida que exista un tipo seleccionado.
                    if (tipoSeleccionado == null) {
                        Toast.makeText(
                            this@activity_marcas,
                            "Selecciona un tipo de marca.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    // Prepara los datos que espera Django.
                    val marcaGuardar = Marca_dto(
                        id = marca?.id,
                        nombre = nombre,
                        tipoId = tipoSeleccionado.id
                    )

                    guardarMarca(
                        marca = marcaGuardar,
                        editando = marca != null,
                        dialogo = dialogo
                    )
                }
        }

        dialogo.show()
    }

    // Registra una marca nueva o actualiza una existente.
    private fun guardarMarca(
        marca: Marca_dto,
        editando: Boolean,
        dialogo: AlertDialog
    ) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.create(this@activity_marcas)

                if (editando) {
                    val id = marca.id

                    if (id == null) {
                        Toast.makeText(
                            this@activity_marcas,
                            "No se encontró el ID de la marca.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }

                    api.actualizarMarca(id, marca)
                } else {
                    api.guardarMarca(marca)
                }

                // Cierra el formulario cuando la operación termina.
                dialogo.dismiss()

                Toast.makeText(
                    this@activity_marcas,
                    if (editando) {
                        "Marca actualizada."
                    } else {
                        "Marca registrada."
                    },
                    Toast.LENGTH_SHORT
                ).show()

                // Recarga los datos para mostrar los cambios.
                cargarDatos()

            } catch (e: Exception) {
                Toast.makeText(
                    this@activity_marcas,
                    "No se pudo guardar la marca. Verifica los datos o la conexión.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Solicita confirmación antes de eliminar una marca.
    private fun confirmarEliminacion(marca: Marca_dto) {
        val id = marca.id

        if (id == null) {
            Toast.makeText(
                this,
                "No se encontró el ID de la marca.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Eliminar marca")
            .setMessage(
                "¿Deseas eliminar la marca \"${marca.nombre}\"?"
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarMarca(id)
            }
            .show()
    }

    // Solicita a Django la eliminación lógica de la marca.
    private fun eliminarMarca(id: Int) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.create(this@activity_marcas)

                api.eliminarMarca(id)

                Toast.makeText(
                    this@activity_marcas,
                    "Marca eliminada.",
                    Toast.LENGTH_SHORT
                ).show()

                // Recarga el listado, que solo incluye marcas activas.
                cargarDatos()

            } catch (e: Exception) {
                Toast.makeText(
                    this@activity_marcas,
                    "No se pudo eliminar la marca.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
