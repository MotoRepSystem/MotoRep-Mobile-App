package com.example.motorep_mobile_app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Abrir la pantalla de inicio de sesión
        val intent = Intent(this, activity_login::class.java)

        startActivity(intent)

        // Cerrar MainActivity para que no quede en la pila
        finish()


    }
}