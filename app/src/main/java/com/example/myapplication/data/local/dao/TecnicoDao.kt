package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.data.local.entity.Tecnico
import com.example.myapplication.data.local.model.UsuarioListado
import kotlinx.coroutines.flow.Flow

// Consultas de los técnicos que inician sesión.
@Dao
interface TecnicoDao {

    // Registra un técnico y devuelve su identificador.
    @Insert
    suspend fun insertar(tecnico: Tecnico): Long

    // Busca por nombre de usuario sin validar la contraseña.
    @Query(" SELECT * FROM tecnico WHERE usuario = :usuario LIMIT 1")
    suspend fun buscarPorUsuario(usuario: String): Tecnico?


    // Permite comprobar que el técnico de la sesión existe.
    @Query("SELECT * FROM tecnico WHERE tecnicoId = :id")
    suspend fun obtener(id: Long): Tecnico?

    // Cantidad de técnicos registrados; en cero la app muestra el primer uso.
    @Query("SELECT COUNT(*) FROM tecnico")
    fun observarCantidad(): Flow<Int>

    // Cuenta los usuarios con ese nombre sin distinguir mayúsculas, para no permitir "Erik" y "erik".
    @Query("SELECT COUNT(*) FROM tecnico WHERE LOWER(usuario) = LOWER(:usuario)")
    suspend fun contarPorUsuario(usuario: String): Int

    // Lista de usuarios con su actividad; se actualiza sola al cambiar cualquiera de las tablas consultadas.
    @Query("""
        SELECT t.tecnicoId AS tecnicoId, t.nombre AS nombre, t.usuario AS usuario,
            (t.recuperacionHash IS NOT NULL) AS tieneRecuperacion,
            (SELECT COUNT(*) FROM ingreso i WHERE i.tecnicoId = t.tecnicoId) AS ingresos,
            (SELECT COUNT(*) FROM ingreso i WHERE i.tecnicoId = t.tecnicoId)
                + (SELECT COUNT(*) FROM evento_ingreso e WHERE e.tecnicoId = t.tecnicoId)
                + (SELECT COUNT(*) FROM firma f WHERE f.tecnicoId = t.tecnicoId)
                + (SELECT COUNT(*) FROM diagnostico d WHERE d.tecnicoId = t.tecnicoId)
                + (SELECT COUNT(*) FROM tabla_evidencias v WHERE v.tecnicoId = t.tecnicoId) AS actividad
        FROM tecnico t
        ORDER BY LOWER(t.nombre), t.tecnicoId""")
    fun observarListado(): Flow<List<UsuarioListado>>

    // Elimina al técnico solo si no es el de la sesión y no dejó registros; devuelve las filas borradas (0 o 1).
    // La comprobación y el borrado van en una sola sentencia para que nada cambie entre una y otra.
    @Query("""
        DELETE FROM tecnico
        WHERE tecnicoId = :id AND tecnicoId != :actualId
            AND NOT EXISTS (SELECT 1 FROM ingreso WHERE tecnicoId = :id)
            AND NOT EXISTS (SELECT 1 FROM evento_ingreso WHERE tecnicoId = :id)
            AND NOT EXISTS (SELECT 1 FROM firma WHERE tecnicoId = :id)
            AND NOT EXISTS (SELECT 1 FROM diagnostico WHERE tecnicoId = :id)
            AND NOT EXISTS (SELECT 1 FROM tabla_evidencias WHERE tecnicoId = :id)""")
    suspend fun eliminarSinActividad(id: Long, actualId: Long): Int

    // Como contarPorUsuario, pero sin contar al propio técnico; sirve al editar su nombre de usuario.
    @Query("SELECT COUNT(*) FROM tecnico WHERE LOWER(usuario) = LOWER(:usuario) AND tecnicoId != :id")
    suspend fun contarPorUsuarioExcepto(usuario: String, id: Long): Int

    // Cambia el nombre y el usuario con que inicia sesión.
    @Query("UPDATE tecnico SET nombre = :nombre, usuario = :usuario WHERE tecnicoId = :id")
    suspend fun actualizarDatos(id: Long, nombre: String, usuario: String): Int

    // Guarda un hash de contraseña nuevo: cambio de contraseña o actualización del formato.
    @Query("UPDATE tecnico SET passwordHash = :passwordHash WHERE tecnicoId = :id")
    suspend fun actualizarPassword(id: Long, passwordHash: String): Int

    // Guarda el hash del código de recuperación.
    @Query("UPDATE tecnico SET recuperacionHash = :recuperacionHash WHERE tecnicoId = :id")
    suspend fun actualizarRecuperacion(id: Long, recuperacionHash: String): Int

    // Restablece la contraseña con el código de recuperación y deja uno nuevo. Solo actúa si el código guardado sigue
    // siendo el que se comprobó, así un mismo código no sirve dos veces aunque se use al mismo tiempo.
    @Query("""
        UPDATE tecnico SET passwordHash = :passwordHash, recuperacionHash = :recuperacionNuevo
        WHERE tecnicoId = :id AND recuperacionHash = :recuperacionActual""")
    suspend fun restablecerConCodigo(id: Long, passwordHash: String, recuperacionNuevo: String,
                                     recuperacionActual: String): Int }
