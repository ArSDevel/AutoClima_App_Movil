package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Vehículo de un cliente; la placa es única en toda la tabla.
@Entity(
    tableName = "vehiculo",
    foreignKeys = [
        ForeignKey(
            entity = Cliente::class,
            parentColumns = ["clienteId"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE)],
    indices = [Index("clienteId"), Index("placa", unique = true)])
data class Vehiculo(
    @PrimaryKey(autoGenerate = true) val vehiculoId: Long = 0,
    val clienteId: Long,
    val placa: String,
    // Marca y modelo juntos, por ejemplo "Nissan Versa".
    val modelo: String,
    val color: String?,
    val numeroSerie: String?,
    val kilometraje: Int?,
    val observaciones: String?)