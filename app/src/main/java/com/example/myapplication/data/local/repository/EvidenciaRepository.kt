package com.example.myapplication.data.local.repository

import androidx.room.withTransaction
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.entity.EventoIngreso
import com.example.myapplication.data.local.entity.Evidencia
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.IngresoResumen
import com.example.myapplication.data.local.model.ReglasIngreso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import android.util.Log
import kotlinx.coroutines.NonCancellable

// Guarda, consulta y elimina las fotografías de un ingreso.
class EvidenciaRepository(
    private val baseDeDatos: AppDatabase,
    private val directorioArchivos: File) {
    private val evidenciaDao = baseDeDatos.evidenciaDao()
    private val ingresoDao = baseDeDatos.ingresoDao()
    private val tecnicoDao = baseDeDatos.tecnicoDao()

    // Consulta los datos del ingreso para mostrar su placa
    // y comprobar el estado actual en la pantalla.
    fun observarIngreso(
        ingresoId: Long
    ): Flow<IngresoResumen?> {
        require(ingresoId > 0) {
            "El identificador del ingreso no es válido." }

        return ingresoDao.observarIngresoPorId(ingresoId) }

    // Cada expediente muestra únicamente sus fotografías.
    fun observarEvidencias(
        ingresoId: Long
    ): Flow<List<Evidencia>> {
        require(ingresoId > 0) {
            "El identificador del ingreso no es válido." }

        return evidenciaDao.observarPorIngreso(ingresoId) }

    // Registra una fotografía que CameraX ya escribió en el dispositivo.
    // Devuelve el identificador de la evidencia guardada.
    suspend fun guardar(
        ingresoId: Long,
        tecnicoId: Long,
        archivo: File,
        descripcion: String = ""
    ): Long = withContext(Dispatchers.IO) {
        require(ingresoId > 0) {
            "Selecciona un ingreso antes de guardar la fotografía." }

        require(tecnicoId > 0) {
            "Inicia sesión nuevamente para continuar." }

        val descripcionLimpia = descripcion.trim()

        require(descripcionLimpia.length <= 300) {
            "La descripción debe tener como máximo 300 caracteres." }

        // La cámara actual guarda directamente en context.filesDir.
        // Comprobamos que se trate de un archivo de ese directorio.
        val archivoReal = archivo.canonicalFile
        val directorioReal = directorioArchivos.canonicalFile

        require(archivoReal.parentFile == directorioReal) {
            "La ubicación de la fotografía no es válida." }

        require(
            archivoReal.isFile &&
                    archivoReal.canRead() &&
                    archivoReal.length() > 0) {
            "No se encontró una fotografía válida. Tómala nuevamente." }

        baseDeDatos.withTransaction {
            requireNotNull(tecnicoDao.obtener(tecnicoId)) {
                "El técnico ya no existe. Inicia sesión nuevamente." }

            val ingreso = requireNotNull(
                ingresoDao.obtener(ingresoId)) {
                "El ingreso ya no existe." }

            // Una repetición del mismo guardado no debe
            // crear otra evidencia ni otro evento.
            val existente = evidenciaDao.buscarPorRuta(
                archivoReal.absolutePath)

            if (existente != null) {
                require(
                    existente.ingresoId == ingresoId &&
                            existente.tecnicoId == tecnicoId) {
                    "Esta fotografía ya está registrada. " +
                            "No puede asignarse automáticamente a otro expediente." }

                return@withTransaction existente.id.toLong() }

            val estadoActual = EstadoIngreso.desdeValor(
                ingreso.estado)

            require(ReglasIngreso.edicionCompleta(estadoActual)) {
                "Solo puedes agregar fotografías cuando el ingreso " +
                        "está registrado, en revisión o en reparación." }

            val fechaRegistro = System.currentTimeMillis()

            val evidenciaId = evidenciaDao.insertarEvidencia(
                Evidencia(
                    rutaImagen = archivoReal.absolutePath,
                    ingresoId = ingresoId,
                    tecnicoId = tecnicoId,
                    fecha = fechaRegistro,
                    descripcion = descripcionLimpia))

            if (ingreso.fotoPath == null) {
                check(ingresoDao.actualizar(ingreso.copy(fotoPath = archivoReal.absolutePath)) == 1) {
                    "No fue posible establecer la fotografía principal." } }

            check(evidenciaId > 0) {
                "No fue posible registrar la fotografía." }

            // La fotografía no cambia el estado del ingreso.
            // El evento documenta quién la agregó y cuándo.
            ingresoDao.insertarEvento(
                EventoIngreso(
                    ingresoId = ingresoId,
                    fecha = fechaRegistro,
                    tecnicoId = tecnicoId,
                    accion = "EVIDENCIA",
                    estadoAnterior = ingreso.estado,
                    estadoNuevo = ingreso.estado,
                    motivo = if (descripcionLimpia.isBlank()) {
                        "Se agregó una evidencia fotográfica."
                    } else {
                        "Se agregó una evidencia fotográfica: " +
                                descripcionLimpia }))

            evidenciaId } }

    // La selección se guarda inmediatamente y actualiza el Dashboard.
    suspend fun seleccionarPrincipal(ingresoId: Long, evidenciaId: Int, tecnicoId: Long) {
        withContext(Dispatchers.IO) {
            baseDeDatos.withTransaction {
                val ingreso = validarOperacion(ingresoId, tecnicoId)
                val evidencia = requireNotNull(evidenciaDao.obtener(evidenciaId)) {
                    "La fotografía ya no existe." }
                require(evidencia.ingresoId == ingresoId) { "La fotografía no pertenece a este ingreso." }
                val archivo = File(evidencia.rutaImagen)
                require(archivo.isFile && archivo.canRead() && archivo.length() > 0) {
                    "No se encontró el archivo de la fotografía." }
                if (ingreso.fotoPath == evidencia.rutaImagen) return@withTransaction
                check(ingresoDao.actualizar(ingreso.copy(fotoPath = evidencia.rutaImagen)) == 1)
                ingresoDao.insertarEvento(
                    EventoIngreso(
                        ingresoId = ingresoId, fecha = System.currentTimeMillis(),
                        tecnicoId = tecnicoId, accion = "FOTO_PRINCIPAL",
                        estadoAnterior = ingreso.estado, estadoNuevo = ingreso.estado,
                        motivo = "Se seleccionó la evidencia n.º $evidenciaId como fotografía principal.")) } } }

    // El conteo, borrado, cambio de principal y evento son atómicos.
    // Dos intentos de borrado no pueden dejar el ingreso sin fotografías.
    suspend fun eliminar(ingresoId: Long, evidenciaId: Int, tecnicoId: Long) {
        withContext(Dispatchers.IO) {
            val ruta = baseDeDatos.withTransaction {
                val ingreso = validarOperacion(ingresoId, tecnicoId)
                val evidencia = requireNotNull(evidenciaDao.obtener(evidenciaId)) {
                    "La fotografía ya no existe." }
                require(evidencia.ingresoId == ingresoId) { "La fotografía no pertenece a este ingreso." }
                require(evidenciaDao.contarPorIngreso(ingresoId) > 1) {
                    "Debes conservar al menos una fotografía. Agrega otra antes de eliminar esta." }
                check(evidenciaDao.eliminar(evidenciaId, ingresoId) == 1) {
                    "No fue posible eliminar la fotografía." }
                if (ingreso.fotoPath == evidencia.rutaImagen) {
                    // La fila ya se borró, así que la primera que queda pasa a ser la principal; el conteo previo
                    // garantiza que existe.
                    val sustituta = evidenciaDao.obtenerPorIngreso(ingresoId).first()
                    check(ingresoDao.actualizar(ingreso.copy(fotoPath = sustituta.rutaImagen)) == 1) }
                ingresoDao.insertarEvento(
                    EventoIngreso(
                        ingresoId = ingresoId, fecha = System.currentTimeMillis(),
                        tecnicoId = tecnicoId, accion = "EVIDENCIA_ELIMINADA",
                        estadoAnterior = ingreso.estado, estadoNuevo = ingreso.estado,
                        motivo = "Se eliminó la evidencia fotográfica n.º $evidenciaId."))
                evidencia.rutaImagen }
            // La eliminación del archivo ocurre después de confirmar Room.
            // Si el sistema no permite borrarlo, no se revierte un guardado confirmado.
            withContext(NonCancellable) {
                try {
                    val archivo = File(ruta).canonicalFile
                    if (archivo.parentFile == directorioArchivos.canonicalFile &&
                        evidenciaDao.contarReferenciasRuta(ruta) == 0 && archivo.exists()) {
                        if (!archivo.delete()) Log.w("Evidencias", "Archivo pendiente de limpieza") }
                } catch (_: Exception) {
                    Log.w("Evidencias", "No fue posible limpiar el archivo local") } } } }

    // Comprueba el técnico y que el estado del ingreso permita cambiar fotografías.
    // Devuelve el ingreso ya cargado para que la operación no lo consulte otra vez.
    private suspend fun validarOperacion(ingresoId: Long, tecnicoId: Long): com.example.myapplication.data.local.entity.Ingreso {
        require(ingresoId > 0 && tecnicoId > 0) { "La sesión o el ingreso no son válidos." }
        requireNotNull(tecnicoDao.obtener(tecnicoId)) { "Inicia sesión nuevamente para continuar." }
        val ingreso = requireNotNull(ingresoDao.obtener(ingresoId)) { "El ingreso ya no existe." }
        require(ReglasIngreso.edicionCompleta(EstadoIngreso.desdeValor(ingreso.estado))) {
            "En esta etapa las fotografías son de consulta." }
        return ingreso } }