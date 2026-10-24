package com.librompaz.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Curso o nivel (entidad "Estudiante y curso" del caso). */
@Entity(tableName = "cursos")
data class Curso(
    @PrimaryKey val id: String,
    val nombre: String
)

@Entity(tableName = "asignaturas")
data class Asignatura(
    @PrimaryKey val id: String,
    val nombre: String
)

/** Estudiante con identificación interna y nombre FICTICIO (prohibido usar datos reales o RUT). */
@Entity(tableName = "estudiantes")
data class Estudiante(
    @PrimaryKey val id: String,
    val nombreFicticio: String,
    val cursoId: String
)

/**
 * Asignación de carga docente (entidad del caso): vínculo docente–curso–asignatura.
 * RF: el docente solo ve los cursos y asignaturas bajo su asignación formal.
 */
@Entity(tableName = "cargas_docentes")
data class CargaDocente(
    @PrimaryKey val id: String,
    val docenteId: String,
    val cursoId: String,
    val asignaturaId: String
)
