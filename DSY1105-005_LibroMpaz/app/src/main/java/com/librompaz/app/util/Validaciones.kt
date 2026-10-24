package com.librompaz.app.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

object Validaciones {
    private val FORMATO_CORREO = Regex("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")
    private val FORMATO_FECHA = Regex("^\\d{4}-\\d{2}-\\d{2}$")

    private val FORMATO_NOTA = Regex("^\\d([.,]\\d)?$")

    const val NOTA_MINIMA = 1.0
    const val NOTA_MAXIMA = 7.0

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

    fun nota(texto: String): String? {
        val limpio = texto.trim()
        if (limpio.isEmpty()) return "Ingresa la nota."
        if (!FORMATO_NOTA.matches(limpio)) return "Usa un número con un decimal, ej. 5,5."
        val valor = notaComoNumero(limpio)
        if (valor < NOTA_MINIMA || valor > NOTA_MAXIMA) return "La nota va de 1,0 a 7,0."
        return null
    }

    fun notaComoNumero(texto: String): Double = texto.trim().replace(',', '.').toDouble()

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
