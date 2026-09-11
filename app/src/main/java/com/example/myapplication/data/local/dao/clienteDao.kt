package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.local.entity.Cliente

@Dao
interface ClienteDao {
    @Insert
    suspend fun insertar(cliente: Cliente): Long

    @Query("SELECT * FROM cliente WHERE telefono = :telefono LIMIT 1")
    suspend fun buscarPorTelefono(telefono: String): Cliente?

    // Obtiene la ficha actual del cliente.
    @Query("SELECT * FROM cliente WHERE clienteId = :id")
    suspend fun obtener(id: Long): Cliente?

    // Devuelve la cantidad de filasS actualizadas.
    @Update
    suspend fun actualizar(cliente: Cliente): Int
}