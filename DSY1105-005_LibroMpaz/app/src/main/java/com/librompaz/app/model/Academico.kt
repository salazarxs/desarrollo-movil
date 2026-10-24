package com.librompaz.app.model

data class Curso(
    val id: String,
    val nombre: String
)

data class Asignatura(
    val id: String,
    val nombre: String
)

data class Estudiante(
    val id: String,
    val nombreFicticio: String,
    val cursoId: String
)

data class CargaDocente(
    val id: String,
    val docenteId: String,
    val cursoId: String,
    val asignaturaId: String
)
