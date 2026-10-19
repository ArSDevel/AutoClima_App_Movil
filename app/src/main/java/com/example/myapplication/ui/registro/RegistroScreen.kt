package com.example.myapplication.ui.registro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.data.local.model.FormularioIngreso
import com.example.myapplication.data.local.model.ReglasIngreso
import com.example.myapplication.ui.dashboard.DashboardMenuOption
import com.example.myapplication.ui.dashboard.components.DashboardDrawer
import com.example.myapplication.ui.registro.components.CampoRegistroTexto
import com.example.myapplication.ui.registro.components.CamposAdaptables
import com.example.myapplication.ui.registro.components.CargaInicialRegistro
import com.example.myapplication.ui.registro.components.ErrorCargaRegistro
import com.example.myapplication.ui.registro.components.FechaRegistro
import com.example.myapplication.ui.registro.components.ResultadoRegistro
import com.example.myapplication.ui.registro.components.SeccionRegistro
import com.example.myapplication.ui.theme.AutoClimasGradients
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import com.example.myapplication.ui.camara.components.GaleriaFotografias
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.layout
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.text.font.FontWeight
import com.example.myapplication.data.catalogo.CatalogoVehiculos
import com.example.myapplication.ui.components.BarraAccionInferior
import com.example.myapplication.ui.components.EncabezadoAutoClimas
import com.example.myapplication.data.local.model.EstadoIngreso
import com.example.myapplication.ui.components.PildoraEstado
import com.example.myapplication.ui.components.CampoBloqueado
import androidx.compose.material.icons.filled.Lock
import com.example.myapplication.ui.components.DialogoAviso
import com.example.myapplication.ui.components.TipoAviso
import java.io.File

// Conecta el estado y las acciones del ViewModel con la interfaz.
@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel,
    onLogout: () -> Unit,
    onMenuSeleccionado: (DashboardMenuOption) -> Unit,
    tecnicoId: Long,
    onTomarFotoClick: () -> Unit,
    modifier: Modifier = Modifier,
    onVerIngreso: ((Long) -> Unit)? = null,
    onVolver: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RegistroContent(
        uiState = uiState,
        onCampoChange = { campo, valor ->
            when (campo) {
                CampoRegistro.PLACA -> viewModel.onPlacaChange(valor)
                CampoRegistro.MODELO -> viewModel.onModeloChange(valor)
                CampoRegistro.COLOR -> viewModel.onColorChange(valor)
                CampoRegistro.SERIE -> viewModel.onNumeroSerieChange(valor)
                CampoRegistro.NOMBRE -> viewModel.onNombreClienteChange(valor)
                CampoRegistro.TELEFONO -> viewModel.onTelefonoClienteChange(valor)
                CampoRegistro.EMAIL -> viewModel.onEmailClienteChange(valor)
                CampoRegistro.FECHA -> viewModel.onFechaEntradaChange(valor)
                CampoRegistro.MOTIVO -> viewModel.onMotivoIngresoChange(valor)
                CampoRegistro.CONDICION -> viewModel.onCondicionInicialChange(valor) } },
        onGuardar = { viewModel.guardar(tecnicoId) },
        onNuevaCaptura = viewModel::resetRegistroExitoso,
        onReintentarCarga = viewModel::reintentarCarga,
        onTomarFotoClick = onTomarFotoClick,
        onSeleccionarFoto = viewModel::seleccionarFotoPrincipal,
        onQuitarFoto = viewModel::quitarFotoPendiente,
        onPasoChange = viewModel::cambiarPaso,
        onValidarPaso = viewModel::validarPaso,
        onLogout = onLogout,
        onMenuSeleccionado = onMenuSeleccionado,
        onVerIngreso = onVerIngreso,
        onVolver = onVolver,
        modifier = modifier) }

// Solo se utilizan para organizar los callbacks de esta pantalla.
private enum class CampoRegistro {
    PLACA, MODELO, COLOR, SERIE, NOMBRE, TELEFONO, EMAIL, FECHA, MOTIVO, CONDICION }

// A dónde se quiere ir al salir del formulario.
private enum class SalidaRegistro {
    // Volver a la pantalla anterior (flecha, ×, atrás del sistema).
    VOLVER,
    DASHBOARD,
    CHECKLIST,
    DIAGNOSTICO,
    EVIDENCIAS,
    HISTORIAL,
    CERRAR_SESION }

// Contenido del registro: tres pasos al crear y una sola pantalla al editar.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistroContent(
    uiState: RegistroUiState,
    onCampoChange: (CampoRegistro, String) -> Unit,
    onGuardar: () -> Unit,
    onNuevaCaptura: () -> Unit,
    onReintentarCarga: () -> Unit,
    onLogout: () -> Unit,
    onMenuSeleccionado: (DashboardMenuOption) -> Unit,
    onTomarFotoClick: () -> Unit,
    modifier: Modifier = Modifier,
    onVerIngreso: ((Long) -> Unit)? = null,
    onSeleccionarFoto: (String) -> Unit = {},
    onQuitarFoto: (String) -> Unit = {},
    onPasoChange: (Int) -> Unit = {},
    onValidarPaso: (Int) -> Boolean = { true },
    onVolver: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var salidaPendiente by rememberSaveable { mutableStateOf<SalidaRegistro?>(null) }
    // Paso visible del formulario: 0 cliente, 1 vehículo, 2 recepción.
    val paso = uiState.paso
    val mensajePendiente = stringResource(R.string.dashboard_section_pending)

    val editable = !uiState.esEdicion || ReglasIngreso.editar(uiState.estadoIngreso)
    val soloContacto = uiState.esEdicion && ReglasIngreso.soloContacto(uiState.estadoIngreso)
    // El formulario solo admite cambios cuando terminó la carga.
    val camposHabilitados = uiState.formularioDisponible && editable && !uiState.cargando && !uiState.registroExitoso
    val datosCompletosHabilitados = camposHabilitados && !soloContacto

    // Una nueva captura vuelve a empezar desde el primer paso.
    LaunchedEffect(uiState.registroExitoso) {
        if (uiState.registroExitoso) onPasoChange(0) }

    val mostrandoFormulario = !uiState.cargandoIngreso && uiState.errorCarga == null && !uiState.registroExitoso
    val puedeAvanzar = when (paso) {
        0 -> uiState.nombreCliente.isNotBlank() && uiState.telefonoCliente.isNotBlank()
        1 -> FormularioIngreso.placaValida(uiState.placa) && uiState.modelo.isNotBlank()
        else -> true }

    // Realiza la salida elegida.
    fun ejecutarSalida(destino: SalidaRegistro) {
        when (destino) {
            SalidaRegistro.VOLVER -> onVolver()
            SalidaRegistro.DASHBOARD -> {
                onMenuSeleccionado(DashboardMenuOption.DASHBOARD) }
            SalidaRegistro.CHECKLIST -> {
                onMenuSeleccionado(DashboardMenuOption.CHECKLIST) }
            SalidaRegistro.DIAGNOSTICO -> {
                onMenuSeleccionado(DashboardMenuOption.DIAGNOSTICO) }
            SalidaRegistro.EVIDENCIAS -> {
                onMenuSeleccionado(DashboardMenuOption.EVIDENCIAS) }
            SalidaRegistro.HISTORIAL -> {
                onMenuSeleccionado(DashboardMenuOption.HISTORIAL) }
            SalidaRegistro.CERRAR_SESION -> {
                onLogout() } } }

    // Sale directo o pide confirmar si hay cambios sin guardar.
    fun solicitarSalida(destino: SalidaRegistro) {
        // Durante el guardado esperamos a que termine la operación.
        // Durante una consulta inicial sí se permite salir.
        if (uiState.cargando) return
        if (uiState.tieneCambiosSinGuardar) { salidaPendiente = destino
        } else { ejecutarSalida(destino) } }

    ModalNavigationDrawer(
        modifier = modifier.fillMaxSize(),
        drawerState = drawerState,
        gesturesEnabled = !uiState.cargando,
        drawerContent = {
            DashboardDrawer(
                opcionSeleccionada = DashboardMenuOption.REGISTRO,
                onOpcionSeleccionada = { opcion ->
                    scope.launch { drawerState.close()
                        if (!uiState.cargando) {
                            when (opcion) {
                                DashboardMenuOption.REGISTRO -> Unit
                                DashboardMenuOption.DASHBOARD -> {
                                    solicitarSalida(SalidaRegistro.DASHBOARD) }
                                DashboardMenuOption.CHECKLIST -> {
                                    solicitarSalida(SalidaRegistro.CHECKLIST) }
                                DashboardMenuOption.DIAGNOSTICO -> {
                                    solicitarSalida(SalidaRegistro.DIAGNOSTICO) }
                                DashboardMenuOption.EVIDENCIAS -> {
                                    solicitarSalida(SalidaRegistro.EVIDENCIAS) }
                                DashboardMenuOption.HISTORIAL -> {
                                    solicitarSalida(SalidaRegistro.HISTORIAL) }
                                else -> snackbar.showSnackbar(mensajePendiente) } } } },
                onLogout = {
                    scope.launch { drawerState.close()
                        solicitarSalida(SalidaRegistro.CERRAR_SESION) } }) }
    ) {
        // También protege la salida mediante el botón Atrás de Android.
        BackHandler {
            if (drawerState.isOpen) { scope.launch { drawerState.close() } }
            else { solicitarSalida(SalidaRegistro.VOLVER) } }
        Scaffold(
            containerColor = colors.background,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                if (uiState.esEdicion) {
                    // Editar: una sola pantalla, con el estado a la derecha de la cabecera.
                    EncabezadoAutoClimas(
                        titulo = stringResource(R.string.registro_title_edit),
                        subtitulo = uiState.placa.ifBlank { null },
                        onVolver = { solicitarSalida(SalidaRegistro.VOLVER) },
                        habilitado = !uiState.cargando,
                        acciones = {
                            if (uiState.estadoIngreso != EstadoIngreso.NO_DISPONIBLE || uiState.formularioDisponible) {
                                PildoraEstado(estado = uiState.estadoIngreso, sobreCabecera = true) } })
                } else {
                Column {
                    EncabezadoAutoClimas(
                        titulo = stringResource(R.string.registro_title_new),
                        onVolver = { solicitarSalida(SalidaRegistro.VOLVER) },
                        habilitado = !uiState.cargando,
                        iconoVolver = Icons.Default.Close,
                        descripcionVolver = stringResource(R.string.evidence_close),
                        espacioInferior = 36.dp)
                    // La tarjeta de pasos se superpone a la parte inferior de la cabecera.
                    if (mostrandoFormulario) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .layout { medible, restricciones ->
                                    val colocable = medible.measure(restricciones)
                                    val solape = 36.dp.roundToPx()
                                    layout(colocable.width, colocable.height - solape) {
                                        colocable.place(0, -solape) } }
                        ) { PasosRegistro(paso = paso) } } } } },
            bottomBar = {
                if (mostrandoFormulario && editable && uiState.esEdicion) {
                    BarraAccionInferior {
                        Button(
                            onClick = onGuardar,
                            enabled = camposHabilitados,
                            modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (uiState.cargando) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp),
                                    color = colors.onPrimary, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null) }
                            Text(
                                text = "  " + stringResource(
                                    if (uiState.cargando) R.string.registro_saving else R.string.registro_save),
                                style = MaterialTheme.typography.titleMedium) } }
                } else if (mostrandoFormulario && editable) {
                    BarraAccionInferior {
                        if (paso > 0) {
                            OutlinedButton(
                                onClick = { onPasoChange(paso - 1) },
                                enabled = !uiState.cargando,
                                modifier = Modifier.heightIn(min = 56.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) { Text(stringResource(R.string.registro_back), style = MaterialTheme.typography.titleMedium) } }
                        Button(
                            onClick = {
                                if (paso < 2) { if (onValidarPaso(paso)) onPasoChange(paso + 1) } else { onGuardar() } },
                            enabled = camposHabilitados || (paso < 2 && !uiState.cargando),
                            modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (uiState.cargando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = colors.onPrimary,
                                    strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp)) }
                            Text(
                                text = stringResource(
                                    when {
                                        paso < 2 -> R.string.registro_next
                                        uiState.cargando -> R.string.registro_saving
                                        uiState.esEdicion -> R.string.registro_save_changes
                                        else -> R.string.registro_save_new }),
                                style = MaterialTheme.typography.titleMedium)
                            if (paso < 2 && !uiState.cargando) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.padding(start = 4.dp)) } } } } }
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(modifier = Modifier
                    .widthIn(max = 640.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when {
                        // No mostramos campos vacíos mientras
                        // se está preparando una edición.
                        uiState.cargandoIngreso -> {
                            CargaInicialRegistro(
                                onVolver = { solicitarSalida(SalidaRegistro.VOLVER)}) }
                        // El reintento vuelve a consultar desde
                        // el ViewModel, sin consultas en la pantalla.
                        uiState.errorCarga != null -> {
                            ErrorCargaRegistro(mensaje = uiState.errorCarga,
                                onReintentar = onReintentarCarga,
                                onVolver = { solicitarSalida(SalidaRegistro.VOLVER) }) }

                        uiState.registroExitoso -> {
                            ResultadoRegistro(uiState = uiState,
                                onNuevaCaptura = onNuevaCaptura,
                                onVolver = { solicitarSalida(SalidaRegistro.VOLVER) },
                                onVerIngreso = onVerIngreso) }
                        uiState.esEdicion -> {
                            ContenidoEdicion(
                                uiState = uiState,
                                soloContacto = soloContacto,
                                editable = editable,
                                camposHabilitados = camposHabilitados,
                                datosCompletosHabilitados = datosCompletosHabilitados,
                                onCampoChange = onCampoChange,
                                onTomarFotoClick = onTomarFotoClick,
                                onSeleccionarFoto = onSeleccionarFoto,
                                onQuitarFoto = onQuitarFoto) }
                        else -> {
                            // Avisos breves en una franja de color suave.
                            if (uiState.esEdicion && paso < 2) {
                                AvisoRegistro(texto = stringResource(R.string.registro_shared_warning)) }
                            if (soloContacto) {
                                AvisoRegistro(texto = stringResource(R.string.registro_contact_only)) }
                            if (!editable) {
                                Text(text = stringResource(R.string.registro_read_only),
                                    color = colors.error) }
                            when (paso) {
                                0 -> {
                                    CampoRegistroTexto(etiqueta = R.string.registro_name,
                                        obligatorio = true, valor = uiState.nombreCliente,
                                        error = uiState.erroresCampos["nombre"],
                                        habilitado = datosCompletosHabilitados,
                                        onCambio = { onCampoChange(CampoRegistro.NOMBRE, it) })
                                    CampoRegistroTexto(
                                        etiqueta = R.string.dashboard_phone,
                                        obligatorio = true, valor = uiState.telefonoCliente,
                                        error = uiState.erroresCampos["telefono"],
                                        habilitado = camposHabilitados, tipo = KeyboardType.Phone,
                                        onCambio = { onCampoChange(CampoRegistro.TELEFONO, it) })
                                    CampoRegistroTexto(
                                        etiqueta = R.string.dashboard_email,
                                        valor = uiState.emailCliente, error = uiState.erroresCampos["email"],
                                        habilitado = camposHabilitados,
                                        tipo = KeyboardType.Email,
                                        onCambio = { onCampoChange(CampoRegistro.EMAIL, it) }) }
                                1 -> {
                                    CampoRegistroTexto(etiqueta = R.string.dashboard_plate,
                                        obligatorio = true, valor = uiState.placa,
                                        error = uiState.erroresCampos["placa"]
                                            ?: if (uiState.placa.count { it != '-' } >= 5 &&
                                                !FormularioIngreso.placaValida(uiState.placa)) {
                                                FormularioIngreso.MENSAJE_PLACA
                                            } else null,
                                        habilitado = datosCompletosHabilitados,
                                        destacado = true,
                                        // El escáner de placa lo desarrollará otro equipo.
                                        contenidoFinal = {
                                            Surface(
                                                modifier = Modifier.padding(end = 8.dp).alpha(0.5f),
                                                shape = RoundedCornerShape(12.dp),
                                                color = colors.primaryContainer,
                                                contentColor = colors.onPrimaryContainer
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.PhotoCamera, contentDescription = null,
                                                        modifier = Modifier.size(20.dp))
                                                    Text(stringResource(R.string.registro_scan),
                                                        style = MaterialTheme.typography.labelLarge) } } },
                                        ayuda = stringResource(R.string.registro_scan_pending),
                                        onCambio = { onCampoChange(CampoRegistro.PLACA, it) })
                                    CampoRegistroTexto(
                                        etiqueta = R.string.dashboard_model,
                                        obligatorio = true, valor = uiState.modelo,
                                        error = uiState.erroresCampos["modelo"],
                                        habilitado = datosCompletosHabilitados,
                                        onCambio = { onCampoChange(CampoRegistro.MODELO, it) })
                                    // Sugerencias del catálogo; también se puede escribir otro modelo.
                                    SugerenciasModelo(
                                        texto = uiState.modelo,
                                        visible = datosCompletosHabilitados,
                                        onSeleccionar = { onCampoChange(CampoRegistro.MODELO, it) })
                                    CampoRegistroTexto(etiqueta = R.string.dashboard_color,
                                        valor = uiState.color,
                                        habilitado = datosCompletosHabilitados,
                                        onCambio = { onCampoChange(CampoRegistro.COLOR, it) })
                                    CampoRegistroTexto(
                                        etiqueta = R.string.dashboard_serial_number,
                                        valor = uiState.numeroSerie, habilitado = datosCompletosHabilitados,
                                        onCambio = { onCampoChange(CampoRegistro.SERIE, it) }) }
                                else -> {
                                    FechaRegistro(valor = uiState.fechaEntrada,
                                        error = uiState.erroresCampos["fecha"],
                                        habilitado = datosCompletosHabilitados,
                                        onCambio = { onCampoChange(CampoRegistro.FECHA, it) })
                                    CampoRegistroTexto(
                                        etiqueta = R.string.registro_reason,
                                        valor = uiState.motivoIngreso,
                                        habilitado = datosCompletosHabilitados,
                                        onCambio = { onCampoChange(CampoRegistro.MOTIVO, it) })
                                    CampoRegistroTexto(
                                        etiqueta = R.string.registro_initial_condition,
                                        valor = uiState.condicionInicial,
                                        habilitado = datosCompletosHabilitados,
                                        multilinea = true,
                                        onCambio = { onCampoChange(CampoRegistro.CONDICION, it) })
                                    Text(
                                        text = stringResource(R.string.registro_evidence_title),
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp))
                                    OutlinedButton(
                                        onClick = onTomarFotoClick,
                                        enabled = datosCompletosHabilitados,
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        border = androidx.compose.foundation.BorderStroke(2.dp, colors.primary),
                                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                            containerColor = colors.primaryContainer,
                                            contentColor = colors.primary)
                                    ) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text(stringResource(R.string.fotos_add)) }
                                    GaleriaFotografias(
                                        rutas = (uiState.fotosGuardadas + uiState.fotosNuevas).distinct(),
                                        principal = uiState.fotoPath,
                                        ocupado = uiState.cargando,
                                        onPrincipal = if (datosCompletosHabilitados) onSeleccionarFoto else null,
                                        onEliminar = if (datosCompletosHabilitados) onQuitarFoto else null,
                                        permiteEliminar = { it in uiState.fotosNuevas && datosCompletosHabilitados },
                                        mostrarVacio = false,
                                        aviso = stringResource(R.string.fotos_registration_hint))
                                    uiState.mensajeError?.let { mensaje ->
                                        Text(text = mensaje, color = colors.error,
                                            style = MaterialTheme.typography.bodyMedium) } } } } } } } } }

    salidaPendiente?.let { destino ->
        DialogoAviso(
            tipo = TipoAviso.ADVERTENCIA,
            titulo = stringResource(R.string.registro_discard_title),
            mensaje = stringResource(if (soloContacto) R.string.registro_discard_contact else R.string.registro_discard_description),
            textoSecundario = stringResource(R.string.registro_discard_confirm),
            onSecundario = { salidaPendiente = null
                ejecutarSalida(destino) },
            textoPrincipal = stringResource(R.string.registro_keep_editing),
            onPrincipal = { salidaPendiente = null },
            onDismiss = { salidaPendiente = null },
            habilitado = !uiState.cargando) } }

// Editar un ingreso en una sola pantalla. Lo que el estado no permite cambiar se ve bloqueado.
@Composable
private fun ContenidoEdicion(
    uiState: RegistroUiState,
    soloContacto: Boolean,
    editable: Boolean,
    camposHabilitados: Boolean,
    datosCompletosHabilitados: Boolean,
    onCampoChange: (CampoRegistro, String) -> Unit,
    onTomarFotoClick: () -> Unit,
    onSeleccionarFoto: (String) -> Unit,
    onQuitarFoto: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (!editable) {
            AvisoRegistro(texto = stringResource(R.string.registro_read_only), icono = Icons.Default.Lock)
        } else if (soloContacto) {
            AvisoRegistro(texto = stringResource(R.string.registro_contact_only), icono = Icons.Default.Lock)
        } else {
            AvisoRegistro(texto = stringResource(R.string.registro_shared_warning)) }

        // Cliente
        if (soloContacto || !editable) {
            CampoBloqueado(stringResource(R.string.dashboard_customer), uiState.nombreCliente)
        } else {
            CampoRegistroTexto(etiqueta = R.string.registro_name, obligatorio = true,
                valor = uiState.nombreCliente, error = uiState.erroresCampos["nombre"],
                habilitado = datosCompletosHabilitados,
                onCambio = { onCampoChange(CampoRegistro.NOMBRE, it) }) }
        CampoRegistroTexto(etiqueta = R.string.dashboard_phone, obligatorio = true,
            valor = uiState.telefonoCliente, error = uiState.erroresCampos["telefono"],
            habilitado = camposHabilitados, tipo = KeyboardType.Phone,
            onCambio = { onCampoChange(CampoRegistro.TELEFONO, it) })
        CampoRegistroTexto(etiqueta = R.string.dashboard_email,
            valor = uiState.emailCliente, error = uiState.erroresCampos["email"],
            habilitado = camposHabilitados, tipo = KeyboardType.Email,
            onCambio = { onCampoChange(CampoRegistro.EMAIL, it) })

        // Vehículo
        if (soloContacto || !editable) {
            CampoBloqueado(
                stringResource(R.string.registro_vehicle_label),
                listOf(uiState.placa, uiState.modelo).filter { it.isNotBlank() }.joinToString(" · "))
            CampoBloqueado(stringResource(R.string.registro_reason), uiState.motivoIngreso)
        } else {
            CampoRegistroTexto(etiqueta = R.string.dashboard_plate, obligatorio = true,
                valor = uiState.placa, destacado = true,
                error = uiState.erroresCampos["placa"],
                habilitado = datosCompletosHabilitados,
                onCambio = { onCampoChange(CampoRegistro.PLACA, it) })
            CampoRegistroTexto(etiqueta = R.string.dashboard_model, obligatorio = true,
                valor = uiState.modelo, error = uiState.erroresCampos["modelo"],
                habilitado = datosCompletosHabilitados,
                onCambio = { onCampoChange(CampoRegistro.MODELO, it) })
            SugerenciasModelo(
                texto = uiState.modelo,
                visible = datosCompletosHabilitados,
                onSeleccionar = { onCampoChange(CampoRegistro.MODELO, it) })
            CampoRegistroTexto(etiqueta = R.string.dashboard_color,
                valor = uiState.color, habilitado = datosCompletosHabilitados,
                onCambio = { onCampoChange(CampoRegistro.COLOR, it) })
            CampoRegistroTexto(etiqueta = R.string.dashboard_serial_number,
                valor = uiState.numeroSerie, habilitado = datosCompletosHabilitados,
                onCambio = { onCampoChange(CampoRegistro.SERIE, it) })
            FechaRegistro(valor = uiState.fechaEntrada, error = uiState.erroresCampos["fecha"],
                habilitado = datosCompletosHabilitados,
                onCambio = { onCampoChange(CampoRegistro.FECHA, it) })
            CampoRegistroTexto(etiqueta = R.string.registro_reason, valor = uiState.motivoIngreso,
                habilitado = datosCompletosHabilitados,
                onCambio = { onCampoChange(CampoRegistro.MOTIVO, it) })
            CampoRegistroTexto(etiqueta = R.string.registro_initial_condition,
                valor = uiState.condicionInicial, habilitado = datosCompletosHabilitados,
                multilinea = true,
                onCambio = { onCampoChange(CampoRegistro.CONDICION, it) })
            OutlinedButton(
                onClick = onTomarFotoClick,
                enabled = datosCompletosHabilitados,
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, colors.primary),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.primaryContainer, contentColor = colors.primary)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.fotos_add)) }
            GaleriaFotografias(
                rutas = (uiState.fotosGuardadas + uiState.fotosNuevas).distinct(),
                principal = uiState.fotoPath,
                ocupado = uiState.cargando,
                onPrincipal = if (datosCompletosHabilitados) onSeleccionarFoto else null,
                onEliminar = if (datosCompletosHabilitados) onQuitarFoto else null,
                permiteEliminar = { it in uiState.fotosNuevas && datosCompletosHabilitados },
                mostrarVacio = false,
                aviso = stringResource(R.string.fotos_registration_hint)) }
        uiState.mensajeError?.let { mensaje ->
            Text(text = mensaje, color = colors.error, style = MaterialTheme.typography.bodyMedium) } } }

// Franja azul con un aviso corto.
@Composable
private fun AvisoRegistro(texto: String, icono: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Info) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(text = texto, style = MaterialTheme.typography.bodyMedium) } } }

// Modelos del catálogo que coinciden con lo escrito, en una sola fila deslizable
// para que se vean aunque el teclado ocupe la pantalla.
@Composable
private fun SugerenciasModelo(
    texto: String,
    visible: Boolean,
    onSeleccionar: (String) -> Unit
) {
    val sugerencias = remember(texto) { CatalogoVehiculos.sugerencias(texto, limite = 8) }
    if (!visible || sugerencias.isEmpty()) return
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(sugerencias.size) { indice ->
            val sugerencia = sugerencias[indice]
            androidx.compose.material3.SuggestionChip(
                onClick = { onSeleccionar(sugerencia) },
                label = { Text(sugerencia, style = MaterialTheme.typography.labelLarge) },
                shape = RoundedCornerShape(20.dp),
                colors = androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.surface)) } } }

// Tarjeta con los tres pasos del registro.
@Composable
private fun PasosRegistro(paso: Int) {
    val colors = MaterialTheme.colorScheme
    val pasos = listOf(
        R.string.registro_step_customer,
        R.string.registro_step_vehicle,
        R.string.registro_step_reception)
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        shadowElevation = 10.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 16.dp)) {
            pasos.forEachIndexed { indice, texto ->
                val hecho = indice < paso
                val actual = indice == paso
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape)
                            .background(if (hecho) colors.primary else colors.surface)
                            .border(width = if (hecho) 0.dp else 3.dp,
                                color = if (actual) colors.primary else colors.outlineVariant,
                                shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hecho) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null,
                                modifier = Modifier.size(24.dp), tint = colors.onPrimary)
                        } else {
                            Text(text = (indice + 1).toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (actual) colors.primary else colors.onSurfaceVariant) } }
                    Text(
                        text = stringResource(texto),
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                        fontWeight = if (actual) FontWeight.Bold else FontWeight.Normal,
                        color = if (actual || hecho) colors.primary else colors.onSurfaceVariant,
                        maxLines = 1) } } } } }

// Vista previa del registro en modo claro.
@Preview(
    name = "Registro · teléfono",
    showBackground = true,
    widthDp = 400,
    heightDp = 850)
@Preview(
    name = "Registro · ancho",
    showBackground = true,
    widthDp = 800,
    heightDp = 850)
@Composable
private fun RegistroClaroPreview() {
    MyApplicationTheme(darkTheme = false) {
        RegistroContent(
            uiState = RegistroUiState(
                placa = "ABC-123",
                nombreCliente = "Cliente de ejemplo",
                modelo = "Honda Civic",
                telefonoCliente = "8100000000"),
            onCampoChange = { _, _ -> },
            onGuardar = {},
            onNuevaCaptura = {},
            onReintentarCarga = {},
            onLogout = {},
            onMenuSeleccionado = {},
            onTomarFotoClick = {}) } }

// Vista previa del registro en modo oscuro.
@Preview(
    name = "Registro · oscuro",
    showBackground = true,
    widthDp = 400,
    heightDp = 2000)
@Composable
private fun RegistroOscuroPreview() {
    MyApplicationTheme(darkTheme = true) {
        RegistroContent(
            uiState = RegistroUiState(),
            onCampoChange = { _, _ -> },
            onGuardar = {},
            onNuevaCaptura = {},
            onReintentarCarga = {},
            onLogout = {},
            onMenuSeleccionado = {},
            onTomarFotoClick = {}) } }

// Vista previa mientras se carga un ingreso para editar.
@Preview(
    name = "Edición · cargando",
    showBackground = true,
    widthDp = 400,
    heightDp = 850)
@Composable
private fun RegistroCargandoPreview() {
    MyApplicationTheme(darkTheme = false) {
        RegistroContent(
            uiState = RegistroUiState(
                ingresoIdEnEdicion = 1L,
                cargandoIngreso = true),
            onCampoChange = { _, _ -> },
            onGuardar = {},
            onNuevaCaptura = {},
            onReintentarCarga = {},
            onLogout = {},
            onMenuSeleccionado = {},
            onTomarFotoClick = {}) } }

// Vista previa con error al cargar el ingreso.
@Preview(
    name = "Edición · error de carga",
    showBackground = true,
    widthDp = 400,
    heightDp = 850)
@Composable
private fun RegistroErrorCargaPreview() {
    MyApplicationTheme(darkTheme = true) {
        RegistroContent(
            uiState = RegistroUiState(
                ingresoIdEnEdicion = 1L,
                errorCarga = "No fue posible cargar el ingreso."),
            onCampoChange = { _, _ -> },
            onGuardar = {},
            onNuevaCaptura = {},
            onReintentarCarga = {},
            onLogout = {},
            onMenuSeleccionado = {},
            onTomarFotoClick = {}) } }
