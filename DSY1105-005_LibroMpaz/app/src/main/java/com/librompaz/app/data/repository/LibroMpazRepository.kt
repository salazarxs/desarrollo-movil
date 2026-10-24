package com.librompaz.app.data.repository

import androidx.room.withTransaction
import com.librompaz.app.data.MockData
import com.librompaz.app.data.local.AppDatabase
import com.librompaz.app.data.local.SesionEntity
import com.librompaz.app.data.local.SesionOa
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
import com.librompaz.app.model.SesionClase
import com.librompaz.app.model.TipoAnotacion
import com.librompaz.app.model.Usuario
import com.librompaz.app.util.ahora
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID

class LibroMpazRepository(private val db: AppDatabase) {
    private val catalogoDao = db.catalogoDao()
    private val asistenciaDao = db.asistenciaDao()
    private val sesionDao = db.sesionDao()
    private val calificacionDao = db.calificacionDao()
    private val anotacionDao = db.anotacionDao()
    private val auditoriaDao = db.auditoriaDao()

    val usuarios: Flow<List<Usuario>> = catalogoDao.usuarios()
    val cursos: Flow<List<Curso>> = catalogoDao.cursos()
    val asignaturas: Flow<List<Asignatura>> = catalogoDao.asignaturas()
    val estudiantes: Flow<List<Estudiante>> = catalogoDao.estudiantes()
    val cargas: Flow<List<CargaDocente>> = catalogoDao.cargas()
    val objetivos: Flow<List<ObjetivoAprendizaje>> = catalogoDao.objetivos()
    val asistencias: Flow<List<RegistroAsistencia>> = asistenciaDao.todas()
    val evaluaciones: Flow<List<Evaluacion>> = calificacionDao.evaluaciones()
    val calificaciones: Flow<List<Calificacion>> = calificacionDao.calificaciones()
    val anotaciones: Flow<List<Anotacion>> = anotacionDao.todas()
    val auditoria: Flow<List<RegistroAuditoria>> = auditoriaDao.todos()

    val sesiones: Flow<List<SesionClase>> =
        combine(sesionDao.todas(), sesionDao.todosLosOa()) { sesiones, enlaces ->
            sesiones.map { sesion ->
                SesionClase(
                    id = sesion.id,
                    cursoId = sesion.cursoId,
                    asignaturaId = sesion.asignaturaId,
                    fecha = sesion.fecha,
                    bloque = sesion.bloque,
                    oaIds = enlaces.filter { it.sesionId == sesion.id }.map { it.oaId },
                    contenido = sesion.contenido,
                    registradoPor = sesion.registradoPor,
                    fechaHoraRegistro = sesion.fechaHoraRegistro
                )
            }
        }

    suspend fun poblarSiEstaVacia() {
        if (catalogoDao.contarUsuarios() > 0) return
        db.withTransaction {
            catalogoDao.insertarUsuarios(MockData.usuarios)
            catalogoDao.insertarCursos(MockData.cursos)
            catalogoDao.insertarAsignaturas(MockData.asignaturas)
            catalogoDao.insertarEstudiantes(MockData.estudiantes)
            catalogoDao.insertarCargas(MockData.cargas)
            catalogoDao.insertarObjetivos(MockData.objetivos)
            asistenciaDao.guardar(MockData.asistencias)
            MockData.sesiones.forEach { sesion ->
                sesionDao.insertar(sesion.comoEntidad())
                sesionDao.insertarOa(sesion.oaIds.map { SesionOa(sesion.id, it) })
            }
            calificacionDao.insertarEvaluaciones(MockData.evaluaciones)
            calificacionDao.guardarNotas(MockData.calificaciones)
            anotacionDao.insertarVarias(MockData.anotaciones)
            auditoriaDao.insertarVarios(MockData.auditoria)
        }
    }

    suspend fun guardarAsistencia(
        usuario: Usuario,
        cursoId: String,
        fecha: String,
        estados: Map<String, EstadoAsistencia>,
        detalle: String
    ) {
        val fechaHora = ahora()
        db.withTransaction {
            val existia = asistenciaDao.contarDelDia(cursoId, fecha) > 0
            asistenciaDao.guardar(
                estados.map { (estudianteId, estado) ->
                    RegistroAsistencia(
                        id = idAsistencia(fecha, estudianteId),
                        estudianteId = estudianteId,
                        cursoId = cursoId,
                        fecha = fecha,
                        estado = estado,
                        registradoPor = usuario.nombre,
                        fechaHoraRegistro = fechaHora
                    )
                }
            )
            auditar(usuario, if (existia) "MODIFICAR" else "CREAR", detalle, fechaHora)
        }
    }

    suspend fun guardarSesion(
        usuario: Usuario,
        cursoId: String,
        asignaturaId: String,
        fecha: String,
        bloque: String,
        oaIds: List<String>,
        contenido: String,
        detalle: String
    ) {
        val fechaHora = ahora()
        val sesion = SesionEntity(UUID.randomUUID().toString(), cursoId, asignaturaId, fecha, bloque, contenido, usuario.nombre, fechaHora)
        db.withTransaction {
            sesionDao.insertar(sesion)
            sesionDao.insertarOa(oaIds.map { SesionOa(sesion.id, it) })
            auditar(usuario, "CREAR", detalle, fechaHora)
        }
    }

    suspend fun crearEvaluacion(
        usuario: Usuario,
        cursoId: String,
        asignaturaId: String,
        nombreInstrumento: String,
        fecha: String,
        detalle: String
    ) {
        db.withTransaction {
            calificacionDao.insertarEvaluacion(Evaluacion(UUID.randomUUID().toString(), cursoId, asignaturaId, nombreInstrumento, fecha))
            auditar(usuario, "CREAR", detalle, ahora())
        }
    }

    suspend fun guardarCalificaciones(usuario: Usuario, evaluacionId: String, notas: Map<String, Double>, detalle: String) {
        db.withTransaction {
            val existia = calificacionDao.contarNotas(evaluacionId) > 0
            calificacionDao.guardarNotas(
                notas.map { (estudianteId, nota) ->
                    Calificacion(idCalificacion(evaluacionId, estudianteId), evaluacionId, estudianteId, nota, usuario.nombre)
                }
            )
            auditar(usuario, if (existia) "MODIFICAR" else "CREAR", detalle, ahora())
        }
    }

    suspend fun registrarAnotacion(usuario: Usuario, estudianteId: String, tipo: TipoAnotacion, descripcion: String, detalle: String) {
        val fechaHora = ahora()
        db.withTransaction {
            anotacionDao.insertar(Anotacion(UUID.randomUUID().toString(), estudianteId, tipo, descripcion, fechaHora, usuario.nombre))
            auditar(usuario, "CREAR", detalle, fechaHora)
        }
    }

    suspend fun registrarExportacion(usuario: Usuario, detalle: String) {
        auditar(usuario, "EXPORTAR", detalle, ahora())
    }

    private suspend fun auditar(usuario: Usuario, accion: String, detalle: String, fechaHora: String) {
        auditoriaDao.insertar(
            RegistroAuditoria(UUID.randomUUID().toString(), usuario.nombre, usuario.perfil, accion, detalle, fechaHora)
        )
    }

    private fun SesionClase.comoEntidad() =
        SesionEntity(id, cursoId, asignaturaId, fecha, bloque, contenido, registradoPor, fechaHoraRegistro)

    companion object {
        fun idAsistencia(fecha: String, estudianteId: String) = "RA-$fecha-$estudianteId"

        fun idCalificacion(evaluacionId: String, estudianteId: String) = "N-$evaluacionId-$estudianteId"
    }
}
