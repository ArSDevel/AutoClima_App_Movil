package com.example.myapplication.data.respaldo

import java.text.Normalizer

// Modelos y reglas de la combinación de un respaldo con los datos del celular. Todo aquí es lógica pura, sin base de
// datos, para poder probarla con facilidad; ServicioRespaldo la usa al leer y escribir.

// Cantidades de datos de un celular o de un respaldo.
data class ConteosDatos(val ingresos: Int, val vehiculos: Int, val clientes: Int, val usuarios: Int)

// De qué lado queda un ingreso que existe en los dos.
enum class Lado { CELULAR, RESPALDO }

// Qué hacer con los ingresos que existen en los dos y son distintos.
enum class ModoCombinacion { MAS_RECIENTE, SIEMPRE_CELULAR, SIEMPRE_RESPALDO }

// Lo que se compara de un ingreso en cada lado.
data class EstadoDeIngreso(
    val estado: String,
    val ultimoMovimiento: Long,
    val fotos: Int,
    val tieneFirma: Boolean)

// Un ingreso del respaldo y, si existe, su equivalente en el celular.
data class IngresoComparado(
    val idRespaldo: Long,
    val idCelular: Long?,
    val placa: String,
    val fecha: Long,
    val motivo: String,
    val celular: EstadoDeIngreso?,
    val respaldo: EstadoDeIngreso) {

    // Solo está en el respaldo.
    val esNuevo: Boolean get() = celular == null
    // Está en los dos y no hay diferencias.
    val esIgual: Boolean get() = celular != null && celular == respaldo
    // Está en los dos y uno avanzó más que el otro.
    val esDistinto: Boolean get() = celular != null && celular != respaldo

    // Quién gana según el modo elegido; con empate en "más reciente" se queda lo del celular.
    fun ganadorSegun(modo: ModoCombinacion): Lado = when (modo) {
        ModoCombinacion.SIEMPRE_CELULAR -> Lado.CELULAR
        ModoCombinacion.SIEMPRE_RESPALDO -> Lado.RESPALDO
        ModoCombinacion.MAS_RECIENTE ->
            if (celular != null && respaldo.ultimoMovimiento > celular.ultimoMovimiento) Lado.RESPALDO else Lado.CELULAR } }

// Resultado del análisis, antes de aplicar nada.
data class PlanCombinacion(
    val ingresos: List<IngresoComparado>,
    val vehiculosNuevos: Int,
    val clientesNuevos: Int) {
    val nuevos: List<IngresoComparado> get() = ingresos.filter { it.esNuevo }
    val iguales: Int get() = ingresos.count { it.esIgual }
    val distintos: List<IngresoComparado> get() = ingresos.filter { it.esDistinto } }

// Lo que ocurrió al combinar.
data class ResultadoCombinacion(
    val agregados: Int,
    val actualizados: Int,
    val sinCambios: Int,
    val vehiculos: Int,
    val clientes: Int,
    val usuarios: Int)

// Reglas para reconocer que dos registros son el mismo.
object ReglasCombinacion {
    // Placa sin guiones, espacios ni mayúsculas: "klm-482-b" y "KLM 482 B" son la misma.
    fun normalizarPlaca(placa: String): String = placa.filter { it.isLetterOrDigit() }.uppercase()

    // Los últimos 10 dígitos: con o sin lada del país (52 o 521) es el mismo teléfono.
    fun normalizarTelefono(telefono: String): String = telefono.filter { it.isDigit() }.takeLast(10)

    // Texto sin acentos, mayúsculas ni espacios de más.
    fun normalizarTexto(texto: String?): String {
        val sinAcentos = Normalizer.normalize(texto.orEmpty(), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
        return sinAcentos.trim().lowercase().replace(Regex("\\s+"), " ") }

    // Un cliente es el mismo si coinciden el teléfono y el nombre.
    fun mismoCliente(nombreA: String, telefonoA: String, nombreB: String, telefonoB: String): Boolean =
        normalizarTelefono(telefonoA).isNotEmpty() &&
            normalizarTelefono(telefonoA) == normalizarTelefono(telefonoB) &&
            normalizarTexto(nombreA) == normalizarTexto(nombreB)

    // Dos ingresos son el mismo si son del mismo vehículo, con la misma fecha y el mismo motivo.
    fun claveIngreso(vehiculoId: Long, fecha: Long, motivo: String?): Triple<Long, Long, String> =
        Triple(vehiculoId, fecha, normalizarTexto(motivo)) }
