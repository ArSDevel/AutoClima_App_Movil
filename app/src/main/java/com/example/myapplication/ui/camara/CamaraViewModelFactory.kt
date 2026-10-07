package com.example.myapplication.ui.camara

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.local.dao.EvidenciaDao

class CamaraViewModelFactory(private val dao: EvidenciaDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CamaraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CamaraViewModel(dao) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}