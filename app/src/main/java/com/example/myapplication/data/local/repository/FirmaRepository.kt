package com.example.myapplication.data.local.repository

import androidx.room.withTransaction
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.entity.EventoIngreso
import com.example.myapplication.data.local.entity.Firma
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.ReglasIngreso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

// Guarda la firma del cliente y pasa el ingreso a listo para entrega.
class FirmaRepository(
    private val baseDeDatos: AppDatabase,
    private val directorioArchivos: File) {
    private val firmaDao = baseDeDatos.firmaDao()
    private val ingresoDao = baseDeDatos.ingresoDao()
    private val tecnicoDao = baseDeDatos.tecnicoDao()

    // Observa la firma vigente del ingreso.
    fun observarFirma(ingresoId: Long): Flow<Firma?> {
        require(ingresoId > 0) { "El identificador del ingreso no es válido." }
        return firmaDao.observarPorIngreso(ingresoId) }

    // Firma vigente del ingreso o null si todavía no se firma.
    suspend fun obtener(ingresoId: Long): Firma? {
        require(ingresoId > 0) { "El identificador del ingreso no es válido." }
        return firmaDao.obtenerPorIngreso(ingresoId) }

    // Escribe la imagen y, en una sola transacción, guarda la firma y cambia el estado.
    // Si algo falla se borra el archivo nuevo y el ingreso queda como estaba.
    suspend fun confirmar(ingresoId: Long, imagenPng: ByteArray, tecnicoId: Long) = withContext(Dispatchers.IO) {
        require(ingresoId > 0) { "El identificador del ingreso no es válido." }
        require(tecnicoId > 0) { "Inicia sesión nuevamente para continuar." }
        require(imagenPng.isNotEmpty()) { "Firma en el recuadro antes de confirmar." }

        val carpeta = File(directorioArchivos, "firmas").apply { mkdirs() }
        val archivo = File(carpeta, "firma_${ingresoId}_${System.currentTimeMillis()}.png")
        archivo.writeBytes(imagenPng)

        var rutaAnterior: String? = null
        try {
            baseDeDatos.withTransaction {
                requireNotNull(tecnicoDao.obtener(tecnicoId)) {
                    "El técnico ya no existe. Inicia sesión nuevamente." }
                val ingreso = requireNotNull(ingresoDao.obtener(ingresoId)) { "El ingreso ya no existe." }
                val estadoActual = EstadoIngreso.desdeValor(ingreso.estado)
                require(ReglasIngreso.firmar(estadoActual)) {
                    "Solo se puede firmar un ingreso que está listo para firma." }

                rutaAnterior = firmaDao.obtenerPorIngreso(ingresoId)?.rutaImagen
                val ahora = System.currentTimeMillis()
                firmaDao.guardar(Firma(ingresoId = ingresoId, rutaImagen = archivo.absolutePath,
                    tecnicoId = tecnicoId, fecha = ahora))
                check(ingresoDao.actualizar(ingreso.copy(estado = EstadoIngreso.LISTO_PARA_ENTREGA.name)) == 1) {
                    "No fue posible actualizar el ingreso." }
                ingresoDao.insertarEvento(
                    EventoIngreso(ingresoId = ingresoId, fecha = ahora, tecnicoId = tecnicoId,
                        accion = "ESTADO", estadoAnterior = ingreso.estado,
                        estadoNuevo = EstadoIngreso.LISTO_PARA_ENTREGA.name,
                        motivo = "Firma del cliente registrada.")) }
        } catch (error: Throwable) {
            archivo.delete()
            throw error }

        // La firma anterior ya no se usa; borrarla no revierte lo guardado.
        rutaAnterior?.let { File(it).delete() } } }
