package com.librompaz.app.nativo

import android.content.Context
import android.net.Uri
import java.io.IOException

object ExportadorCsv {
    fun fila(vararg campos: String): String =
        campos.joinToString(";") { "\"" + it.replace("\"", "\"\"") + "\"" }

    fun escribir(context: Context, uri: Uri, contenido: String): Boolean =
        try {
            context.contentResolver.openOutputStream(uri)?.use { salida ->
                salida.write(("\uFEFF" + contenido).toByteArray(Charsets.UTF_8))
            } != null
        } catch (e: IOException) {
            false
        } catch (e: SecurityException) {
            false
        }
}
