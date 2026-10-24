package com.librompaz.app.model

data class ObjetivoAprendizaje(
    val id: String,
    val asignaturaId: String,
    val codigo: String,
    val descripcion: String
)

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
