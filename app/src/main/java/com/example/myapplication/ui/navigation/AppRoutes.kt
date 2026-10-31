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

// Selección del ingreso cuyo checklist se desea abrir.
@Serializable
data class SeleccionIngresoChecklistRoute(
    val userId: String
) : NavKey

// Selección del ingreso cuyo diagnóstico se desea abrir.
@Serializable
data class SeleccionIngresoDiagnosticoRoute(
    val userId: String
) : NavKey

// Selección del ingreso cuyas fotografías se desean consultar.
@Serializable
data class SeleccionIngresoEvidenciasRoute(
    val userId: String
) : NavKey

// Reporte y cotización asociados a un ingreso.
@Serializable
data class DiagnosticoRoute(
    val userId: String,
    val ingresoId: Long
) : NavKey

// Checklist asociado a un ingreso existente.
@Serializable
data class ChecklistRoute(
    val userId: String,
    val ingresoId: Long
) : NavKey

// Expediente de un ingreso existente.
@Serializable
data class DetalleIngresoRoute(
    val userId: String,
    val ingresoId: Long
) : NavKey

// Galería y captura de evidencias de un ingreso.
@Serializable
data class EvidenciasIngresoRoute(
    val userId: String,
    val ingresoId: Long
) : NavKey

// Bitácora general de operaciones del taller.
@Serializable
data class HistorialRoute(val userId: String) : NavKey

// Selección del ingreso que se va a firmar o entregar.
@Serializable
data class SeleccionIngresoFirmaRoute(
    val userId: String
) : NavKey

// Firma del cliente de un ingreso listo para firma.
@Serializable
data class FirmaRoute(
    val userId: String,
    val ingresoId: Long
) : NavKey

// Entrega del vehículo de un ingreso firmado.
@Serializable
data class EntregaRoute(
    val userId: String,
    val ingresoId: Long
) : NavKey

// Lista de usuarios con acceso a la app.
@Serializable
data class UsuariosRoute(val userId: String) : NavKey

// Formulario para agregar un usuario con la sesión abierta.
@Serializable
data class NuevoUsuarioRoute(val userId: String) : NavKey

// Edición de un usuario; tecnicoId es el usuario que se edita, que puede ser el de la sesión.
@Serializable
data class EditarUsuarioRoute(val userId: String, val tecnicoId: Long) : NavKey

// Restablecer la contraseña con el código de recuperación; se abre desde el inicio de sesión, sin sesión.
@Serializable
data object RecuperarPasswordRoute : NavKey
