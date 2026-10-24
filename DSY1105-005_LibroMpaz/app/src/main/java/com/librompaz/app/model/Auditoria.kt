package com.librompaz.app.model

data class RegistroAuditoria(
    val id: String,
    val usuario: String,
    val perfil: Perfil,
    val accion: String,
    val detalle: String,
    val fechaHora: String
)

data class ResumenCurso(
    val cursoId: String,
    val porcentajeAsistencia: Int? = null,
    val sesionesRegistradas: Int? = null,
    val calificacionesRegistradas: Int? = null,
    val anotacionesNegativas: Int? = null
)
