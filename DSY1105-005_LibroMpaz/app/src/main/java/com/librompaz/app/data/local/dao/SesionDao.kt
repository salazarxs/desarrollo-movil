package com.librompaz.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.librompaz.app.data.local.SesionEntity
import com.librompaz.app.data.local.SesionOa
import kotlinx.coroutines.flow.Flow

@Dao
interface SesionDao {
    @Query("SELECT * FROM sesiones ORDER BY fecha DESC, bloque")
    fun todas(): Flow<List<SesionEntity>>

    @Query("SELECT * FROM sesion_oa")
    fun todosLosOa(): Flow<List<SesionOa>>

    @Insert
    suspend fun insertar(sesion: SesionEntity)

    @Insert
    suspend fun insertarOa(enlaces: List<SesionOa>)
}
