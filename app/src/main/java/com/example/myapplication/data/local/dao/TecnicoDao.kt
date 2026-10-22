package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.data.local.entity.Tecnico

// Consultas de los técnicos que inician sesión.
@Dao
interface TecnicoDao {

    // Registra un técnico y devuelve su identificador.
    @Insert
    suspend fun insertar(tecnico: Tecnico): Long

    // Busca por nombre de usuario sin validar la contraseña.
    @Query(" SELECT * FROM tecnico WHERE usuario = :usuario LIMIT 1")
    suspend fun buscarPorUsuario(usuario: String): Tecnico?

    // Comprueba usuario y hash de contraseña juntos; devuelve null si no coinciden.
    @Query("SELECT * FROM tecnico WHERE usuario = :usuario AND passwordHash = :passwordHash LIMIT 1")
    suspend fun login(usuario: String, passwordHash: String): Tecnico?

    // Permite comprobar que el técnico de la sesión existe.
    @Query("SELECT * FROM tecnico WHERE tecnicoId = :id")
    suspend fun obtener(id: Long): Tecnico? }