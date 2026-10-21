package com.example.myapplication.ui.diagnostico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.repository.DashboardRepository
import com.example.myapplication.data.local.repository.FirmaRepository
import com.example.myapplication.data.reporte.FirmaReporte
import com.example.myapplication.data.reporte.TipoReporte
import com.example.myapplication.data.reporte.DatosReporte
import com.example.myapplication.data.reporte.EnvioReporte
import com.example.myapplication.data.reporte.FormatoReporte
import com.example.myapplication.data.reporte.GeneradorReportePdf
import com.example.myapplication.data.reporte.ResultadoEnvio
import com.example.myapplication.data.reporte.SolicitudEnvio
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// Etapas del envío del reporte al cliente.
enum class FaseEnvio { LIBRE, PREPARANDO, LISTO, ERROR }

// Estado de la hoja para enviar el reporte.
data class EnvioReporteUiState(
    val tipo: TipoReporte = TipoReporte.COMPLETO,
    val incluirReporte: Boolean = true,
    val incluirFotos: Boolean = true,
    val fase: FaseEnvio = FaseEnvio.LIBRE,
    val error: String? = null
) {
    // Hace falta elegir al menos un contenido y no estar ocupado.
    val puedeEnviar: Boolean
        get() = (incluirReporte || incluirFotos) && fase != FaseEnvio.PREPARANDO }

// Prepara el PDF y las fotos, los entrega al servicio de envío y anota el resultado en el historial.
class EnvioReporteViewModel(
    private val ingresoId: Long,
    private val envio: EnvioReporte,
    private val generador: GeneradorReportePdf,
    private val dashboardRepository: DashboardRepository,
    private val firmaRepository: FirmaRepository,
    private val carpetaTemporal: File
) : ViewModel() {
    private val _uiState = MutableStateFlow(EnvioReporteUiState())
    val uiState: StateFlow<EnvioReporteUiState> = _uiState.asStateFlow()

    fun cambiarTipo(tipo: TipoReporte) {
        if (_uiState.value.fase == FaseEnvio.PREPARANDO) return
        _uiState.update { it.copy(tipo = tipo, fase = FaseEnvio.LIBRE, error = null) } }

    fun cambiarReporte(incluir: Boolean) {
        if (_uiState.value.fase == FaseEnvio.PREPARANDO) return
        _uiState.update { it.copy(incluirReporte = incluir, fase = FaseEnvio.LIBRE, error = null) } }

    fun cambiarFotos(incluir: Boolean) {
        if (_uiState.value.fase == FaseEnvio.PREPARANDO) return
        _uiState.update { it.copy(incluirFotos = incluir, fase = FaseEnvio.LIBRE, error = null) } }

    // Vuelve al estado inicial al cerrar la hoja.
    fun reiniciar() {
        if (_uiState.value.fase == FaseEnvio.PREPARANDO) return
        _uiState.update { it.copy(fase = FaseEnvio.LIBRE, error = null) } }

    fun enviar(datos: DatosReporte, mensaje: String, tecnicoId: Long) {
        val actual = _uiState.value
        if (!actual.puedeEnviar) return
        val telefono = FormatoReporte.telefonoWhatsApp(datos.telefono)
        if (telefono == null) {
            _uiState.update { it.copy(fase = FaseEnvio.ERROR,
                error = "El teléfono del cliente no es válido. Corrígelo desde Editar.") }
            return }
        val fotos = if (actual.incluirFotos) datos.fotos.map { it.archivo }.filter { it.isFile }.take(MAXIMO_FOTOS) else emptyList()
        if (!actual.incluirReporte && fotos.isEmpty()) {
            _uiState.update { it.copy(fase = FaseEnvio.ERROR, error = "Este ingreso no tiene fotos para enviar.") }
            return }

        _uiState.update { it.copy(fase = FaseEnvio.PREPARANDO, error = null) }
        viewModelScope.launch {
            try {
                val archivos = prepararArchivos(completar(datos, actual.tipo), actual.tipo, actual.incluirReporte, fotos)
                val resultado = envio.enviar(SolicitudEnvio(telefono, mensaje, archivos.first, archivos.second))
                when (resultado) {
                    is ResultadoEnvio.Listo -> {
                        val detalle = detalleHistorial(actual.tipo, actual.incluirReporte, archivos.second.size)
                        // El historial es un registro auxiliar: si falla, el envío ya se abrió.
                        try { dashboardRepository.registrarEnvioReporte(ingresoId, detalle, tecnicoId)
                        } catch (cancelacion: CancellationException) { throw cancelacion
                        } catch (_: Exception) { }
                        _uiState.update { it.copy(fase = FaseEnvio.LISTO) } }
                    is ResultadoEnvio.Error ->
                        _uiState.update { it.copy(fase = FaseEnvio.ERROR, error = resultado.mensaje) } }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update { it.copy(fase = FaseEnvio.ERROR,
                    error = error.message ?: "No fue posible preparar el reporte.") } } } }

    // Agrega al reporte lo que solo se conoce al enviarlo: el técnico, la firma y los datos de entrega.
    private suspend fun completar(datos: DatosReporte, tipo: TipoReporte): DatosReporte {
        // La firma solo va en el acta de entrega.
        val firma = if (tipo == TipoReporte.ACTA) firmaRepository.obtener(ingresoId)
            ?.let { FirmaReporte(File(it.rutaImagen), it.fecha) }?.takeIf { it.archivo.isFile } else null
        val entrega = if (tipo == TipoReporte.ACTA) dashboardRepository.obtenerEntrega(ingresoId) else null
        // En el acta firma quien entregó; en los demás, quien guardó el diagnóstico.
        val tecnico = if (tipo == TipoReporte.ACTA) entrega?.tecnicoId else datos.tecnicoId
        val nombre = tecnico?.let { dashboardRepository.obtenerNombreTecnico(it) }
        return datos.copy(firma = firma, entrega = entrega, tecnicoNombre = nombre) }

    // Deja en la carpeta temporal el PDF y copias de las fotos.
    private suspend fun prepararArchivos(
        datos: DatosReporte,
        tipo: TipoReporte,
        incluirReporte: Boolean,
        fotos: List<File>
    ): Pair<File?, List<File>> = withContext(Dispatchers.IO) {
        carpetaTemporal.deleteRecursively()
        carpetaTemporal.mkdirs()
        val pdf = if (incluirReporte) {
            val prefijo = when (tipo) { TipoReporte.COMPLETO -> "reporte"; TipoReporte.COTIZACION -> "cotizacion"; TipoReporte.ACTA -> "acta" }
            generador.generar(datos, tipo, File(carpetaTemporal, "${prefijo}_${datos.placa}.pdf"))
        } else null
        val copias = fotos.mapIndexed { indice, foto ->
            foto.copyTo(File(carpetaTemporal, "foto_${indice + 1}.jpg"), overwrite = true) }
        pdf to copias }

    private fun detalleHistorial(tipo: TipoReporte, conReporte: Boolean, fotos: Int): String {
        val partes = buildList {
            if (conReporte) add(when (tipo) {
                TipoReporte.COMPLETO -> "reporte en PDF"
                TipoReporte.COTIZACION -> "cotización en PDF"
                TipoReporte.ACTA -> "acta de entrega en PDF" })
            if (fotos > 0) add(if (fotos == 1) "1 foto" else "$fotos fotos") }
        return "Se abrió WhatsApp para enviar: ${partes.joinToString(" y ")}." }

    private companion object {
        const val MAXIMO_FOTOS = 10 } }
