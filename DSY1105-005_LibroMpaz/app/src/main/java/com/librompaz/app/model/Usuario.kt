package com.librompaz.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Usuario del sistema. Solo datos ficticios (ver MockData.usuarios). */
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val id: String,
    val nombre: String,
    val usuario: String,
    val clave: String,
    val perfil: Perfil
)
