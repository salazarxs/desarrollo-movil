package com.librompaz.app.viewmodel

import androidx.lifecycle.ViewModel
import com.librompaz.app.data.MockData
import com.librompaz.app.model.Anotacion
import com.librompaz.app.model.Asignatura
import com.librompaz.app.model.Calificacion
import com.librompaz.app.model.CargaDocente
import com.librompaz.app.model.Curso
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.model.Estudiante
import com.librompaz.app.model.Evaluacion
import com.librompaz.app.model.ObjetivoAprendizaje
import com.librompaz.app.model.RegistroAsistencia
import com.librompaz.app.model.RegistroAuditoria
import com.librompaz.app.model.ResumenCurso
import com.librompaz.app.model.SesionClase
import com.librompaz.app.model.TipoAnotacion
import com.librompaz.app.model.Usuario
import com.librompaz.app.util.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LibroMpazViewModel : ViewModel() {
    private val usuarios = MockData.usuarios

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
    val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo.asStateFlow()

    private val _errorLogin = MutableStateFlow<String?>(null)
    val errorLogin: StateFlow<String?> = _errorLogin.asStateFlow()

    fun validarCorreo(texto: String): String? = Validaciones.correo(texto)
    fun validarClave(texto: String): String? = Validaciones.obligatorio(texto, "tu clave")

    fun login(usuario: String, clave: String): Boolean {
        if (validarCorreo(usuario) != null || validarClave(clave) != null) {
            _errorLogin.value = "Revisa los campos marcados."
            return false
        }
        val encontrado = usuarios.find { it.usuario.equals(usuario, ignoreCase = true) && it.clave == clave }
        if (encontrado == null) {
            _errorLogin.value = "Correo o clave incorrectos."
            return false
        }
        _usuarioActivo.value = encontrado
        _errorLogin.value = null
        return true
    }

    fun logout() {
        _usuarioActivo.value = null
        _errorLogin.value = null
        _cursoSeleccionadoId.value = null
        _asignaturaSeleccionadaId.value = null
    }

    val cursos: StateFlow<List<Curso>> = MutableStateFlow(MockData.cursos)
    val asignaturas: StateFlow<List<Asignatura>> = MutableStateFlow(MockData.asignaturas)
    private val estudiantes = MockData.estudiantes
    private val objetivos = MockData.objetivos
    private val cargas = MockData.cargas

    private val _cursoSeleccionadoId = MutableStateFlow<String?>(null)
    val cursoSeleccionadoId: StateFlow<String?> = _cursoSeleccionadoId.asStateFlow()

    private val _asignaturaSeleccionadaId = MutableStateFlow<String?>(null)
    val asignaturaSeleccionadaId: StateFlow<String?> = _asignaturaSeleccionadaId.asStateFlow()

    fun seleccionarCurso(cursoId: String?, asignaturaId: String? = null) {
        _cursoSeleccionadoId.value = cursoId
        _asignaturaSeleccionadaId.value = asignaturaId
    }

    fun cursoPorId(id: String?): Curso? = cursos.value.find { it.id == id }
    fun asignaturaPorId(id: String?): Asignatura? = asignaturas.value.find { it.id == id }
    fun estudiantePorId(id: String?): Estudiante? = estudiantes.find { it.id == id }
    fun estudiantesDelCurso(cursoId: String?): List<Estudiante> = estudiantes.filter { it.cursoId == cursoId }
    fun objetivosDe(asignaturaId: String?): List<ObjetivoAprendizaje> = objetivos.filter { it.asignaturaId == asignaturaId }

    fun misCargas(usuario: Usuario?): List<CargaDocente> = cargas

    val asistencias: StateFlow<List<RegistroAsistencia>> = MutableStateFlow(MockData.asistencias)

    fun validarFecha(texto: String): String? = Validaciones.fecha(texto)

    fun asistenciaGuardada(cursoId: String, fecha: String): Map<String, EstadoAsistencia> = emptyMap()

    fun guardarAsistencia(cursoId: String, fecha: String, estados: Map<String, EstadoAsistencia>): Boolean = false

    fun porcentajeAsistencia(cursoId: String): Int? {
        val registros = asistencias.value.filter { it.cursoId == cursoId }
        if (registros.isEmpty()) return null
        val asistieron = registros.count { it.estado == EstadoAsistencia.PRESENTE || it.estado == EstadoAsistencia.ATRASADO }
        return Math.round(asistieron * 100.0 / registros.size).toInt()
    }

    fun inasistenciasReiteradas(): List<Pair<Estudiante, Int>> = emptyList()

    val sesiones: StateFlow<List<SesionClase>> = MutableStateFlow(MockData.sesiones)

    fun sesionesDe(cursoId: String?, asignaturaId: String?): List<SesionClase> =
        sesiones.value.filter { it.cursoId == cursoId && it.asignaturaId == asignaturaId }

    fun validarBloque(texto: String): String? = Validaciones.obligatorio(texto, "el bloque")
    fun validarOas(oaIds: Collection<String>): String? = if (oaIds.isEmpty()) "Marca al menos un OA trabajado." else null
    fun validarContenido(texto: String): String? = Validaciones.largoMinimo(texto, 10, "los contenidos")

    fun guardarSesion(cursoId: String, asignaturaId: String, fecha: String, bloque: String, oaIds: List<String>, contenido: String): Boolean = false

    fun oasTrabajados(cursoId: String?, asignaturaId: String?): Set<String> = emptySet()

    val evaluaciones: StateFlow<List<Evaluacion>> = MutableStateFlow(MockData.evaluaciones)
    val calificaciones: StateFlow<List<Calificacion>> = MutableStateFlow(MockData.calificaciones)

    fun evaluacionesDe(cursoId: String?, asignaturaId: String?): List<Evaluacion> =
        evaluaciones.value.filter { it.cursoId == cursoId && it.asignaturaId == asignaturaId }

    fun calificacionesDe(evaluacionId: String?): List<Calificacion> =
        calificaciones.value.filter { it.evaluacionId == evaluacionId }

    fun validarNombreInstrumento(texto: String): String? = Validaciones.largoMinimo(texto, 3, "el nombre del instrumento")

    fun crearEvaluacion(cursoId: String, asignaturaId: String, nombreInstrumento: String, fecha: String): Boolean = false

    fun validarNota(texto: String): String? = null

    fun validarPlanillaNotas(notas: Map<String, String>): String? {
        val escritas = notas.values.filter { it.isNotBlank() }
        return when {
            escritas.isEmpty() -> "Escribe al menos una nota."
            escritas.any { validarNota(it) != null } -> "Corrige las notas marcadas en rojo."
            else -> null
        }
    }

    fun guardarCalificaciones(evaluacionId: String, notas: Map<String, String>): Boolean = false

    fun desfasesCalificaciones(): List<String> = emptyList()

    val anotaciones: StateFlow<List<Anotacion>> = MutableStateFlow(MockData.anotaciones)

    fun anotacionesDe(estudianteId: String?): List<Anotacion> =
        anotaciones.value.filter { it.estudianteId == estudianteId }

    fun validarEstudiante(estudiante: Estudiante?): String? = Validaciones.seleccion(estudiante, "un estudiante")
    fun validarTipoAnotacion(tipo: TipoAnotacion?): String? = Validaciones.seleccion(tipo, "el tipo de anotación")
    fun validarDescripcion(texto: String): String? = Validaciones.largoMinimo(texto, 10, "la descripción")

    fun registrarAnotacion(estudiante: Estudiante?, tipo: TipoAnotacion?, descripcion: String): Boolean = false

    fun anotacionesQueRequierenIntervencion(): List<Anotacion> = emptyList()

    val auditoria: StateFlow<List<RegistroAuditoria>> = MutableStateFlow(MockData.auditoria)

    fun resumenPorCurso(): List<ResumenCurso> = cursos.value.map { ResumenCurso(it.id) }
}
