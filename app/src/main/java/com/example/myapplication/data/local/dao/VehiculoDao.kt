package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.data.local.entity.Vehiculo

@Dao
interface VehiculoDao {
    @Insert
    suspend fun insertar(vehiculo: Vehiculo): Long

    @Query("SELECT * FROM vehiculo WHERE placa = :placa LIMIT 1")
    suspend fun buscarPorPlaca(placa: String): Vehiculo?
}