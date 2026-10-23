
package com.example.motorep_mobile_app.ui.reportes

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.ui.common.MenuLateral

class activity_tendencias : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tendencias)

        // Configurar menú lateral y permisos por rol.
        MenuLateral(this).configurar()
    }
}
