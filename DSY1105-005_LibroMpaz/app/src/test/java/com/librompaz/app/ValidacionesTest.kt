package com.librompaz.app

import com.librompaz.app.util.Validaciones
import com.librompaz.app.util.hoy
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesTest {
    @Test
    fun fechas() {
        assertNull(Validaciones.fecha("2026-10-01"))
        assertNull(Validaciones.fecha(hoy()))
        assertNotNull(Validaciones.fecha(""))
        assertNotNull(Validaciones.fecha("01-10-2026"))
        assertNotNull(Validaciones.fecha("2026-02-30"))
        assertNotNull(Validaciones.fecha("2999-01-01"))
    }

    @Test
    fun correos() {
        assertNull(Validaciones.correo("docente.mat@libromp.cl"))
        assertNotNull(Validaciones.correo(""))
        assertNotNull(Validaciones.correo("docente.mat"))
        assertNotNull(Validaciones.correo("docente@"))
    }

    @Test
    fun obligatoriosYLargoMinimo() {
        assertNotNull(Validaciones.obligatorio("   ", "el bloque"))
        assertNull(Validaciones.obligatorio("Bloque 1", "el bloque"))
        assertNotNull(Validaciones.largoMinimo("corto", 10, "la descripción"))
        assertNull(Validaciones.largoMinimo("Descripción suficientemente larga", 10, "la descripción"))
        assertNotNull(Validaciones.seleccion(null, "un estudiante"))
        assertNull(Validaciones.seleccion("E3A-1", "un estudiante"))
    }
}
