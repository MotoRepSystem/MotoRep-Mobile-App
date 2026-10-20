package com.example.motorep_mobile_app.ui.miscelanea

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.ui.common.MenuLateral
class activity_notificaciones : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permite que la pantalla se dibuje de borde a borde.
        enableEdgeToEdge()

        // Carga el diseño XML de las notificaciones.
        setContentView(R.layout.activity_notificaciones)

        MenuLateral(this).configurar()
    }
}