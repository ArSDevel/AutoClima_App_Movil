package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Revisión del sistema de aire acondicionado de un ingreso; cada casilla en true indica un problema detectado.
@Entity(
    tableName = "checklist_diagnostico",
    foreignKeys = [
        ForeignKey(
            entity = Ingreso::class,
            parentColumns = ["ingresoId"],
            childColumns = ["ingresoId"],
            onDelete = ForeignKey.CASCADE)],
    // Un checklist por ingreso, no varios.
    indices = [Index("ingresoId", unique = true)])
data class ChecklistDiagnostico(
    @PrimaryKey(autoGenerate = true) val checklistId: Long = 0,
    val ingresoId: Long,

    val faltaGasRefrigerante: Boolean = false,
    val fugaVisibleMangueras: Boolean = false,
    val fugasDetectadasLuzUV: Boolean = false,
    val compresorEmbragaAlEncender: Boolean = false,
    val compresorDanadoOAmarrado: Boolean = false,
    val bandaCompresorDesgastada: Boolean = false,
    val funcionanAbanicosRadiador: Boolean = false,
    val filtroCabinaSucioUObstruido: Boolean = false,
    val condensadorObstruido: Boolean = false,

    // Texto libre del técnico; null cuando no escribió nada.
    val comentarios: String?,
    // Momento del último guardado, en milisegundos.
    val fecha: Long = System.currentTimeMillis())