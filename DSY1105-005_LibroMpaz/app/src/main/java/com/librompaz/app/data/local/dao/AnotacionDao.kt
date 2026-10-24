package com.librompaz.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.librompaz.app.model.Anotacion
import kotlinx.coroutines.flow.Flow

@Dao
interface AnotacionDao {
    @Query("SELECT * FROM anotaciones ORDER BY fechaHora DESC")
    fun todas(): Flow<List<Anotacion>>

    @Insert
    suspend fun insertar(anotacion: Anotacion)

    @Insert
    suspend fun insertarVarias(anotaciones: List<Anotacion>)
}
