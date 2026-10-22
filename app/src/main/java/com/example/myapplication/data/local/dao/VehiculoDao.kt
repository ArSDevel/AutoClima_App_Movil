package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.local.entity.Vehiculo

// Consultas de los vehículos registrados.
@Dao
interface VehiculoDao {

    // Registra un vehículo y devuelve su identificador.
    @Insert
    suspend fun insertar(vehiculo: Vehiculo): Long

    // La placa es única; la usa el registro para reutilizar el vehículo de un ingreso anterior.
    @Query("SELECT * FROM vehiculo WHERE placa = :placa LIMIT 1")
    suspend fun buscarPorPlaca(placa: String): Vehiculo?

    // Obtiene la ficha actual del vehículo.
    @Query("SELECT * FROM vehiculo WHERE vehiculoId = :id")
    suspend fun obtener(id: Long): Vehiculo?

    // Devuelve la cantidad de filas actualizadas.
    @Update
    suspend fun actualizar(vehiculo: Vehiculo): Int }