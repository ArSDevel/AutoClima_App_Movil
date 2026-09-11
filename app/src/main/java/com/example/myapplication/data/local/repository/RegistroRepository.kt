package com.example.myapplication.data.repository

import androidx.room.withTransaction
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.entity.Cliente
import com.example.myapplication.data.local.entity.EventoIngreso
import com.example.myapplication.data.local.entity.Ingreso
import com.example.myapplication.data.local.entity.Vehiculo
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.data.local.model.FormularioIngreso
import java.util.Locale

class RegistroRepository(
    private val baseDeDatos: AppDatabase
) {
    private val clienteDao = baseDeDatos.clienteDao()
    private val vehiculoDao = baseDeDatos.vehiculoDao()
    private val ingresoDao = baseDeDatos.ingresoDao()
    private val tecnicoDao = baseDeDatos.tecnicoDao()

    // Registra una visita y su evento inicial.
    // Las fichas existentes solo se reutilizan si sus datos
    // coinciden con los capturados.
    suspend fun registrarIngreso(
        placa: String,
        nombreCliente: String,
        modelo: String,
        telefonoCliente: String,
        color: String?,
        emailCliente: String?,
        numeroSerie: String?,
        fechaEntrada: Long,
        tecnicoId: Long,
        motivoIngreso: String? = null,
        condicionInicial: String? = null
    ): Long {
        val placaLimpia = placa.trim().uppercase(Locale.ROOT)
        val nombreLimpio = nombreCliente.trim()
        val modeloLimpio = modelo.trim()
        val telefonoLimpio = telefonoCliente.trim()

        val colorLimpio = color?.trim()?.ifEmpty { null }
        val emailLimpio = emailCliente?.trim()?.ifEmpty { null }
        val serieLimpia = numeroSerie?.trim()?.ifEmpty { null }
        val motivoLimpio = motivoIngreso?.trim()?.ifEmpty { null }
        val condicionLimpia = condicionInicial?.trim()?.ifEmpty { null }

        // La validación también se ejecuta en la capa de datos.
        val formulario = FormularioIngreso(
            placa = placaLimpia, modelo = modeloLimpio, color = colorLimpio.orEmpty(),
            numeroSerie = serieLimpia.orEmpty(), nombre = nombreLimpio, telefono = telefonoLimpio,
            email = emailLimpio.orEmpty(), fecha = FormularioIngreso.formatearFecha(fechaEntrada),
            motivo = motivoLimpio.orEmpty(), condicion = condicionLimpia.orEmpty()
        )

        val errores = formulario.errores()

        require(errores.isEmpty()) {
            errores.values.firstOrNull() ?: "Revisa los datos del ingreso."
        }

        return baseDeDatos.withTransaction {
            require(tecnicoId > 0) { "Inicia sesión nuevamente para continuar." }

            requireNotNull(tecnicoDao.obtener(tecnicoId)) { "El técnico ya no existe. Inicia sesión nuevamente." }

            val clienteExistente = clienteDao.buscarPorTelefono(telefonoLimpio)
            val vehiculoExistente = vehiculoDao.buscarPorPlaca(placaLimpia)

            // Evita reutilizar un cliente ignorando diferencias
            // en su nombre o correo.
            if (clienteExistente != null) {
                require(
                    clienteExistente.nombre == nombreLimpio &&
                            clienteExistente.email.orEmpty() == emailLimpio.orEmpty()) {
                    "El teléfono pertenece a un cliente con otros datos. Revisa su ficha antes de registrar otra visita."
                }
            }

            // Evita asociar silenciosamente la visita a otro propietario
            // o ignorar diferencias en los datos del vehículo.
            if (vehiculoExistente != null) {
                require(
                    clienteExistente != null &&
                            vehiculoExistente.clienteId == clienteExistente.clienteId) {
                    "La placa ya está registrada con otro cliente. Revisa la ficha existente." }

                require(
                    vehiculoExistente.modelo == modeloLimpio &&
                            vehiculoExistente.color.orEmpty() == colorLimpio.orEmpty() &&
                            vehiculoExistente.numeroSerie.orEmpty() == serieLimpia.orEmpty()) {
                    "La placa ya existe con otros datos del vehículo. Corrige su ficha antes de registrar otra visita."
                }
            }

            val clienteId = clienteExistente?.clienteId
                ?: clienteDao.insertar(Cliente(nombre = nombreLimpio, telefono = telefonoLimpio,
                    email = emailLimpio))

            val vehiculoId = vehiculoExistente?.vehiculoId
                ?: vehiculoDao.insertar(
                    Vehiculo(clienteId = clienteId, placa = placaLimpia,
                        modelo = modeloLimpio, color = colorLimpio,
                        numeroSerie = serieLimpia, kilometraje = null,
                        observaciones = null)
                )

            val ingresoId = ingresoDao.insertar(
                Ingreso(vehiculoId = vehiculoId, tecnicoId = tecnicoId,
                    fecha = fechaEntrada, motivoIngreso = motivoLimpio,
                    condicionInicial = condicionLimpia, estado = EstadoIngreso.REGISTRADO.name
                )
            )

            ingresoDao.insertarEvento(
                EventoIngreso(ingresoId = ingresoId, fecha = System.currentTimeMillis(),
                    tecnicoId = tecnicoId, accion = "CREADO", estadoAnterior = "",
                    estadoNuevo = EstadoIngreso.REGISTRADO.name, motivo = "Ingreso registrado."
                )
            )
            ingresoId
        }
    }
}