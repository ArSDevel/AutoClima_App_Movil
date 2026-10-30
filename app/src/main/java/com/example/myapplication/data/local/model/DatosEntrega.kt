package com.example.myapplication.data.local.model

// Cómo se cerró un ingreso: cuándo, quién, con qué nota y qué pasos estaban completos.
data class DatosEntrega(
    val fecha: Long,
    val tecnicoId: Long,
    // null cuando no se escribió una nota.
    val nota: String?,
    val requisitos: RequisitosEntrega)
