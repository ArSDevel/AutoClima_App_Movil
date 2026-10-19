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
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.myapplication.data.local.entity.Evidencia
import com.example.myapplication.data.local.model.ReglasIngreso
import com.example.myapplication.data.local.repository.DashboardRepository

class RegistroRepository(
    private val baseDeDatos: AppDatabase
) {
    private val clienteDao = baseDeDatos.clienteDao()
    private val vehiculoDao = baseDeDatos.vehiculoDao()
    private val ingresoDao = baseDeDatos.ingresoDao()
    private val tecnicoDao = baseDeDatos.tecnicoDao()
    private val evidenciaDao = baseDeDatos.evidenciaDao()
    private val dashboardRepository = DashboardRepository(baseDeDatos)

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
        condicionInicial: String? = null,
        fotoPath: String? = null,
        fotosPaths: List<String> = emptyList()
    ): Long = withContext(Dispatchers.IO) {
        val rutasNuevas = (fotosPaths + listOfNotNull(fotoPath))
            .map { requireNotNull(validarFoto(it)) }.distinct()
        val fotoValidada = fotoPath?.let { requireNotNull(validarFoto(it)) } ?: rutasNuevas.firstOrNull()
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

        baseDeDatos.withTransaction {
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
                    condicionInicial = condicionLimpia,
                    estado = EstadoIngreso.REGISTRADO.name,
                    fotoPath = fotoValidada
                )
            )

            ingresoDao.insertarEvento(
                EventoIngreso(ingresoId = ingresoId, fecha = System.currentTimeMillis(),
                    tecnicoId = tecnicoId, accion = "CREADO", estadoAnterior = "",
                    estadoNuevo = EstadoIngreso.REGISTRADO.name, motivo = "Ingreso registrado."
                )
            )
            if (rutasNuevas.isNotEmpty()) {
                val ingresoGuardado = requireNotNull(ingresoDao.obtener(ingresoId))
                rutasNuevas.forEach { ruta ->
                    registrarFotoPrincipal(ingresoGuardado, ruta, tecnicoId)
                }
            }
            ingresoId
        }
    }


    // Consulta las fotografías del expediente sin modificar el formulario.
    fun observarFotos(ingresoId: Long): kotlinx.coroutines.flow.Flow<List<Evidencia>> =
        evidenciaDao.observarPorIngreso(ingresoId)

    suspend fun obtenerFotos(ingresoId: Long): List<Evidencia> =
        evidenciaDao.obtenerPorIngreso(ingresoId)

    suspend fun editarIngreso(
        ingresoId: Long,
        formulario: FormularioIngreso,
        tecnicoId: Long,
        nuevaFotoPath: String? = null,
        nuevasFotosPaths: List<String> = emptyList()
    ): Unit = withContext(Dispatchers.IO) {
        val nuevas = nuevasFotosPaths.map { requireNotNull(validarFoto(it)) }.distinct()
        val seleccionada = nuevaFotoPath?.let { requireNotNull(validarFoto(it)) }
        baseDeDatos.withTransaction {
            dashboardRepository.editar(ingresoId, formulario, tecnicoId)
            val ingreso = requireNotNull(ingresoDao.obtener(ingresoId)) { "El ingreso ya no existe." }
            if (nuevas.isNotEmpty() || (seleccionada != null && seleccionada != ingreso.fotoPath)) {
                require(ReglasIngreso.edicionCompleta(EstadoIngreso.desdeValor(ingreso.estado))) {
                    "En esta etapa no puedes modificar las fotografías."
                }
            }
            nuevas.forEach { registrarFotoPrincipal(ingreso, it, tecnicoId) }
            val principal = seleccionada ?: ingreso.fotoPath ?: nuevas.firstOrNull()
            if (principal != ingreso.fotoPath && principal != null) {
                val evidencia = evidenciaDao.buscarPorRuta(principal)
                require(evidencia?.ingresoId == ingresoId) { "La fotografía seleccionada ya no está disponible en este ingreso." }
                check(ingresoDao.actualizar(ingreso.copy(fotoPath = principal)) == 1)
                ingresoDao.insertarEvento(
                    EventoIngreso(
                        ingresoId = ingresoId, fecha = System.currentTimeMillis(), tecnicoId = tecnicoId,
                        accion = "FOTO_PRINCIPAL", estadoAnterior = ingreso.estado, estadoNuevo = ingreso.estado,
                        motivo = "Se actualizó la fotografía principal desde Registro."
                    )
                )
            }
        }
    }

    // Se ejecuta en Dispatchers.IO para no consultar el disco en el hilo de UI.
    private fun validarFoto(ruta: String?): String? {
        if (ruta == null) return null
        require(ruta.isNotBlank()) { "La ubicación de la fotografía no es válida." }
        val archivo = File(ruta).canonicalFile
        require(archivo.isFile && archivo.canRead() && archivo.length() > 0) {
            "No se encontró la fotografía. Tómala nuevamente antes de guardar."
        }
        return archivo.absolutePath
    }

    // Conserva la foto principal también en la galería del expediente.
    // Las fotografías anteriores no se borran al cambiar la portada.
    // Se llama únicamente dentro de la transacción del registro o la edición.
    private suspend fun registrarFotoPrincipal(
        ingreso: Ingreso,
        ruta: String,
        tecnicoId: Long
    ) {
        val existente = evidenciaDao.buscarPorRuta(ruta)
        val fecha = System.currentTimeMillis()

        if (existente == null) {
            evidenciaDao.insertarEvidencia(
                Evidencia(
                    rutaImagen = ruta,
                    ingresoId = ingreso.ingresoId,
                    tecnicoId = tecnicoId,
                    fecha = fecha,
                    descripcion = "Fotografía del vehículo adjuntada desde Registro."
                )
            )
        } else {
            require(existente.ingresoId == ingreso.ingresoId) {
                "La fotografía ya está registrada y no pertenece a este ingreso."
            }
            return
        }

        ingresoDao.insertarEvento(
            EventoIngreso(
                ingresoId = ingreso.ingresoId,
                fecha = fecha,
                tecnicoId = tecnicoId,
                accion = "EVIDENCIA",
                estadoAnterior = ingreso.estado,
                estadoNuevo = ingreso.estado,
                motivo = "Se agregó una fotografía del vehículo desde Registro."
            )
        )
    }
}