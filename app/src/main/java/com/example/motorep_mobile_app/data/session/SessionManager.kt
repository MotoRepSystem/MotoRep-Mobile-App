package com.example.motorep_mobile_app.data.session
import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("Sesion", Context.MODE_PRIVATE)

    fun guardarToken(token: String) {
        prefs.edit().putString("TOKEN", token).apply()
    }

    fun obtenerToken(): String? {
        return prefs.getString("TOKEN", null)
    }

    fun cerrarSesion() {
        prefs.edit().remove("TOKEN").apply()
    }
}