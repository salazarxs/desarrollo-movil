package com.librompaz.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Objetivo de Aprendizaje. Pendiente con el CITT: si los OA vienen de un catálogo oficial
 * cerrado o los equipos pueden crear OA de ejemplo.
 */
@Entity(tableName = "objetivos")
data class ObjetivoAprendizaje(
    @PrimaryKey val id: String,
    val asignaturaId: String,
    val codigo: String,
    val descripcion: String
)

/**
 * Sesión o bloque lectivo con los OA y contenidos trabajados (entidad "Trazabilidad y control
 * curricular" del caso).
 */
data class SesionClase(
    val id: String,
    val cursoId: String,
    val asignaturaId: String,
    val fecha: String,
    val bloque: String,
    val oaIds: List<String>,
    val contenido: String,
    val registradoPor: String,
    val fechaHoraRegistro: String
)
