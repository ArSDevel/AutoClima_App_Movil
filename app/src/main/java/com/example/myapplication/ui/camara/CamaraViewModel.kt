package com.example.myapplication.ui.camara

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.dao.EvidenciaDao
import com.example.myapplication.data.local.entity.Evidencia
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CamaraViewModel(private val evidenciaDao: EvidenciaDao) : ViewModel() {

    // Lee la base de datos en tiempo real y mantiene la lista actualizada
    val listaEvidencias: StateFlow<List<Evidencia>> = evidenciaDao.obtenerTodas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Recibe la ruta de la foto y la guarda en Room
    fun guardarEvidencia(ruta: String) {
        viewModelScope.launch {
            val nuevaEvidencia = Evidencia(rutaImagen = ruta)
            evidenciaDao.insertarEvidencia(nuevaEvidencia)
        }
    }
}