package com.librompaz.app.model

enum class TipoAnotacion(val etiqueta: String) {
    POSITIVA("Anotación positiva"),
    NEGATIVA("Anotación negativa"),
    OBSERVACION("Observación conductual")
}

data class Anotacion(
    val id: String,
    val estudianteId: String,
    val tipo: TipoAnotacion,
    val descripcion: String,
    val fechaHora: String,
    val firmadoPor: String
)
