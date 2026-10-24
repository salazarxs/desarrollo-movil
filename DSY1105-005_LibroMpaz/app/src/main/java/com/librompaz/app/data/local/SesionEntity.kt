package com.librompaz.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sesiones")
data class SesionEntity(
    @PrimaryKey val id: String,
    val cursoId: String,
    val asignaturaId: String,
    val fecha: String,
    val bloque: String,
    val contenido: String,
    val registradoPor: String,
    val fechaHoraRegistro: String
)

@Entity(tableName = "sesion_oa", primaryKeys = ["sesionId", "oaId"])
data class SesionOa(
    val sesionId: String,
    val oaId: String
)
