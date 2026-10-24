package com.librompaz.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EstadoAsistencia(val etiqueta: String, val abreviatura: String) {
    PRESENTE("Presente", "P"),
    AUSENTE("Ausente", "A"),
    ATRASADO("Atrasado", "T"),
    JUSTIFICADO("Justificado", "J")
}

/**
 * Registro de asistencia diaria (entidad del caso). registradoPor y fechaHoraRegistro son la
 * "firma" automática exigida por la regla de auditoría.
 */
@Entity(tableName = "asistencias")
data class RegistroAsistencia(
    @PrimaryKey val id: String,
    val estudianteId: String,
    val cursoId: String,
    val fecha: String,
    val estado: EstadoAsistencia,
    val registradoPor: String,
    val fechaHoraRegistro: String
)
