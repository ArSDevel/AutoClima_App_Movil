package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ingreso",
    foreignKeys = [
        ForeignKey(entity = Vehiculo::class, parentColumns = ["vehiculoId"], childColumns = ["vehiculoId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Tecnico::class, parentColumns = ["tecnicoId"], childColumns = ["tecnicoId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("vehiculoId"), Index("tecnicoId")]
)
data class Ingreso(
    @PrimaryKey(autoGenerate = true) val ingresoId: Long = 0,
    val vehiculoId: Long,
    val tecnicoId: Long,
    val fecha: Long,
    val motivoIngreso: String? = null,
    val condicionInicial: String? = null
)