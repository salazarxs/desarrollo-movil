package com.librompaz.app.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

object Validaciones {
    private val FORMATO_CORREO = Regex("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")
    private val FORMATO_FECHA = Regex("^\\d{4}-\\d{2}-\\d{2}$")

    fun correo(texto: String): String? = when {
        texto.isBlank() -> "Ingresa tu correo."
        !FORMATO_CORREO.matches(texto.trim()) -> "El correo no tiene un formato válido."
        else -> null
    }

    fun obligatorio(texto: String, campo: String): String? =
        if (texto.isBlank()) "Ingresa $campo." else null

    fun largoMinimo(texto: String, minimo: Int, campo: String): String? = when {
        texto.isBlank() -> "Ingresa $campo."
        texto.trim().length < minimo -> "Escribe al menos $minimo caracteres."
        else -> null
    }

    fun seleccion(valor: Any?, campo: String): String? =
        if (valor == null) "Elige $campo." else null

    fun fecha(texto: String): String? = when {
        texto.isBlank() -> "Ingresa la fecha."
        !FORMATO_FECHA.matches(texto) -> "Usa el formato AAAA-MM-DD."
        !fechaExiste(texto) -> "Esa fecha no existe."
        texto > hoy() -> "La fecha no puede ser futura."
        else -> null
    }

    private fun fechaExiste(texto: String): Boolean {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        formato.isLenient = false
        return try {
            formato.parse(texto)
            true
        } catch (e: ParseException) {
            false
        }
    }
}
