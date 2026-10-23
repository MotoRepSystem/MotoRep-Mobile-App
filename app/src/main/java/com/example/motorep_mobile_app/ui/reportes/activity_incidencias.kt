package com.example.motorep_mobile_app.ui.reportes

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.motorep_mobile_app.ui.common.MenuLateral
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.motorep_mobile_app.data.remote.api.ApiService
import com.example.motorep_mobile_app.data.remote.retrofit.RetrofitClient
import com.example.motorep_mobile_app.data.repository.IncidenciaRepository
import com.example.motorep_mobile_app.databinding.ActivityIncidenciasBinding
import com.example.motorep_mobile_app.ui.incidencias.IncidenciaAdapter
import com.example.motorep_mobile_app.viewmodel.IncidenciaViewModel
import com.example.motorep_mobile_app.viewmodel.IncidenciaViewModelFactory
import com.example.motorep_mobile_app.viewmodel.UiState


class activity_incidencias() : AppCompatActivity() {

    private lateinit var binding: ActivityIncidenciasBinding
    private val adapter = IncidenciaAdapter()

    private lateinit var viewModel: IncidenciaViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIncidenciasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MenuLateral(this).configurar()

        val api = RetrofitClient.create(applicationContext)
        val repository = IncidenciaRepository(api)
        val factory = IncidenciaViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[IncidenciaViewModel::class.java]


        binding.reciclerviewIncidencias.layoutManager = LinearLayoutManager(this)
        binding.reciclerviewIncidencias.adapter = adapter

        binding.imgMenu.setOnClickListener {
            binding.DrawerLayout.openDrawer(GravityCompat.START)
        }

        binding.btnReintentar.setOnClickListener { viewModel.cargar() }

        viewModel.state.observe(this) { estado ->
            binding.progressIncidencias.isVisible = false
            binding.reciclerviewIncidencias.isVisible = false
            binding.txtEstado.isVisible = false
            binding.btnReintentar.isVisible = false

            when (estado) {
                is UiState.Loading -> {
                    Log.d("INCIDENCIAS", "cargando")
                    binding.progressIncidencias.isVisible = true
                }

                is UiState.Success -> {
                    Log.d("INCIDENCIAS", "recibidas=${estado.data.size}")
                    if (estado.data.isEmpty()) {
                        binding.txtEstado.text = "No hay incidencias registradas."
                        binding.txtEstado.isVisible = true
                    } else {
                        adapter.submitList(estado.data)
                        binding.reciclerviewIncidencias.isVisible = true
                    }
                }

                is UiState.Error -> {
                    Log.e("INCIDENCIAS", estado.message)
                    binding.txtEstado.text = estado.message
                    binding.txtEstado.isVisible = true
                    binding.btnReintentar.isVisible = true
                }
            }
        }
    }
}