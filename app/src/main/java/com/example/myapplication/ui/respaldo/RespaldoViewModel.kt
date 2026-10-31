package com.example.myapplication.ui.respaldo

import android.content.ContentResolver
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.data.respaldo.ConteosDatos
import com.example.myapplication.data.respaldo.Lado
import com.example.myapplication.data.respaldo.ManifiestoRespaldo
import com.example.myapplication.data.respaldo.ModoCombinacion
import com.example.myapplication.data.respaldo.PlanCombinacion
import com.example.myapplication.data.respaldo.RespaldoException
import com.example.myapplication.data.respaldo.ResultadoCombinacion
import com.example.myapplication.data.respaldo.ServicioRespaldo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// En qué punto de la restauración está el usuario.
enum class PasoRespaldo {
    NINGUNO,
    // Celular vacío: solo falta confirmar.
    CONFIRMAR_VACIO,
    // Celular con datos: elegir entre reemplazar todo o combinar.
    ELEGIR,
    CONFIRMAR_REEMPLAZO,
    // Pantalla "Esto es lo que pasaría" de la combinación y su confirmación final.
    VISTA_PREVIA,
    CONFIRMAR_COMBINAR,
    BORRAR_TODO }

// Copia automática guardada dentro del teléfono.
data class CopiaAutomatica(val archivo: File, val fechaMs: Long)

// Estado de la pantalla de respaldo y de la restauración desde la bienvenida.
data class RespaldoUiState(
    val trabajando: Boolean = false,
    @StringRes val textoTrabajo: Int? = null,
    @StringRes val mensaje: Int? = null,
    val mensajeEsError: Boolean = false,
    val paso: PasoRespaldo = PasoRespaldo.NINGUNO,
    // Respaldo ya revisado y lo que hay ahora en el celular, para compararlos.
    val manifiesto: ManifiestoRespaldo? = null,
    val actuales: ConteosDatos? = null,
    val plan: PlanCombinacion? = null,
    val modo: ModoCombinacion = ModoCombinacion.MAS_RECIENTE,
    val ganadores: Map<Long, Lado> = emptyMap(),
    val errorContrasena: Boolean = false,
    val resultado: ResultadoCombinacion? = null,
    val restauracionCompleta: Boolean = false,
    val archivoParaCompartir: File? = null,
    val ultimoRespaldoMs: Long? = null,
    val copias: List<CopiaAutomatica> = emptyList()) {
    val celularVacio: Boolean get() = actuales?.let { it.ingresos == 0 && it.vehiculos == 0 && it.clientes == 0 } ?: true }

// Exporta respaldos, restaura, combina y borra datos. Cada operación larga corre fuera del hilo principal.
// tecnicoActualId es el usuario de la sesión; en la bienvenida no hay sesión y vale null.
class RespaldoViewModel(
    private val servicio: ServicioRespaldo,
    private val contentResolver: ContentResolver,
    private val tecnicoActualId: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(RespaldoUiState())
    val uiState: StateFlow<RespaldoUiState> = _uiState.asStateFlow()

    init { refrescar() }

    fun nombreSugerido(): String = servicio.nombreArchivo()

    // Vuelve a leer la fecha del último respaldo y las copias automáticas.
    fun refrescar() {
        viewModelScope.launch(Dispatchers.IO) {
            val ultimo = servicio.ultimoRespaldoMs()
            val copias = servicio.copiasAutomaticas().map { CopiaAutomatica(it, it.lastModified()) }
            _uiState.update { it.copy(ultimoRespaldoMs = ultimo, copias = copias) } } }

    // ---- Exportar ----

    fun exportarA(destino: Uri) {
        if (_uiState.value.trabajando) return
        ejecutar(R.string.respaldo_exporting) {
            val salida = contentResolver.openOutputStream(destino) ?: throw RespaldoException(R.string.respaldo_error_generico)
            salida.use { servicio.exportarManual(it) }
            _uiState.update { it.copy(mensaje = R.string.respaldo_exported, mensajeEsError = false) } } }

    fun prepararParaCompartir() {
        if (_uiState.value.trabajando) return
        ejecutar(R.string.respaldo_exporting) {
            val archivo = servicio.exportarParaCompartir()
            _uiState.update { it.copy(archivoParaCompartir = archivo) } } }

    fun compartirListo() { _uiState.update { it.copy(archivoParaCompartir = null) } }

    // ---- Elegir un respaldo ----

    // Revisa el respaldo elegido, sin tocar los datos, y decide el siguiente paso según lo que haya en el celular.
    fun elegirRespaldo(origen: Uri) = revisar {
        contentResolver.openInputStream(origen) ?: throw RespaldoException(R.string.respaldo_error_generico) }

    fun elegirCopia(copia: CopiaAutomatica) = revisar { copia.archivo.inputStream() }

    private fun revisar(abrir: () -> java.io.InputStream) {
        if (_uiState.value.trabajando) return
        ejecutar(R.string.respaldo_checking) {
            val manifiesto = abrir().use { servicio.prepararRestauracion(it) }
            val actuales = servicio.conteosActuales()
            val vacio = actuales.ingresos == 0 && actuales.vehiculos == 0 && actuales.clientes == 0
            _uiState.update { it.copy(
                manifiesto = manifiesto, actuales = actuales, errorContrasena = false,
                paso = if (vacio) PasoRespaldo.CONFIRMAR_VACIO else PasoRespaldo.ELEGIR) } } }

    // El usuario decidió no seguir: se descarta lo que se había preparado.
    fun cancelar() {
        viewModelScope.launch(Dispatchers.IO) { servicio.descartarRestauracion() }
        _uiState.update { it.copy(paso = PasoRespaldo.NINGUNO, manifiesto = null, plan = null, ganadores = emptyMap(),
            errorContrasena = false) } }

    // ---- Restaurar en un celular vacío ----

    fun confirmarRestauracionVacia() {
        val actual = _uiState.value
        if (actual.trabajando || actual.paso != PasoRespaldo.CONFIRMAR_VACIO || !actual.celularVacio) return
        aplicarReemplazo() }

    // ---- Reemplazar todo ----

    fun elegirReemplazar() { _uiState.update { it.copy(paso = PasoRespaldo.CONFIRMAR_REEMPLAZO, errorContrasena = false) } }

    // Reemplaza todos los datos por los del respaldo. Pide la contraseña del usuario de la sesión.
    fun confirmarReemplazo(contrasena: String) {
        if (_uiState.value.trabajando || _uiState.value.paso != PasoRespaldo.CONFIRMAR_REEMPLAZO) return
        conContrasena(contrasena) { aplicarReemplazo() } }

    private fun aplicarReemplazo() {
        ejecutar(R.string.respaldo_restoring) {
            servicio.aplicarRestauracion()
            _uiState.update { it.copy(manifiesto = null, paso = PasoRespaldo.NINGUNO, restauracionCompleta = true) } } }

    // ---- Combinar ----

    fun elegirCombinar() {
        if (_uiState.value.trabajando) return
        ejecutar(R.string.respaldo_analyzing) {
            val plan = servicio.analizarCombinacion()
            val modo = ModoCombinacion.MAS_RECIENTE
            _uiState.update { it.copy(plan = plan, modo = modo, ganadores = ganadoresSegun(plan, modo),
                paso = PasoRespaldo.VISTA_PREVIA) } } }

    fun cambiarModo(modo: ModoCombinacion) {
        val plan = _uiState.value.plan ?: return
        _uiState.update { it.copy(modo = modo, ganadores = ganadoresSegun(plan, modo)) } }

    // Cambia a mano el ganador de un ingreso; el modo deja de aplicar a todos.
    fun cambiarGanador(idRespaldo: Long, lado: Lado) {
        _uiState.update { it.copy(ganadores = it.ganadores + (idRespaldo to lado)) } }

    private fun ganadoresSegun(plan: PlanCombinacion, modo: ModoCombinacion): Map<Long, Lado> =
        plan.distintos.associate { it.idRespaldo to it.ganadorSegun(modo) }

    fun pedirConfirmarCombinacion() { _uiState.update { it.copy(paso = PasoRespaldo.CONFIRMAR_COMBINAR) } }

    fun volverALaVistaPrevia() { _uiState.update { it.copy(paso = PasoRespaldo.VISTA_PREVIA) } }

    fun confirmarCombinacion() {
        val actual = _uiState.value
        val plan = actual.plan ?: return
        if (actual.trabajando || actual.paso != PasoRespaldo.CONFIRMAR_COMBINAR) return
        val actor = tecnicoActualId ?: return
        ejecutar(R.string.respaldo_combining) {
            val resultado = servicio.aplicarCombinacion(plan, actual.ganadores, actor)
            _uiState.update { it.copy(paso = PasoRespaldo.NINGUNO, manifiesto = null, plan = null, ganadores = emptyMap(),
                resultado = resultado) } } }

    fun resultadoVisto() { _uiState.update { it.copy(resultado = null) } }

    // ---- Borrar todo ----

    fun pedirBorrarTodo() { _uiState.update { it.copy(paso = PasoRespaldo.BORRAR_TODO, errorContrasena = false) } }

    fun confirmarBorrarTodo(contrasena: String) {
        val actor = tecnicoActualId ?: return
        if (_uiState.value.trabajando || _uiState.value.paso != PasoRespaldo.BORRAR_TODO) return
        conContrasena(contrasena) {
            ejecutar(R.string.respaldo_deleting) {
                servicio.borrarTodoExcepto(actor)
                _uiState.update { it.copy(paso = PasoRespaldo.NINGUNO, mensaje = R.string.respaldo_deleted_all,
                    mensajeEsError = false) } } } }

    // Comprueba la contraseña del usuario de la sesión antes de una acción que no se puede deshacer.
    private fun conContrasena(contrasena: String, accion: () -> Unit) {
        val actor = tecnicoActualId
        // Sin sesión (bienvenida, celular vacío) no hay contraseña que pedir.
        if (actor == null) { accion(); return }
        _uiState.update { it.copy(trabajando = true, textoTrabajo = R.string.respaldo_checking_password) }
        viewModelScope.launch {
            val correcta = try {
                withContext(Dispatchers.IO) { servicio.contrasenaCorrecta(actor, contrasena) }
            } catch (cancelacion: CancellationException) { throw cancelacion } catch (_: Exception) { false }
            _uiState.update { it.copy(trabajando = false, textoTrabajo = null, errorContrasena = !correcta) }
            if (correcta) accion() } }

    fun mensajeVisto() { _uiState.update { it.copy(mensaje = null) } }

    private fun ejecutar(@StringRes texto: Int, bloque: suspend () -> Unit) {
        _uiState.update { it.copy(trabajando = true, textoTrabajo = texto, mensaje = null) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                bloque()
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: RespaldoException) {
                _uiState.update { it.copy(mensaje = e.mensaje, mensajeEsError = true) }
            } catch (_: Exception) {
                _uiState.update { it.copy(mensaje = R.string.respaldo_error_generico, mensajeEsError = true) }
            } finally {
                _uiState.update { it.copy(trabajando = false, textoTrabajo = null) }
                refrescar() } } } }
