package com.example.motorep_mobile_app.ui.configuracion

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.example.motorep_mobile_app.R

class activity_configuracion : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_configuracion)

        // Mostrar el título correcto en la pantalla.
        findViewById<TextView>(R.id.txtDashboard).text = "Configuración"
    }
}