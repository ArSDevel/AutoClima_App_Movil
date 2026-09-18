package com.example.myapplication.data.repository

import androidx.room.withTransaction
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.entity.ChecklistDiagnostico
import com.example.myapplication.data.local.entity.EventoIngreso
import com.example.myapplication.data.local.model.EstadoIngreso

class ChecklistRepository(
    private val baseDeDatos: AppDatabase
) {
    private val checklistDao = baseDeDatos.checklistDao()
    private val ingresoDao = baseDeDatos.ingresoDao()
    private val tecnicoDao = baseDeDatos.tecnicoDao()

    // Recupera las respuestas guardadas.
    // Devuelve null cuando el ingreso existe, pero aún no tiene checklist.
    suspend fun obtenerPorIngreso(
        ingresoId: Long
    ): ChecklistDiagnostico? {
        require(ingresoId > 0) {
            "El identificador del ingreso no es válido."
        }

        return baseDeDatos.withTransaction {
            requireNotNull(ingresoDao.obtener(ingresoId)) {
                "El ingreso ya no existe."
            }

            checklistDao.obtenerPorIngreso(ingresoId)
        }
    }

    // Guarda o actualiza el checklist y registra el evento.
    // Toda la operación se revierte si alguna parte falla.
    // El técnico debe provenir de la sesión iniciada.
    suspend fun guardarChecklist(
        ingresoId: Long,
        faltaGasRefrigerante: Boolean,
        fugaVisibleMangueras: Boolean,
        fugasDetectadasLuzUV: Boolean,
        compresorEmbragaAlEncender: Boolean,
        compresorDanadoOAmarrado: Boolean,
        bandaCompresorDesgastada: Boolean,
        funcionanAbanicosRadiador: Boolean,
        filtroCabinaSucioUObstruido: Boolean,
        condensadorObstruido: Boolean,
        comentarios: String?,
        tecnicoId: Long
    ): Long {
        require(ingresoId > 0) {
            "El identificador del ingreso no es válido."
        }

        require(tecnicoId > 0) {
            "Inicia sesión nuevamente para continuar."
        }

        return baseDeDatos.withTransaction {
            requireNotNull(tecnicoDao.obtener(tecnicoId)) {
                "El técnico ya no existe. Inicia sesión nuevamente."
            }

            val ingreso = requireNotNull(ingresoDao.obtener(ingresoId)) {
                "El ingreso ya no existe."
            }

            // Consulta el estado real al guardar.
            // Evita modificar un checklist si el ingreso cambió
            // de etapa después de abrir la pantalla.
            val estado = EstadoIngreso.desdeValor(ingreso.estado)

            require(puedeEditar(estado)) {
                "Solo puedes guardar el checklist cuando el ingreso " +
                        "está en revisión o en reparación."
            }

            val existente = checklistDao.obtenerPorIngreso(ingresoId)
            val ahora = System.currentTimeMillis()

            // Conserva el identificador del checklist existente.
            // Para comparar respuestas, conserva inicialmente su fecha.
            val candidato = ChecklistDiagnostico(
                checklistId = existente?.checklistId ?: 0L,
                ingresoId = ingresoId,
                faltaGasRefrigerante = faltaGasRefrigerante,
                fugaVisibleMangueras = fugaVisibleMangueras,
                fugasDetectadasLuzUV = fugasDetectadasLuzUV,
                compresorEmbragaAlEncender = compresorEmbragaAlEncender,
                compresorDanadoOAmarrado = compresorDanadoOAmarrado,
                bandaCompresorDesgastada = bandaCompresorDesgastada,
                funcionanAbanicosRadiador = funcionanAbanicosRadiador,
                filtroCabinaSucioUObstruido = filtroCabinaSucioUObstruido,
                condensadorObstruido = condensadorObstruido,
                comentarios = comentarios?.trim()?.ifEmpty { null },
                fecha = existente?.fecha ?: ahora
            )

            // Guardar las mismas respuestas no modifica la fecha
            // ni agrega eventos repetidos al historial.
            if (existente != null && candidato == existente) {
                return@withTransaction existente.checklistId
            }

            // La fecha representa el último guardado con cambios.
            val checklist = candidato.copy(fecha = ahora)

            val checklistId = if (existente == null) {
                checklistDao.insertar(checklist)
            } else {
                check(checklistDao.actualizar(checklist) == 1) {
                    "No fue posible actualizar el checklist."
                }

                existente.checklistId
            }

            // El checklist no cambia el estado del ingreso.
            // Su guardado sí cuenta como actividad posterior a la creación.
            ingresoDao.insertarEvento(
                EventoIngreso(
                    ingresoId = ingresoId,
                    fecha = ahora,
                    tecnicoId = tecnicoId,
                    accion = "CHECKLIST",
                    estadoAnterior = ingreso.estado,
                    estadoNuevo = ingreso.estado,
                    motivo = if (existente == null) {
                        "Se guardó el checklist de revisión."
                    } else {
                        "Se actualizaron las respuestas o comentarios del checklist."
                    }
                )
            )

            checklistId
        }
    }

    companion object {
        // La pantalla puede consultar esta condición para habilitar
        // la edición. El repositorio también la valida al guardar.
        fun puedeEditar(estado: EstadoIngreso): Boolean {
            return estado == EstadoIngreso.EN_REVISION ||
                    estado == EstadoIngreso.EN_REPARACION
        }
    }
}