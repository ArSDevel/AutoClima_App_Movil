package com.example.myapplication.data.local.repository

import com.example.myapplication.data.local.dao.IngresoDao
import com.example.myapplication.data.local.model.EventoHistorial
import kotlinx.coroutines.flow.Flow

// Solo consulta la bitácora. Las operaciones se registran desde sus repositorios.
class HistorialRepository(private val ingresoDao: IngresoDao) {
    // Observa los eventos aplicando búsqueda, tipo de actividad y periodo.
    fun observar(busqueda: String, accion: String?,
        desde: Long?, hasta: Long?, limite: Int): Flow<List<EventoHistorial>> {
        require(limite > 0)
        return ingresoDao.observarHistorial(busqueda.trim(), accion, desde, hasta, limite) } }
