package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.ClienteDao
import com.example.myapplication.data.local.dao.IngresoDao
import com.example.myapplication.data.local.dao.VehiculoDao
import com.example.myapplication.data.local.entity.Cliente
import com.example.myapplication.data.local.entity.Ingreso
import com.example.myapplication.data.local.entity.Vehiculo

class RegistroRepository(
    private val clienteDao: ClienteDao,
    private val vehiculoDao: VehiculoDao,
    private val ingresoDao: IngresoDao
) {
    suspend fun registrarIngreso(
        placa: String,
        nombreCliente: String,
        modelo: String,
        telefonoCliente: String,
        color: String?,
        emailCliente: String?,
        numeroSerie: String?,
        fechaEntrada: Long,
        tecnicoId: Long
    ): Long {
        val clienteId = clienteDao.buscarPorTelefono(telefonoCliente)?.clienteId
            ?: clienteDao.insertar(Cliente(nombre = nombreCliente, telefono = telefonoCliente, email = emailCliente))

        val vehiculoId = vehiculoDao.buscarPorPlaca(placa)?.vehiculoId
            ?: vehiculoDao.insertar(
                Vehiculo(
                    clienteId = clienteId,
                    placa = placa,
                    modelo = modelo,
                    color = color,
                    numeroSerie = numeroSerie,
                    kilometraje = null,
                    observaciones = null
                )
            )

        return ingresoDao.insertar(
            Ingreso(
                vehiculoId = vehiculoId,
                tecnicoId = tecnicoId,
                fecha = fechaEntrada
                // motivoIngreso y condicionInicial quedan null, se llenarían desde otra pantalla
            )
        )
    }
}