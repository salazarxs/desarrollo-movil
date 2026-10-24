package com.librompaz.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.librompaz.app.model.Asignatura
import com.librompaz.app.model.CargaDocente
import com.librompaz.app.model.Curso
import com.librompaz.app.model.Estudiante
import com.librompaz.app.model.ObjetivoAprendizaje
import com.librompaz.app.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogoDao {
    @Query("SELECT * FROM usuarios")
    fun usuarios(): Flow<List<Usuario>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int

    @Query("SELECT * FROM cursos ORDER BY nombre")
    fun cursos(): Flow<List<Curso>>

    @Query("SELECT * FROM asignaturas ORDER BY nombre")
    fun asignaturas(): Flow<List<Asignatura>>

    @Query("SELECT * FROM estudiantes ORDER BY nombreFicticio")
    fun estudiantes(): Flow<List<Estudiante>>

    @Query("SELECT * FROM cargas_docentes")
    fun cargas(): Flow<List<CargaDocente>>

    @Query("SELECT * FROM objetivos ORDER BY codigo")
    fun objetivos(): Flow<List<ObjetivoAprendizaje>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarUsuarios(usuarios: List<Usuario>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarCursos(cursos: List<Curso>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarAsignaturas(asignaturas: List<Asignatura>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarEstudiantes(estudiantes: List<Estudiante>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarCargas(cargas: List<CargaDocente>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarObjetivos(objetivos: List<ObjetivoAprendizaje>)
}
