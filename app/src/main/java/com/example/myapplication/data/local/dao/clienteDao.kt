package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.data.local.entity.Cliente

@Dao
interface ClienteDao {
    @Insert
    suspend fun insertar(cliente: Cliente): Long

    @Query("SELECT * FROM cliente WHERE telefono = :telefono LIMIT 1")
    suspend fun buscarPorTelefono(telefono: String): Cliente?
}