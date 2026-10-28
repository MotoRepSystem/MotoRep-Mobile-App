package com.example.motorep_mobile_app.ui.operacional.catalogos

import android.os.Bundle
import android.util.Log
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.data.dao.Marcas_Dao
import com.example.motorep_mobile_app.data.dao.TipoMarca_Dao
import com.example.motorep_mobile_app.data.database.Motorep_Database
import com.example.motorep_mobile_app.data.entity.Marcas
import com.example.motorep_mobile_app.data.entity.TipoMarcaEntity
import com.example.motorep_mobile_app.data.remote.dto.Marca_dto
import com.example.motorep_mobile_app.data.remote.dto.TipoMarca_dto
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.ui.common.MenuLateral
import kotlinx.coroutines.launch

class activity_marcas : AppCompatActivity() {

    private var listaMarcas: List<Marca_dto> = emptyList()
    private var listaTipos: List<TipoMarca_dto> = emptyList()

    private lateinit var adapter: MarcaAdapter
    private lateinit var spinnerTipo: Spinner
    private lateinit var marcasDao: Marcas_Dao
    private lateinit var tipoMarcaDao: TipoMarca_Dao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_marcas)

        val database = Motorep_Database.getDatabase(applicationContext)
        marcasDao = database.marcasDao()
        tipoMarcaDao = database.tipoMarcaDao()

        MenuLateral(this).configurar()

        val recycler = findViewById<RecyclerView>(R.id.recyclerMarcas)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = MarcaAdapter(
            emptyList(),
            onEditar = { marca -> mostrarFormulario(marca) },
            onEliminar = { marca -> confirmarEliminacion(marca) }
        )
        recycler.adapter = adapter

        spinnerTipo = findViewById(R.id.spinnerTipoMarca)

        findViewById<FloatingActionButton>(
            R.id.FloatingActionButtonAgregar
        ).setOnClickListener {
            mostrarFormulario()
        }

        observarMarcasLocales()
        cargarDatos()
    }

    // Muestra los cambios guardados en Room.
    private fun observarMarcasLocales() {
        marcasDao.obtenerMarcas().observe(this) { marcasLocales ->
            listaMarcas = marcasLocales.map { local ->
                Marca_dto(
                    id = local.serverId,
                    nombre = local.nombre,
                    tipoId = local.TipoId,
                    tipoNombre = listaTipos.firstOrNull {
                        it.id == local.TipoId
                    }?.tipoMarca,
                    localId = local.id
                )
            }
            aplicarFiltro()
        }
    }

    // Carga los datos locales y después intenta sincronizarlos con la API.
    private fun cargarDatos() {
        lifecycleScope.launch {
            val api = RetrofitClient.create(this@activity_marcas)

            // Carga primero los tipos guardados en Room.
            try {
                val tiposLocales = tipoMarcaDao.obtenerTipos()
                listaTipos = tiposLocales.map { local ->
                    TipoMarca_dto(
                        id = local.id,
                        tipoMarca = local.tipoMarca
                    )
                }
                configurarFiltro()
            } catch (e: Exception) {
                Log.e("SYNC_MARCAS", "Error al consultar los tipos locales", e)
            }

            // Actualiza los tipos desde el servidor.
            try {
                val tiposServidor = api.obtenerTiposMarca()

                if (tiposServidor.isNotEmpty()) {
                    val tiposLocales = tiposServidor.map { tipo ->
                        TipoMarcaEntity(
                            id = tipo.id,
                            tipoMarca = tipo.tipoMarca
                        )
                    }
                    tipoMarcaDao.guardarTipos(tiposLocales)
                    listaTipos = tiposServidor
                    configurarFiltro()
                }
            } catch (e: Exception) {
                Log.e("SYNC_MARCAS", "No se pudieron actualizar los tipos", e)
            }

            // Descarga las marcas y elimina las que ya no existen en el servidor.
            try {
                val marcasServidor = api.obtenerMarcas()

                for (remota in marcasServidor) {
                    val idServidor = remota.id ?: continue

                    val tipoId = remota.tipoId
                        ?: listaTipos.firstOrNull {
                            it.tipoMarca == remota.tipoNombre
                        }?.id
                        ?: continue

                    val local = marcasDao.buscarPorServerId(idServidor)

                    if (local == null) {
                        marcasDao.insertar(
                            Marcas(
                                serverId = idServidor,
                                nombre = remota.nombre,
                                TipoId = tipoId,
                                sincronizado = true
                            )
                        )
                    } else if (local.sincronizado && !local.pendienteEliminar) {
                        marcasDao.actualizar(
                            local.copy(
                                nombre = remota.nombre,
                                TipoId = tipoId,
                                sincronizado = true
                            )
                        )
                    }
                }

                // Limpia de Room las marcas sincronizadas que ya no están en la API.
                val idsServidor = marcasServidor.mapNotNull { it.id }.toSet()
                val marcasLocales = marcasDao.obtenerMarcasSincronizadas()

                for (local in marcasLocales) {
                    if (local.serverId !in idsServidor) {
                        marcasDao.eliminarPorId(local.id)
                    }
                }

            } catch (e: Exception) {
                Log.e("SYNC_MARCAS", "No se pudieron descargar las marcas", e)
                Toast.makeText(
                    this@activity_marcas,
                    "No se pudo actualizar desde internet. Se conservarán los datos locales.",
                    Toast.LENGTH_LONG
                ).show()
            }

            // Intenta sincronizar los cambios pendientes.
            sincronizarPendientes()
        }
    }

    // Envía al servidor las altas, modificaciones y eliminaciones pendientes.
    private suspend fun sincronizarPendientes() {
        val api = RetrofitClient.create(this@activity_marcas)

        for (marca in marcasDao.obtenerPendientes()) {
            try {
                val respuesta = if (marca.serverId == null) {
                    api.guardarMarca(
                        Marca_dto(
                            nombre = marca.nombre,
                            tipoId = marca.TipoId
                        )
                    )
                } else {
                    api.actualizarMarca(
                        marca.serverId,
                        Marca_dto(
                            nombre = marca.nombre,
                            tipoId = marca.TipoId
                        )
                    )
                }

                val serverId = respuesta.id
                    ?: throw IllegalStateException("La API no devolvió el ID de la marca.")

                marcasDao.marcarComoSincronizada(marca.id, serverId)

                Log.d("SYNC_MARCAS", "Marca sincronizada: ${marca.nombre}")
            } catch (e: Exception) {
                Log.e("SYNC_MARCAS", "No se pudo sincronizar ${marca.nombre}", e)
            }
        }

        for (marca in marcasDao.obtenerPendientesEliminar()) {
            try {
                val serverId = marca.serverId

                if (serverId == null) {
                    marcasDao.eliminarLocal(marca.id)
                } else {
                    api.eliminarMarca(serverId)
                    marcasDao.eliminarPorId(marca.id)
                }

                Log.d("SYNC_MARCAS", "Eliminación sincronizada: ${marca.nombre}")
            } catch (e: Exception) {
                Log.e("SYNC_MARCAS", "No se pudo eliminar ${marca.nombre}", e)
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

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    // Aplica el filtro seleccionado.
    private fun aplicarFiltro() {
        if (!::spinnerTipo.isInitialized || !::adapter.isInitialized) return

        val posicion = spinnerTipo.selectedItemPosition
        val marcasFiltradas = if (posicion <= 0) {
            listaMarcas
        } else {
            val tipoSeleccionado = listaTipos.getOrNull(posicion - 1)
            listaMarcas.filter { marca ->
                marca.tipoId == tipoSeleccionado?.id ||
                        marca.tipoNombre == tipoSeleccionado?.tipoMarca
            }
        }

        adapter.actualizarMarcas(marcasFiltradas)
    }

    // Abre el formulario para crear o editar una marca.
    private fun mostrarFormulario(marca: Marca_dto? = null) {
        if (listaTipos.isEmpty()) {
            Toast.makeText(
                this,
                "No hay tipos de marca guardados. Conéctate a internet para cargarlos.",
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

        val indiceTipo = listaTipos.indexOfFirst { tipo ->
            tipo.id == marca?.tipoId || tipo.tipoMarca == marca?.tipoNombre
        }
        if (indiceTipo >= 0) selectorTipo.setSelection(indiceTipo)

        contenedor.addView(campoNombre)
        contenedor.addView(selectorTipo)

        val dialogo = AlertDialog.Builder(this)
            .setTitle(if (marca == null) "Registrar marca" else "Editar marca")
            .setView(contenedor)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar", null)
            .create()

        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nombre = campoNombre.text.toString().trim()
                val tipoSeleccionado =
                    listaTipos.getOrNull(selectorTipo.selectedItemPosition)

                if (nombre.isBlank()) {
                    campoNombre.error = "Ingresa el nombre de la marca"
                    return@setOnClickListener
                }

                if (tipoSeleccionado == null) {
                    Toast.makeText(
                        this@activity_marcas,
                        "Selecciona un tipo de marca.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                guardarMarca(
                    marca = Marca_dto(
                        id = marca?.id,
                        nombre = nombre,
                        tipoId = tipoSeleccionado.id,
                        tipoNombre = tipoSeleccionado.tipoMarca,
                        localId = marca?.localId
                    ),
                    editando = marca != null,
                    dialogo = dialogo
                )
            }
        }

        dialogo.show()
    }

    // Guarda primero en Room y luego intenta sincronizar.
    private fun guardarMarca(
        marca: Marca_dto,
        editando: Boolean,
        dialogo: AlertDialog
    ) {
        lifecycleScope.launch {
            try {
                if (editando) {
                    val local = marca.localId?.let {
                        marcasDao.buscarPorId(it)
                    } ?: marca.id?.let {
                        marcasDao.buscarPorServerId(it)
                    }

                    if (local == null) {
                        Toast.makeText(
                            this@activity_marcas,
                            "No se encontró la marca local.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }

                    marcasDao.actualizar(
                        local.copy(
                            nombre = marca.nombre,
                            TipoId = marca.tipoId ?: local.TipoId,
                            sincronizado = false,
                            pendienteEliminar = false
                        )
                    )
                } else {
                    val tipoId = marca.tipoId ?: return@launch

                    marcasDao.insertar(
                        Marcas(
                            nombre = marca.nombre,
                            TipoId = tipoId,
                            sincronizado = false
                        )
                    )
                }

                dialogo.dismiss()
                sincronizarPendientes()

                Toast.makeText(
                    this@activity_marcas,
                    "Marca guardada localmente. Se intentó sincronizar con Azure.",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Log.e("SYNC_MARCAS", "Error al guardar la marca", e)
                Toast.makeText(
                    this@activity_marcas,
                    "No se pudo guardar la marca localmente.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Pide confirmación antes de eliminar.
    private fun confirmarEliminacion(marca: Marca_dto) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar marca")
            .setMessage("¿Deseas eliminar la marca \"${marca.nombre}\"?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarMarca(marca)
            }
            .show()
    }

    // Registra la eliminación local y trata de sincronizarla.
    private fun eliminarMarca(marca: Marca_dto) {
        lifecycleScope.launch {
            try {
                val local = marca.localId?.let {
                    marcasDao.buscarPorId(it)
                } ?: marca.id?.let {
                    marcasDao.buscarPorServerId(it)
                }

                if (local == null) {
                    Toast.makeText(
                        this@activity_marcas,
                        "No se encontró la marca local.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                if (local.serverId == null) {
                    marcasDao.eliminarLocal(local.id)
                } else {
                    marcasDao.actualizar(
                        local.copy(
                            pendienteEliminar = true,
                            sincronizado = false
                        )
                    )
                }

                sincronizarPendientes()

                Toast.makeText(
                    this@activity_marcas,
                    "Se guardó la eliminación local y se intentó sincronizar con Azure.",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Log.e("SYNC_MARCAS", "Error al eliminar la marca", e)
                Toast.makeText(
                    this@activity_marcas,
                    "No se pudo eliminar la marca localmente.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
