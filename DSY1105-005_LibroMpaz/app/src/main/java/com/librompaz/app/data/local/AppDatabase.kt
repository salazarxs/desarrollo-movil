package com.librompaz.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.librompaz.app.data.local.dao.AnotacionDao
import com.librompaz.app.data.local.dao.AsistenciaDao
import com.librompaz.app.data.local.dao.AuditoriaDao
import com.librompaz.app.data.local.dao.CalificacionDao
import com.librompaz.app.data.local.dao.CatalogoDao
import com.librompaz.app.data.local.dao.SesionDao
import com.librompaz.app.model.Anotacion
import com.librompaz.app.model.Asignatura
import com.librompaz.app.model.Calificacion
import com.librompaz.app.model.CargaDocente
import com.librompaz.app.model.Curso
import com.librompaz.app.model.Estudiante
import com.librompaz.app.model.Evaluacion
import com.librompaz.app.model.ObjetivoAprendizaje
import com.librompaz.app.model.RegistroAsistencia
import com.librompaz.app.model.RegistroAuditoria
import com.librompaz.app.model.Usuario

@Database(
    entities = [
        Usuario::class,
        Curso::class,
        Asignatura::class,
        Estudiante::class,
        CargaDocente::class,
        RegistroAsistencia::class,
        ObjetivoAprendizaje::class,
        SesionEntity::class,
        SesionOa::class,
        Evaluacion::class,
        Calificacion::class,
        Anotacion::class,
        RegistroAuditoria::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catalogoDao(): CatalogoDao
    abstract fun asistenciaDao(): AsistenciaDao
    abstract fun sesionDao(): SesionDao
    abstract fun calificacionDao(): CalificacionDao
    abstract fun anotacionDao(): AnotacionDao
    abstract fun auditoriaDao(): AuditoriaDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "libro_mpaz.db"
                ).build().also { instancia = it }
            }
    }
}
