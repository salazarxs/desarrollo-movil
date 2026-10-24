package com.librompaz.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.librompaz.app.model.Calificacion
import com.librompaz.app.model.Evaluacion
import kotlinx.coroutines.flow.Flow

@Dao
interface CalificacionDao {
    @Query("SELECT * FROM evaluaciones ORDER BY fecha DESC")
    fun evaluaciones(): Flow<List<Evaluacion>>

    @Query("SELECT * FROM calificaciones")
    fun calificaciones(): Flow<List<Calificacion>>

    @Query("SELECT COUNT(*) FROM calificaciones WHERE evaluacionId = :evaluacionId")
    suspend fun contarNotas(evaluacionId: String): Int

    @Insert
    suspend fun insertarEvaluacion(evaluacion: Evaluacion)

    @Insert
    suspend fun insertarEvaluaciones(evaluaciones: List<Evaluacion>)

    @Upsert
    suspend fun guardarNotas(notas: List<Calificacion>)
}
