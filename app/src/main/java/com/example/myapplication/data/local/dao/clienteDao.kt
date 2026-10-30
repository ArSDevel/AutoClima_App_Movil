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

    // Reutiliza al cliente cuando el teléfono ya está registrado, aunque se haya escrito con otro formato.
    // Compara solo los dígitos del teléfono guardado, sin espacios, guiones, paréntesis ni signo +.
    // Recibe las formas válidas de un mismo número, por ejemplo 5512345678, 525512345678 y 5215512345678.
    @Query("""SELECT * FROM cliente WHERE
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(telefono, ' ', ''), '-', ''), '(', ''), ')', ''), '+', '') IN (:variantes)
        LIMIT 1""")
    suspend fun buscarPorTelefono(variantes: List<String>): Cliente?

    // Obtiene la ficha actual del cliente.
    @Query("SELECT * FROM cliente WHERE clienteId = :id")
    suspend fun obtener(id: Long): Cliente?

    // Devuelve la cantidad de filas actualizadas.
    @Update
    suspend fun actualizar(cliente: Cliente): Int }