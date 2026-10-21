package com.example.myapplication.data.local.model

// Pasos que deben estar guardados antes de entregar el vehículo.
data class RequisitosEntrega(
    val checklist: Boolean,
    val diagnostico: Boolean,
    val firma: Boolean
) {
    // La entrega solo se confirma con los tres pasos completos.
    val completos: Boolean
        get() = checklist && diagnostico && firma }
