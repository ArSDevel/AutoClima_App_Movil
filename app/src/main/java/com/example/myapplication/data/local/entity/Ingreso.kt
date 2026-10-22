package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.myapplication.data.local.model.EstadoIngreso

// Visita de un vehículo al taller. Al borrar el vehículo se borran sus ingresos; el técnico no puede borrarse
// mientras tenga ingresos.
@Entity(
    tableName = "ingreso",
    foreignKeys = [
        ForeignKey(entity = Vehiculo::class, parentColumns = ["vehiculoId"], childColumns = ["vehiculoId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Tecnico::class, parentColumns = ["tecnicoId"], childColumns = ["tecnicoId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("vehiculoId"), Index("tecnicoId")])
data class Ingreso(
    @PrimaryKey(autoGenerate = true) val ingresoId: Long = 0,
    val vehiculoId: Long,
    val tecnicoId: Long,
    val fecha: Long,
    val motivoIngreso: String? = null,
    val condicionInicial: String? = null,
    // Se guarda el nombre de EstadoIngreso como texto.
    val estado: String = EstadoIngreso.REGISTRADO.name,
    // Ruta de la foto principal; las demás viven en la tabla de evidencias.
    val fotoPath: String? = null)