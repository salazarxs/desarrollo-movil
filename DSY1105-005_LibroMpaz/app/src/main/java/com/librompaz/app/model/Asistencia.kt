package com.librompaz.app.model

enum class EstadoAsistencia(val etiqueta: String, val abreviatura: String) {
    PRESENTE("Presente", "P"),
    AUSENTE("Ausente", "A"),
    ATRASADO("Atrasado", "T"),
    JUSTIFICADO("Justificado", "J")
}

data class RegistroAsistencia(
    val id: String,
    val estudianteId: String,
    val cursoId: String,
    val fecha: String,
    val estado: EstadoAsistencia,
    val registradoPor: String,
    val fechaHoraRegistro: String
)
