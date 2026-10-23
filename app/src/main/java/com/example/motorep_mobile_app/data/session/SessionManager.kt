
package com.example.motorep_mobile_app.data.session

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "Sesion",
        Context.MODE_PRIVATE
    )

    fun guardarToken(token: String) {
        prefs.edit()
            .putString("TOKEN", token)
            .apply()
    }

    fun obtenerToken(): String? {
        return prefs.getString("TOKEN", null)
    }

    fun guardarGrupos(grupos: List<String>) {
        prefs.edit()
            .putStringSet("GRUPOS", grupos.toSet())
            .apply()
    }

    fun obtenerGrupos(): Set<String> {
        return prefs.getStringSet(
            "GRUPOS",
            emptySet()
        )?.toSet() ?: emptySet()
    }

    fun perteneceAlGrupo(nombreGrupo: String): Boolean {
        return obtenerGrupos().any {
            it.equals(nombreGrupo, ignoreCase = true)
        }
    }

    fun cerrarSesion() {
        prefs.edit()
            .remove("TOKEN")
            .remove("GRUPOS")
            .apply()
    }
}
