package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Una fila de la cotización del diagnóstico.
// El mecánico captura la descripción y el importe.
@Entity(
    tableName = "concepto_cotizacion",
    foreignKeys = [
        ForeignKey(
            entity = Diagnostico::class,
            parentColumns = ["diagnosticoId"],
            childColumns = ["diagnosticoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["diagnosticoId"])
    ]
)
data class ConceptoCotizacion(
    @PrimaryKey(autoGenerate = true)
    val conceptoId: Long = 0,

    val diagnosticoId: Long,

    // Concepto de servicio, refacción o mano de obra.
    val descripcion: String,

    // Importe total de esta fila, expresado en centavos.
    // Ejemplo: $150.50 se guarda como 15050.
    // Evita errores de precisión al sumar cantidades monetarias.
    val importeCentavos: Long,

    // Conserva el orden de las filas mostrado por el mecánico.
    val orden: Int = 0
)