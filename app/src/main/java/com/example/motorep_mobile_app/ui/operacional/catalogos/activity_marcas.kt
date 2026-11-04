package com.example.motorep_mobile_app.ui.operacional.catalogos

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.data.dao.Marcas_Dao
import com.example.motorep_mobile_app.data.dao.TipoMarca_Dao
import com.example.motorep_mobile_app.data.database.Motorep_Database
import com.example.motorep_mobile_app.data.entity.Marcas
import com.example.motorep_mobile_app.data.entity.TipoMarcaEntity
import com.example.motorep_mobile_app.data.remote.dto.Marca_dto
import com.example.motorep_mobile_app.data.remote.dto.TipoMarca_dto
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.worker.MarcasSyncScheduler
import com.example.motorep_mobile_app.ui.common.MenuLateral
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class activity_marcas : AppCompatActivity() {

    // Datos que se muestran en pantalla.
    private var listaMarcas: List<Marca_dto> = emptyList()
    private var listaTipos: List<TipoMarca_dto> = emptyList()

    // Base de datos local y DAO.
    private lateinit var database: Motorep_Database
    private lateinit var marcasDao: Marcas_Dao
    private lateinit var tipoMarcaDao: TipoMarca_Dao

    // Adaptador y filtro.
    private lateinit var adapter: MarcaAdapter
    private lateinit var spinnerTipo: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_marcas)

        MenuLateral(this).configurar()

        // Inicializar Room.
        database = Motorep_Database.getDatabase(this)
        marcasDao = database.marcasDao()
        tipoMarcaDao = database.tipoMarcaDao()

        val recycler = findViewById<RecyclerView>(R.id.recyclerMarcas)
        recycler.layoutManager = LinearLayoutManager(this)

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

        spinnerTipo = findViewById(R.id.spinnerTipoMarca)

        findViewById<FloatingActionButton>(
            R.id.FloatingActionButtonAgregar
        ).setOnClickListener {
            mostrarFormulario()
        }

        // Observar las marcas guardadas en Room.
        marcasDao.obtenerMarcas().observe(this) { marcasLocales ->
            listaMarcas = marcasLocales.map { marca ->
                Marca_dto(
                    id = marca.serverId,
                    nombre = marca.nombre,
                    tipoId = marca.TipoId,
                    tipoNombre = listaTipos.firstOrNull {
                        it.id == marca.TipoId
                    }?.tipoMarca,
                    localId = marca.id
                )
            }

            aplicarFiltro()
        }

        // Observar los tipos guardados en Room.
        tipoMarcaDao.obtenerTipos().observe(this) { tiposLocales ->
            listaTipos = tiposLocales.map {
                TipoMarca_dto(
                    id = it.id,
                    tipoMarca = it.tipoMarca
                )
            }

            configurarFiltro()
            aplicarFiltro()
        }

        // Intentar actualizar los datos desde la API.
        cargarDatos()
    }

    // Consulta los tipos y las marcas de Django.
    private fun cargarDatos() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.create(this@activity_marcas)

                // Obtener y guardar los tipos en Room.
                val tiposApi = api.obtenerTiposMarca()

                tipoMarcaDao.reemplazarTipos(
                    tiposApi.map {
                        TipoMarcaEntity(
                            id = it.id,
                            tipoMarca = it.tipoMarca
                        )
                    }
                )

                listaTipos = tiposApi
                configurarFiltro()

                // Obtener las marcas del servidor.
                val marcasApi = api.obtenerMarcas()

                // Guardarlas localmente sin reemplazar cambios pendientes.
                guardarMarcasLocales(marcasApi)

            } catch (e: Exception) {
                Toast.makeText(
                    this@activity_marcas,
                    "Se mostrarán los datos guardados localmente.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Guarda las marcas recibidas desde Django sin duplicarlas.
    private suspend fun guardarMarcasLocales(
        marcasApi: List<Marca_dto>
    ) {
        for (marca in marcasApi) {

            val serverId = marca.id ?: continue

            val tipoId = marca.tipoId
                ?: listaTipos.firstOrNull {
                    it.tipoMarca == marca.tipoNombre
                }?.id
                ?: continue

            val existente = marcasDao.buscarPorServerId(serverId)

            if (existente == null) {

                marcasDao.insertar(
                    Marcas(
                        serverId = serverId,
                        nombre = marca.nombre,
                        TipoId = tipoId,
                        sincronizado = true,
                        pendienteEliminar = false
                    )
                )

            } else if (
                existente.sincronizado &&
                !existente.pendienteEliminar
            ) {

                marcasDao.actualizar(
                    existente.copy(
                        nombre = marca.nombre,
                        TipoId = tipoId,
                        sincronizado = true
                    )
                )
            }
        }
    }

    // Configura el filtro por tipo de marca.
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
                }
            }
    }

    // Filtra las marcas utilizando el nombre de su tipo.
    private fun aplicarFiltro() {
        if (!::spinnerTipo.isInitialized) return

        val posicion = spinnerTipo.selectedItemPosition

        val marcasFiltradas = if (posicion <= 0) {
            listaMarcas
        } else {
            val tipoSeleccionado = listaTipos.getOrNull(posicion - 1)

            listaMarcas.filter {
                it.tipoId == tipoSeleccionado?.id
            }
        }

        adapter.actualizarMarcas(marcasFiltradas)
    }

    // Formulario para registrar o editar una marca.
    private fun mostrarFormulario(marca: Marca_dto? = null) {

        if (listaTipos.isEmpty()) {
            Toast.makeText(
                this,
                "No hay tipos de marca disponibles. Conéctate a internet para cargarlos.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
        }

        val campoNombre = EditText(this).apply {
            hint = "Nombre de la marca"
            setSingleLine(true)
            setText(marca?.nombre ?: "")
        }

        val selectorTipo = Spinner(this)

        val adaptadorTipos = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listaTipos.map { it.tipoMarca }
        )

        adaptadorTipos.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        selectorTipo.adapter = adaptadorTipos

        // Seleccionar el tipo actual al editar.
        val indiceTipo = listaTipos.indexOfFirst {
            it.id == marca?.tipoId
        }

        if (indiceTipo >= 0) {
            selectorTipo.setSelection(indiceTipo)
        }

        contenedor.addView(campoNombre)
        contenedor.addView(selectorTipo)

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

                    if (nombre.isBlank()) {
                        campoNombre.error = "Ingresa el nombre de la marca"
                        return@setOnClickListener
                    }

                    if (tipoSeleccionado == null) {
                        Toast.makeText(
                            this,
                            "Selecciona un tipo de marca.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    guardarMarcaLocal(
                        nombre = nombre,
                        tipoId = tipoSeleccionado.id,
                        marca = marca,
                        dialogo = dialogo
                    )
                }
        }

        dialogo.show()
    }

    // Guarda primero en Room, independientemente de la conexión.
    private fun guardarMarcaLocal(
        nombre: String,
        tipoId: Int,
        marca: Marca_dto?,
        dialogo: AlertDialog
    ) {
        lifecycleScope.launch {
            try {

                if (marca == null) {

                    // Registrar una marca nueva como pendiente.
                    marcasDao.insertar(
                        Marcas(
                            nombre = nombre,
                            TipoId = tipoId,
                            sincronizado = false
                        )
                    )

                } else {

                    // Buscar la marca mediante su ID local.
                    val localId = marca.localId

                    if (localId == null) {
                        Toast.makeText(
                            this@activity_marcas,
                            "No se encontró el registro local de la marca.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }

                    val existente = marcasDao.buscarPorId(localId)

                    if (existente == null) {
                        Toast.makeText(
                            this@activity_marcas,
                            "No se encontró la marca.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }

                    marcasDao.actualizar(
                        existente.copy(
                            nombre = nombre,
                            TipoId = tipoId,
                            sincronizado = false
                        )
                    )
                }

                dialogo.dismiss()

                Toast.makeText(
                    this@activity_marcas,
                    if (marca == null) {
                        "Marca guardada localmente."
                    } else {
                        "Marca actualizada localmente."
                    },
                    Toast.LENGTH_SHORT
                ).show()

                // Programar sincronización cuando haya conexión.
                MarcasSyncScheduler.programar(this@activity_marcas)

            } catch (e: Exception) {
                Toast.makeText(
                    this@activity_marcas,
                    "No se pudo guardar la marca localmente.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Solicita confirmación antes de eliminar.
    private fun confirmarEliminacion(marca: Marca_dto) {

        AlertDialog.Builder(this)
            .setTitle("Eliminar marca")
            .setMessage(
                "¿Deseas eliminar la marca \"${marca.nombre}\"?"
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarMarcaLocal(marca)
            }
            .show()
    }

    // Registra la eliminación local y programa la sincronización.
    private fun eliminarMarcaLocal(marca: Marca_dto) {
        lifecycleScope.launch {
            try {

                val localId = marca.localId

                if (localId == null) {
                    Toast.makeText(
                        this@activity_marcas,
                        "No se encontró el registro local.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val existente = marcasDao.buscarPorId(localId)

                if (existente == null) {
                    Toast.makeText(
                        this@activity_marcas,
                        "La marca ya no existe localmente.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                if (existente.serverId == null) {

                    // Si nunca llegó al servidor, basta con borrarla de Room.
                    marcasDao.eliminarLocal(existente.id)

                } else {

                    // Si existe en la API, dejar pendiente su eliminación.
                    marcasDao.actualizar(
                        existente.copy(
                            sincronizado = false,
                            pendienteEliminar = true
                        )
                    )
                }

                Toast.makeText(
                    this@activity_marcas,
                    "Marca eliminada localmente.",
                    Toast.LENGTH_SHORT
                ).show()

                MarcasSyncScheduler.programar(this@activity_marcas)

            } catch (e: Exception) {
                Toast.makeText(
                    this@activity_marcas,
                    "No se pudo eliminar la marca localmente.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
