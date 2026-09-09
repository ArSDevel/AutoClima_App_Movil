package com.example.myapplication.data.local.repository

import com.example.myapplication.data.local.dao.IngresoDao
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.IngresoResumen
import kotlinx.coroutines.flow.Flow

class DashboardRepository(
    private val ingresoDao: IngresoDao
) {
    fun observarIngresos(
        textoBusqueda: String,
        estadoFiltro: EstadoIngreso? = null
    ): Flow<List<IngresoResumen>> {
        return ingresoDao.observarIngresos(
            textoBusqueda = textoBusqueda.trim(),
            estadoFiltro = estadoFiltro?.name
        )
    }
}