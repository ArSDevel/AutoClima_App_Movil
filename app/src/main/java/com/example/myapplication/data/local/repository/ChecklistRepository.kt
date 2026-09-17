package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.ChecklistDao
import com.example.myapplication.data.local.entity.ChecklistDiagnostico

class ChecklistRepository(private val checklistDao: ChecklistDao) {

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
        comentarios: String?
    ): Long {
        return checklistDao.insertar(
            ChecklistDiagnostico(
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
                comentarios = comentarios
            )
        )
    }
}