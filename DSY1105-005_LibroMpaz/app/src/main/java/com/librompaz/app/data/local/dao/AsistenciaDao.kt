package com.librompaz.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.librompaz.app.model.RegistroAsistencia
import kotlinx.coroutines.flow.Flow

@Dao
interface AsistenciaDao {
    @Query("SELECT * FROM asistencias ORDER BY fecha DESC")
    fun todas(): Flow<List<RegistroAsistencia>>

    @Query("SELECT COUNT(*) FROM asistencias WHERE cursoId = :cursoId AND fecha = :fecha")
    suspend fun contarDelDia(cursoId: String, fecha: String): Int

    @Upsert
    suspend fun guardar(registros: List<RegistroAsistencia>)
}
