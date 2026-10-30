package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.local.entity.Cliente

// Consultas de los clientes del taller.
@Dao
interface ClienteDao {
    // Registra un cliente y devuelve su identificador.
    @Insert
    suspend fun insertar(cliente: Cliente): Long

    // Reutiliza al cliente cuando el teléfono ya está registrado; compara el texto exacto, sin normalizar.
    @Query("SELECT * FROM cliente WHERE telefono = :telefono LIMIT 1")
    suspend fun buscarPorTelefono(telefono: String): Cliente?

    // Obtiene la ficha actual del cliente.
    @Query("SELECT * FROM cliente WHERE clienteId = :id")
    suspend fun obtener(id: Long): Cliente?

    // Devuelve la cantidad de filas actualizadas.
    @Update
    suspend fun actualizar(cliente: Cliente): Int }