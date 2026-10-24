package com.librompaz.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.librompaz.app.model.RegistroAuditoria
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditoriaDao {
    @Query("SELECT * FROM auditoria ORDER BY fechaHora DESC")
    fun todos(): Flow<List<RegistroAuditoria>>

    @Insert
    suspend fun insertar(registro: RegistroAuditoria)

    @Insert
    suspend fun insertarVarios(registros: List<RegistroAuditoria>)
}
