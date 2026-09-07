package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import com.example.myapplication.data.local.entity.Ingreso

@Dao
interface IngresoDao {
    @Insert
    suspend fun insertar(ingreso: Ingreso): Long
}