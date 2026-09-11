package com.example.myapplication.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Inicio de sesión.
@Serializable
data object LoginRoute : NavKey

// Panel principal del técnico.
@Serializable
data class DashboardRoute(
    val userId: String = ""
) : NavKey

// Sin ingresoId abre un registro nuevo.
// Con ingresoId abre un ingreso para editarlo.
@Serializable
data class RegistroRoute(
    val userId: String = "",
    val ingresoId: Long? = null
) : NavKey

// Expediente de un ingreso existente.
@Serializable
data class DetalleIngresoRoute(
    val userId: String,
    val ingresoId: Long
) : NavKey