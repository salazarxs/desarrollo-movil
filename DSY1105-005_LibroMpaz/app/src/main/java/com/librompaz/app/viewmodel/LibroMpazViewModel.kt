package com.librompaz.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.librompaz.app.data.local.AppDatabase
import com.librompaz.app.data.repository.LibroMpazRepository
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
import com.librompaz.app.nativo.ExportadorCsv
import com.librompaz.app.nativo.Vibracion
import com.librompaz.app.util.Validaciones
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel único, compartido por todas las pantallas (se crea una sola vez en AppNavigation).
 *
 * Regla de negocio transversal: TODA creación o modificación debe dejar un RegistroAuditoria
 * (usuario, fecha y hora) automático e inalterable.
 */
class LibroMpazViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = LibroMpazRepository(AppDatabase.obtener(application))
    private val vibracion = Vibracion(application)

    init {
        viewModelScope.launch { repositorio.poblarSiEstaVacia() }
    }

    private fun <T> Flow<List<T>>.comoEstado(): StateFlow<List<T>> =
        stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // =====================================================================
    // Módulo "Autenticación y alcance por rol"
    // =====================================================================

    private val usuarios: StateFlow<List<Usuario>> = repositorio.usuarios.comoEstado()

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
    val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo.asStateFlow()

    private val _errorLogin = MutableStateFlow<String?>(null)
    val errorLogin: StateFlow<String?> = _errorLogin.asStateFlow()

    fun validarCorreo(texto: String): String? = Validaciones.correo(texto)
    fun validarClave(texto: String): String? = Validaciones.obligatorio(texto, "tu clave")

    /**
     * Validar campos vacíos (patrón Guía 11). Si es correcto: dejar _usuarioActivo y devolver
     * true (AppNavigation ya lleva a cada perfil a su pantalla). Si no: _errorLogin y false.
     */
    fun login(usuario: String, clave: String): Boolean {
        if (validarCorreo(usuario) != null || validarClave(clave) != null) {
            _errorLogin.value = "Revisa los campos marcados."
            vibracion.error()
            return false
        }
        val encontrado = usuarios.value.find { it.usuario.equals(usuario, ignoreCase = true) && it.clave == clave }
        if (encontrado == null) {
            _errorLogin.value = "Correo o clave incorrectos."
            vibracion.error()
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

    // =====================================================================
    // Datos de referencia y selección (no es lógica de negocio)
    // =====================================================================

    val cursos: StateFlow<List<Curso>> = repositorio.cursos.comoEstado()
    val asignaturas: StateFlow<List<Asignatura>> = repositorio.asignaturas.comoEstado()
    private val estudiantes: StateFlow<List<Estudiante>> = repositorio.estudiantes.comoEstado()
    private val objetivos: StateFlow<List<ObjetivoAprendizaje>> = repositorio.objetivos.comoEstado()
    private val cargas: StateFlow<List<CargaDocente>> = repositorio.cargas.comoEstado()

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
    fun estudiantePorId(id: String?): Estudiante? = estudiantes.value.find { it.id == id }
    fun estudiantesDelCurso(cursoId: String?): List<Estudiante> = estudiantes.value.filter { it.cursoId == cursoId }
    fun objetivosDe(asignaturaId: String?): List<ObjetivoAprendizaje> = objetivos.value.filter { it.asignaturaId == asignaturaId }

    /**
     * RF: "mostrar únicamente los cursos/asignaturas bajo su asignación formal" (RBAC).
     */
    fun misCargas(usuario: Usuario?): List<CargaDocente> = cargas.value.filter { it.docenteId == usuario?.id }

    // =====================================================================
    // Módulo "Asistencia"
    // =====================================================================

    val asistencias: StateFlow<List<RegistroAsistencia>> = repositorio.asistencias.comoEstado()

    fun validarFecha(texto: String): String? = Validaciones.fecha(texto)

    /**
     * Devolver la asistencia ya guardada del curso en esa fecha
     * (idEstudiante -> estado), para mostrarla y poder corregirla.
     * Pendiente con el CITT: si se puede corregir la asistencia de días anteriores.
     */
    fun asistenciaGuardada(cursoId: String, fecha: String): Map<String, EstadoAsistencia> =
        asistencias.value
            .filter { it.cursoId == cursoId && it.fecha == fecha }
            .associate { it.estudianteId to it.estado }

    /**
     * Guardar (o sobrescribir) la asistencia del curso en esa fecha, con
     * registradoPor = usuario activo y la fecha y hora actuales, y dejar un RegistroAuditoria.
     * RF: "registrar la asistencia diaria con estados presente, ausente, atrasado, justificado".
     */
    fun guardarAsistencia(cursoId: String, fecha: String, estados: Map<String, EstadoAsistencia>): Boolean {
        val usuario = _usuarioActivo.value
        if (usuario == null || validarFecha(fecha) != null || estados.isEmpty()) {
            vibracion.error()
            return false
        }
        val detalle = "Asistencia ${cursoPorId(cursoId)?.nombre.orEmpty()}, $fecha"
        viewModelScope.launch { repositorio.guardarAsistencia(usuario, cursoId, fecha, estados, detalle) }
        vibracion.exito()
        return true
    }

    /**
     * Porcentaje de asistencia del curso (0 a 100) para Inspectoría y Dirección.
     */
    fun porcentajeAsistencia(cursoId: String): Int? {
        val registros = asistencias.value.filter { it.cursoId == cursoId }
        if (registros.isEmpty()) return null
        val asistieron = registros.count { it.estado == EstadoAsistencia.PRESENTE || it.estado == EstadoAsistencia.ATRASADO }
        return asistieron * 100 / registros.size
    }

    /**
     * Estudiantes con inasistencias reiteradas (por ejemplo, 3 o más ausencias),
     * con su número de ausencias. RF de Inspectoría: "identificar inasistencias reiteradas".
     */
    fun inasistenciasReiteradas(): List<Pair<Estudiante, Int>> =
        asistencias.value
            .filter { it.estado == EstadoAsistencia.AUSENTE }
            .groupingBy { it.estudianteId }
            .eachCount()
            .filter { it.value >= MINIMO_AUSENCIAS }
            .mapNotNull { (estudianteId, ausencias) -> estudiantePorId(estudianteId)?.let { it to ausencias } }
            .sortedByDescending { it.second }

    // =====================================================================
    // Módulo "Cobertura curricular"
    // =====================================================================

    val sesiones: StateFlow<List<SesionClase>> = repositorio.sesiones.comoEstado()

    fun sesionesDe(cursoId: String?, asignaturaId: String?): List<SesionClase> =
        sesiones.value.filter { it.cursoId == cursoId && it.asignaturaId == asignaturaId }

    fun validarBloque(texto: String): String? = Validaciones.obligatorio(texto, "el bloque")
    fun validarOas(oaIds: Collection<String>): String? = if (oaIds.isEmpty()) "Marca al menos un OA trabajado." else null
    fun validarContenido(texto: String): String? = Validaciones.largoMinimo(texto, 10, "los contenidos")

    /**
     * Guardar la sesión con los OA y el contenido trabajados, con auditoría.
     * RF: "asociar OA y contenidos didácticos a cada sesión/bloque lectivo".
     */
    fun guardarSesion(cursoId: String, asignaturaId: String, fecha: String, bloque: String, oaIds: List<String>, contenido: String): Boolean {
        val usuario = _usuarioActivo.value
        val hayErrores = validarFecha(fecha) != null || validarBloque(bloque) != null ||
            validarOas(oaIds) != null || validarContenido(contenido) != null
        if (usuario == null || hayErrores) {
            vibracion.error()
            return false
        }
        val detalle = "Sesión de clase ${cursoPorId(cursoId)?.nombre.orEmpty()}, ${asignaturaPorId(asignaturaId)?.nombre.orEmpty()}, $fecha"
        viewModelScope.launch { repositorio.guardarSesion(usuario, cursoId, asignaturaId, fecha, bloque, oaIds, contenido, detalle) }
        vibracion.exito()
        return true
    }

    /**
     * Ids de los OA ya trabajados en el curso y asignatura (según las sesiones
     * registradas). RF de UTP: "vistas analíticas de cobertura curricular por curso".
     */
    fun oasTrabajados(cursoId: String?, asignaturaId: String?): Set<String> =
        sesionesDe(cursoId, asignaturaId).flatMap { it.oaIds }.toSet()

    // =====================================================================
    // Módulo "Calificaciones"
    // =====================================================================

    val evaluaciones: StateFlow<List<Evaluacion>> = repositorio.evaluaciones.comoEstado()
    val calificaciones: StateFlow<List<Calificacion>> = repositorio.calificaciones.comoEstado()

    fun evaluacionesDe(cursoId: String?, asignaturaId: String?): List<Evaluacion> =
        evaluaciones.value.filter { it.cursoId == cursoId && it.asignaturaId == asignaturaId }

    fun calificacionesDe(evaluacionId: String?): List<Calificacion> =
        calificaciones.value.filter { it.evaluacionId == evaluacionId }

    fun validarNombreInstrumento(texto: String): String? = Validaciones.largoMinimo(texto, 3, "el nombre del instrumento")

    /**
     * Crear la evaluación (instrumento + fecha) para el curso y la asignatura, con
     * auditoría. Validar nombre y fecha obligatorios.
     */
    fun crearEvaluacion(cursoId: String, asignaturaId: String, nombreInstrumento: String, fecha: String): Boolean {
        val usuario = _usuarioActivo.value
        if (usuario == null || validarNombreInstrumento(nombreInstrumento) != null || validarFecha(fecha) != null) {
            vibracion.error()
            return false
        }
        val detalle = "Evaluación \"$nombreInstrumento\" ${cursoPorId(cursoId)?.nombre.orEmpty()}, ${asignaturaPorId(asignaturaId)?.nombre.orEmpty()}"
        viewModelScope.launch { repositorio.crearEvaluacion(usuario, cursoId, asignaturaId, nombreInstrumento, fecha, detalle) }
        vibracion.exito()
        return true
    }

    /**
     * Validar una nota escrita por el docente y devolver el mensaje de error o
     * null si es válida (número con coma o punto; rango 1,0 a 7,0 si el CITT lo confirma).
     */
    fun validarNota(texto: String): String? = Validaciones.nota(texto)

    fun validarPlanillaNotas(notas: Map<String, String>): String? {
        val escritas = notas.values.filter { it.isNotBlank() }
        return when {
            escritas.isEmpty() -> "Escribe al menos una nota."
            escritas.any { validarNota(it) != null } -> "Corrige las notas marcadas en rojo."
            else -> null
        }
    }

    /**
     * Guardar las notas de la evaluación (idEstudiante -> nota escrita), con
     * auditoría. RF: "registrar calificaciones asociadas a un instrumento y fecha".
     */
    fun guardarCalificaciones(evaluacionId: String, notas: Map<String, String>): Boolean {
        val usuario = _usuarioActivo.value
        val escritas = notas.filterValues { it.isNotBlank() }
        if (usuario == null || validarPlanillaNotas(notas) != null) {
            vibracion.error()
            return false
        }
        val evaluacion = evaluaciones.value.find { it.id == evaluacionId }
        val detalle = "Notas \"${evaluacion?.nombreInstrumento.orEmpty()}\" ${cursoPorId(evaluacion?.cursoId)?.nombre.orEmpty()}"
        val numeros = escritas.mapValues { Validaciones.notaComoNumero(it.value) }
        viewModelScope.launch { repositorio.guardarCalificaciones(usuario, evaluacionId, numeros, detalle) }
        vibracion.exito()
        return true
    }

    /**
     * Detectar desfases u omisiones de registro de notas, por ejemplo
     * evaluaciones con estudiantes sin nota, o cursos/asignaturas sin evaluaciones.
     * RF de UTP: "identificar desfases u omisiones de registro".
     */
    fun desfasesCalificaciones(): List<String> {
        val alertas = mutableListOf<String>()
        evaluaciones.value.forEach { evaluacion ->
            val faltan = estudiantesDelCurso(evaluacion.cursoId).size - calificacionesDe(evaluacion.id).size
            if (faltan > 0) {
                alertas += "${evaluacion.nombreInstrumento} (${cursoPorId(evaluacion.cursoId)?.nombre.orEmpty()}, " +
                    "${asignaturaPorId(evaluacion.asignaturaId)?.nombre.orEmpty()}): faltan $faltan notas."
            }
        }
        cargas.value.forEach { carga ->
            if (evaluacionesDe(carga.cursoId, carga.asignaturaId).isEmpty()) {
                alertas += "${cursoPorId(carga.cursoId)?.nombre.orEmpty()} · ${asignaturaPorId(carga.asignaturaId)?.nombre.orEmpty()}: sin evaluaciones registradas."
            }
        }
        return alertas
    }

    // =====================================================================
    // Módulo "Hoja de vida"
    // =====================================================================

    val anotaciones: StateFlow<List<Anotacion>> = repositorio.anotaciones.comoEstado()

    fun anotacionesDe(estudianteId: String?): List<Anotacion> =
        anotaciones.value.filter { it.estudianteId == estudianteId }

    fun validarEstudiante(estudiante: Estudiante?): String? = Validaciones.seleccion(estudiante, "un estudiante")
    fun validarTipoAnotacion(tipo: TipoAnotacion?): String? = Validaciones.seleccion(tipo, "el tipo de anotación")
    fun validarDescripcion(texto: String): String? = Validaciones.largoMinimo(texto, 10, "la descripción")

    /**
     * Registrar la anotación con marca temporal y "firma digital" del docente
     * (usuario activo + fecha y hora), y dejar auditoría.
     */
    fun registrarAnotacion(estudiante: Estudiante?, tipo: TipoAnotacion?, descripcion: String): Boolean {
        val usuario = _usuarioActivo.value
        val hayErrores = validarEstudiante(estudiante) != null || validarTipoAnotacion(tipo) != null || validarDescripcion(descripcion) != null
        if (usuario == null || estudiante == null || tipo == null || hayErrores) {
            vibracion.error()
            return false
        }
        val detalle = "${tipo.etiqueta} de ${estudiante.nombreFicticio}"
        viewModelScope.launch { repositorio.registrarAnotacion(usuario, estudiante.id, tipo, descripcion, detalle) }
        vibracion.exito()
        return true
    }

    /**
     * Anotaciones que requieren intervención de Inspectoría (por ejemplo,
     * negativas reiteradas de un mismo estudiante).
     */
    fun anotacionesQueRequierenIntervencion(): List<Anotacion> =
        anotaciones.value
            .filter { it.tipo == TipoAnotacion.NEGATIVA }
            .groupBy { it.estudianteId }
            .filter { it.value.size >= MINIMO_NEGATIVAS }
            .flatMap { it.value }

    // =====================================================================
    // Módulo "Panel administrativo" (Dirección) y auditoría
    // =====================================================================

    val auditoria: StateFlow<List<RegistroAuditoria>> = repositorio.auditoria.comoEstado()

    /**
     * Calcular las métricas consolidadas de cada curso (asistencia, sesiones,
     * notas registradas, anotaciones negativas). RF de Dirección: "estado global administrativo
     * del colegio".
     */
    fun resumenPorCurso(): List<ResumenCurso> = cursos.value.map { curso ->
        val evaluacionesDelCurso = evaluaciones.value.filter { it.cursoId == curso.id }.map { it.id }
        ResumenCurso(
            cursoId = curso.id,
            porcentajeAsistencia = porcentajeAsistencia(curso.id),
            sesionesRegistradas = sesiones.value.count { it.cursoId == curso.id },
            calificacionesRegistradas = calificaciones.value.count { it.evaluacionId in evaluacionesDelCurso },
            anotacionesNegativas = anotaciones.value.count {
                it.tipo == TipoAnotacion.NEGATIVA && estudiantePorId(it.estudianteId)?.cursoId == curso.id
            }
        )
    }

    fun csvAsistencia(): String = buildString {
        appendLine(ExportadorCsv.fila("Fecha", "Curso", "Estudiante", "Estado", "Registrado por", "Fecha y hora de registro"))
        asistencias.value.forEach {
            appendLine(
                ExportadorCsv.fila(
                    it.fecha,
                    cursoPorId(it.cursoId)?.nombre.orEmpty(),
                    estudiantePorId(it.estudianteId)?.nombreFicticio.orEmpty(),
                    it.estado.etiqueta,
                    it.registradoPor,
                    it.fechaHoraRegistro
                )
            )
        }
    }

    fun csvAuditoria(): String = buildString {
        appendLine(ExportadorCsv.fila("Fecha y hora", "Usuario", "Perfil", "Acción", "Detalle"))
        auditoria.value.forEach {
            appendLine(ExportadorCsv.fila(it.fechaHora, it.usuario, it.perfil.etiqueta, it.accion, it.detalle))
        }
    }

    fun exportarCsv(uri: Uri, contenido: String, detalle: String): Boolean {
        val usuario = _usuarioActivo.value
        val escrito = ExportadorCsv.escribir(getApplication(), uri, contenido)
        if (!escrito || usuario == null) {
            vibracion.error()
            return false
        }
        viewModelScope.launch { repositorio.registrarExportacion(usuario, detalle) }
        vibracion.exito()
        return true
    }

    companion object {
        const val MINIMO_AUSENCIAS = 3
        const val MINIMO_NEGATIVAS = 2
    }
}
