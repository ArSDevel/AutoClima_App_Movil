package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.data.local.entity.Tecnico

@Dao
interface TecnicoDao {

    @Insert
    suspend fun insertar(tecnico: Tecnico): Long

    @Query("SELECT * FROM tecnico WHERE usuario = :usuario LIMIT 1")
    suspend fun buscarPorUsuario(usuario: String): Tecnico?

    @Query("SELECT * FROM tecnico WHERE usuario = :usuario AND passwordHash = :passwordHash LIMIT 1")
    suspend fun login(usuario: String, passwordHash: String): Tecnico?
}