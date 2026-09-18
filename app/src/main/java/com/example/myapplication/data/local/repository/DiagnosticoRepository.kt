package com.example.myapplication.data.local.repository

import androidx.room.withTransaction
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.entity.ConceptoCotizacion
import com.example.myapplication.data.local.entity.Diagnostico
import com.example.myapplication.data.local.entity.EventoIngreso
import com.example.myapplication.data.local.model.ConceptoCotizacionFormulario
import com.example.myapplication.data.local.model.DiagnosticoConConceptos
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.FormularioDiagnostico
import java.math.BigDecimal

class DiagnosticoRepository(
    private val baseDeDatos: AppDatabase
) {
    private val diagnosticoDao = baseDeDatos.diagnosticoDao()
    private val ingresoDao = baseDeDatos.ingresoDao()
    private val checklistDao = baseDeDatos.checklistDao()
    private val tecnicoDao = baseDeDatos.tecnicoDao()

    // Recupera las notas y la cotización en una misma transacción.
    // Devuelve null si el ingreso todavía no tiene diagnóstico.
    suspend fun obtenerPorIngreso(
        ingresoId: Long
    ): DiagnosticoConConceptos? {
        require(ingresoId > 0) {
            "El identificador del ingreso no es válido."
        }

        return baseDeDatos.withTransaction {
            requireNotNull(ingresoDao.obtener(ingresoId)) {
                "El ingreso ya no existe."
            }

            val diagnostico = diagnosticoDao.obtenerPorIngreso(ingresoId)
                ?: return@withTransaction null

            DiagnosticoConConceptos(
                diagnostico = diagnostico,
                conceptos = diagnosticoDao.obtenerConceptos(
                    diagnostico.diagnosticoId
                )
            )
        }
    }

    // Guarda las notas, la cotización y el evento del historial.
    // Si alguna operación falla, se revierten todos los cambios.
    suspend fun guardar(
        ingresoId: Long,
        formulario: FormularioDiagnostico,
        tecnicoId: Long
    ): Long {
        require(ingresoId > 0) {
            "El identificador del ingreso no es válido."
        }

        require(tecnicoId > 0) {
            "Inicia sesión nuevamente para continuar."
        }

        val errores = formulario.errores()

        require(errores.isEmpty()) {
            errores.values.firstOrNull()
                ?: "Revisa los datos de la cotización."
        }

        val normalizado = formulario.normalizado()

        return baseDeDatos.withTransaction {
            requireNotNull(tecnicoDao.obtener(tecnicoId)) {
                "El técnico ya no existe. Inicia sesión nuevamente."
            }

            val ingreso = requireNotNull(ingresoDao.obtener(ingresoId)) {
                "El ingreso ya no existe."
            }

            // Comprueba el estado actual de la base de datos,
            // aunque la pantalla se haya abierto en otra etapa.
            val estadoActual = EstadoIngreso.desdeValor(ingreso.estado)

            require(puedeEditar(estadoActual)) {
                "Solo puedes guardar el diagnóstico cuando el ingreso " +
                        "está en revisión o en reparación."
            }

            requireNotNull(checklistDao.obtenerPorIngreso(ingresoId)) {
                "Guarda primero el checklist para elaborar el diagnóstico."
            }

            val existente = diagnosticoDao.obtenerPorIngreso(ingresoId)

            val conceptosExistentes = if (existente != null) {
                diagnosticoDao.obtenerConceptos(existente.diagnosticoId)
            } else {
                emptyList()
            }

            // Convierte los datos guardados al mismo formato
            // del formulario para comparar su contenido.
            val formularioAnterior = FormularioDiagnostico(
                observaciones = existente?.observaciones.orEmpty(),
                conceptos = conceptosExistentes.map { concepto ->
                    ConceptoCotizacionFormulario(
                        descripcion = concepto.descripcion,
                        importe = BigDecimal.valueOf(
                            concepto.importeCentavos,
                            2
                        ).toPlainString()
                    )
                }
            ).normalizado()

            // No modifica fechas ni genera eventos por guardar
            // nuevamente el mismo contenido.
            if (existente != null && normalizado == formularioAnterior) {
                return@withTransaction existente.diagnosticoId
            }

            val ahora = System.currentTimeMillis()

            val diagnosticoId = if (existente == null) {
                diagnosticoDao.insertar(
                    Diagnostico(
                        ingresoId = ingresoId,
                        observaciones = normalizado.observaciones,
                        tecnicoId = tecnicoId,
                        fechaCreacion = ahora,
                        fechaActualizacion = ahora
                    )
                )
            } else {
                val actualizado = existente.copy(
                    observaciones = normalizado.observaciones,
                    tecnicoId = tecnicoId,
                    fechaActualizacion = ahora
                )

                check(diagnosticoDao.actualizar(actualizado) == 1) {
                    "No fue posible actualizar el diagnóstico."
                }

                existente.diagnosticoId
            }

            // Solo reemplaza las filas cuando cambia la cotización.
            // Editar únicamente las notas conserva los conceptos existentes.
            val cambioCotizacion =
                normalizado.conceptos != formularioAnterior.conceptos

            if (cambioCotizacion) {
                diagnosticoDao.eliminarConceptos(diagnosticoId)

                val nuevosConceptos = normalizado.conceptos.mapIndexed {
                        indice, concepto ->
                    ConceptoCotizacion(
                        diagnosticoId = diagnosticoId,
                        descripcion = concepto.descripcion,
                        importeCentavos = requireNotNull(
                            concepto.importeEnCentavos()
                        ) {
                            "El importe de un concepto no es válido."
                        },
                        orden = indice
                    )
                }

                if (nuevosConceptos.isNotEmpty()) {
                    diagnosticoDao.insertarConceptos(nuevosConceptos)
                }
            }

            // Registra la operación sin cambiar el estado del ingreso.
            ingresoDao.insertarEvento(
                EventoIngreso(
                    ingresoId = ingresoId,
                    fecha = ahora,
                    tecnicoId = tecnicoId,
                    accion = "DIAGNOSTICO",
                    estadoAnterior = ingreso.estado,
                    estadoNuevo = ingreso.estado,
                    motivo = if (existente == null) {
                        if (normalizado.tieneCotizacion) {
                            "Se guardó el diagnóstico con su cotización."
                        } else {
                            "Se guardó el diagnóstico con cotización pendiente."
                        }
                    } else {
                        "Se actualizaron las notas del diagnóstico " +
                                "o los conceptos de cotización."
                    }
                )
            )

            diagnosticoId
        }
    }

    companion object {
        // La interfaz consulta esta condición para habilitar los campos.
        // El repositorio vuelve a comprobarla al guardar.
        fun puedeEditar(estado: EstadoIngreso): Boolean {
            return estado == EstadoIngreso.EN_REVISION ||
                    estado == EstadoIngreso.EN_REPARACION
        }
    }
}