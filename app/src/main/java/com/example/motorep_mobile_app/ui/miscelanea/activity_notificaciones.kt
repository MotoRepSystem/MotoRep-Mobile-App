package com.example.motorep_mobile_app.ui.miscelanea

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.motorep_mobile_app.R

class activity_notificaciones : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permite que la pantalla se dibuje de borde a borde.
        enableEdgeToEdge()

        // Carga el diseño XML de las notificaciones.
        setContentView(R.layout.activity_notificaciones)

        // Ajusta el contenido a las barras del sistema.
        // El ID correcto es mainConstraint, según tu XML.
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.mainConstraint)
        ) { v, insets ->

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }
}