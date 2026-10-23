
package com.example.motorep_mobile_app.ui.reportes

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.motorep_mobile_app.R
import com.example.motorep_mobile_app.ui.common.MenuLateral

class activity_indicadores : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_indicadores)

        // Configurar el menú lateral y los permisos según el rol.
        MenuLateral(this).configurar()
    }
}
