package com.example.myapplication.ui.registro

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.data.local.model.FormularioIngreso
import com.example.myapplication.data.local.model.ReglasIngreso
import com.example.myapplication.ui.dashboard.DashboardMenuOption
import com.example.myapplication.ui.dashboard.components.DashboardDrawer
import com.example.myapplication.ui.theme.AutoClimasGradients
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.util.Calendar

// Conecta el estado y las acciones del ViewModel con la interfaz.
@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel,
    onLogout: () -> Unit,
    onMenuSeleccionado: (DashboardMenuOption) -> Unit,
    tecnicoId: Long,
    modifier: Modifier = Modifier,
    onVerIngreso: ((Long) -> Unit)? = null
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
                CampoRegistro.CONDICION -> viewModel.onCondicionInicialChange(valor)
            }
        },
        onGuardar = { viewModel.guardar(tecnicoId) },
        onNuevaCaptura = viewModel::resetRegistroExitoso,
        onReintentarCarga = viewModel::reintentarCarga,
        onLogout = onLogout,
        onMenuSeleccionado = onMenuSeleccionado,
        onVerIngreso = onVerIngreso,
        modifier = modifier
    )
}

// Solo se utilizan para organizar los callbacks de esta pantalla.
private enum class CampoRegistro {
    PLACA, MODELO, COLOR, SERIE, NOMBRE, TELEFONO, EMAIL, FECHA, MOTIVO, CONDICION
}

private enum class SalidaRegistro {
    DASHBOARD, CERRAR_SESION
}

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
    modifier: Modifier = Modifier,
    onVerIngreso: ((Long) -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var salidaPendiente by rememberSaveable { mutableStateOf<SalidaRegistro?>(null) }
    val mensajePendiente = stringResource(R.string.dashboard_section_pending)

    val editable = !uiState.esEdicion || ReglasIngreso.editar(uiState.estadoIngreso)
    val soloContacto = uiState.esEdicion && ReglasIngreso.soloContacto(uiState.estadoIngreso)
    // El formulario solo admite cambios cuando terminó la carga.
    val camposHabilitados = uiState.formularioDisponible && editable && !uiState.cargando && !uiState.registroExitoso
    val datosCompletosHabilitados = camposHabilitados && !soloContacto

    fun ejecutarSalida(destino: SalidaRegistro) {
        when (destino) {
            SalidaRegistro.DASHBOARD -> onMenuSeleccionado(DashboardMenuOption.DASHBOARD)
            SalidaRegistro.CERRAR_SESION -> onLogout()
        }
    }

    fun solicitarSalida(destino: SalidaRegistro) {
        // Durante el guardado esperamos a que termine la operación.
        // Durante una consulta inicial sí se permite salir.
        if (uiState.cargando) return
        if (uiState.tieneCambiosSinGuardar) { salidaPendiente = destino
        } else { ejecutarSalida(destino) }
    }

    ModalNavigationDrawer(
        modifier = modifier.fillMaxSize(),
        drawerState = drawerState,
        gesturesEnabled = !uiState.cargando,
        drawerContent = {
            DashboardDrawer(
                opcionSeleccionada = DashboardMenuOption.REGISTRO,
                onOpcionSeleccionada = { opcion ->
                    scope.launch {
                        drawerState.close()
                        if (!uiState.cargando) {
                            when (opcion) {
                                DashboardMenuOption.REGISTRO -> Unit
                                DashboardMenuOption.DASHBOARD -> solicitarSalida(SalidaRegistro.DASHBOARD)
                                else -> snackbar.showSnackbar(mensajePendiente)
                            }
                        }
                    }
                },
                onLogout = {
                    scope.launch { drawerState.close()
                        solicitarSalida(SalidaRegistro.CERRAR_SESION)
                    }
                }
            )
        }
    ) {
        // También protege la salida mediante el botón Atrás de Android.
        BackHandler {
            if (drawerState.isOpen) { scope.launch { drawerState.close() } }
            else { solicitarSalida(SalidaRegistro.DASHBOARD) }
        }
        Scaffold(
            containerColor = colors.background,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(text = stringResource(R.string.dashboard_title),
                                style = MaterialTheme.typography.titleLarge)
                            Text(text = stringResource(
                                if (uiState.esEdicion) { R.string.registro_title_edit
                                } else { R.string.registro_title_new }),
                                style = MaterialTheme.typography.labelMedium)
                        }
                    },
                    navigationIcon = { IconButton(
                        enabled = !uiState.cargando,
                        onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.dashboard_open_menu))
                        }
                    },
                    actions = {
                        Image(painter = painterResource(R.drawable.logo_autoclimas),
                            contentDescription = stringResource(R.string.dashboard_logo_description),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.padding(end = 12.dp).width(72.dp).height(56.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.tertiary,
                        titleContentColor = colors.onTertiary,
                        navigationIconContentColor = colors.onTertiary
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(AutoClimasGradients.loginBackground)
                    .padding(padding).imePadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(modifier = Modifier
                        .widthIn(max = 800.dp).fillMaxWidth()
                        .verticalScroll(rememberScrollState()).padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(elevation = 10.dp, shape = MaterialTheme.shapes.large),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = colors.surface, contentColor = colors.onSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            when {
                                // No mostramos campos vacíos mientras
                                // se está preparando una edición.
                                uiState.cargandoIngreso -> {
                                    CargaInicialRegistro(
                                        onVolver = { solicitarSalida(SalidaRegistro.DASHBOARD)}
                                    )
                                }
                                // El reintento vuelve a consultar desde
                                // el ViewModel, sin consultas en la pantalla.
                                uiState.errorCarga != null -> {
                                    ErrorCargaRegistro(mensaje = uiState.errorCarga,
                                        onReintentar = onReintentarCarga,
                                        onVolver = { solicitarSalida(SalidaRegistro.DASHBOARD) }
                                    )
                                }

                                uiState.registroExitoso -> {
                                    ResultadoRegistro(uiState = uiState,
                                        onNuevaCaptura = onNuevaCaptura,
                                        onVolver = { solicitarSalida(SalidaRegistro.DASHBOARD) },
                                        onVerIngreso = onVerIngreso
                                    )
                                }
                                else -> {
                                    Text(
                                        text = stringResource(if (uiState.esEdicion) { R.string.registro_title_edit
                                            } else { R.string.registro_title_new }),
                                        style = MaterialTheme.typography.headlineSmall)
                                    Text(text = stringResource(R.string.registro_required_hint),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.onSurfaceVariant)
                                    if (uiState.esEdicion) {
                                        Text(text = stringResource(R.string.registro_shared_warning),
                                            style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (soloContacto) {
                                        Text(text = stringResource(R.string.registro_contact_only),
                                            color = colors.primary)
                                    }
                                    if (!editable) {
                                        Text(text = stringResource(R.string.registro_read_only),
                                            color = colors.error)
                                    }
                                    SeccionRegistro(
                                        titulo = stringResource(R.string.registro_section_vehicle)
                                    ) {
                                        CamposAdaptables(
                                            primero = { CampoRegistroTexto(etiqueta = R.string.dashboard_plate,
                                                obligatorio = true, valor = uiState.placa,
                                                error = uiState.erroresCampos["placa"],
                                                habilitado = datosCompletosHabilitados,
                                                onCambio = { onCampoChange(CampoRegistro.PLACA, it) })
                                            },
                                            segundo = { CampoRegistroTexto(
                                                etiqueta = R.string.dashboard_model,
                                                obligatorio = true, valor = uiState.modelo,
                                                error = uiState.erroresCampos["modelo"],
                                                habilitado = datosCompletosHabilitados,
                                                onCambio = { onCampoChange(CampoRegistro.MODELO, it) }
                                                )
                                            }
                                        )
                                        CamposAdaptables(
                                            primero = { CampoRegistroTexto(etiqueta = R.string.dashboard_color,
                                                valor = uiState.color,
                                                habilitado = datosCompletosHabilitados,
                                                onCambio = { onCampoChange(CampoRegistro.COLOR, it) })
                                            },
                                            segundo = { CampoRegistroTexto(
                                                etiqueta = R.string.dashboard_serial_number,
                                                valor = uiState.numeroSerie, habilitado = datosCompletosHabilitados,
                                                onCambio = { onCampoChange(CampoRegistro.SERIE, it) })
                                            }
                                        )
                                    }
                                    HorizontalDivider()
                                    SeccionRegistro(
                                        titulo = stringResource(R.string.registro_section_customer)
                                    ) {
                                        CampoRegistroTexto(etiqueta = R.string.dashboard_customer,
                                            obligatorio = true, valor = uiState.nombreCliente,
                                            error = uiState.erroresCampos["nombre"],
                                            habilitado = datosCompletosHabilitados,
                                            onCambio = { onCampoChange(CampoRegistro.NOMBRE, it) }
                                        )
                                        CamposAdaptables(
                                            primero = { CampoRegistroTexto(
                                                etiqueta = R.string.dashboard_phone,
                                                obligatorio = true, valor = uiState.telefonoCliente,
                                                error = uiState.erroresCampos["telefono"],
                                                habilitado = camposHabilitados, tipo = KeyboardType.Phone,
                                                onCambio = { onCampoChange(CampoRegistro.TELEFONO, it) })
                                            },
                                            segundo = { CampoRegistroTexto(
                                                etiqueta = R.string.dashboard_email,
                                                valor = uiState.emailCliente, error = uiState.erroresCampos["email"],
                                                habilitado = camposHabilitados,
                                                tipo = KeyboardType.Email,
                                                onCambio = { onCampoChange(CampoRegistro.EMAIL, it) })
                                            }
                                        )
                                    }

                                    HorizontalDivider()

                                    SeccionRegistro(
                                        titulo = stringResource(R.string.registro_section_reception)
                                    ) {
                                        FechaRegistro(valor = uiState.fechaEntrada,
                                            error = uiState.erroresCampos["fecha"],
                                            habilitado = datosCompletosHabilitados,
                                            onCambio = { onCampoChange(CampoRegistro.FECHA, it) })
                                        CampoRegistroTexto(
                                            etiqueta = R.string.registro_reason,
                                            valor = uiState.motivoIngreso,
                                            habilitado = datosCompletosHabilitados,
                                            multilinea = true,
                                            onCambio = { onCampoChange(CampoRegistro.MOTIVO, it) })
                                        CampoRegistroTexto(
                                            etiqueta = R.string.registro_initial_condition,
                                            valor = uiState.condicionInicial,
                                            habilitado = datosCompletosHabilitados,
                                            multilinea = true,
                                            onCambio = { onCampoChange(CampoRegistro.CONDICION, it) })
                                    }
                                    HorizontalDivider()
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.PhotoCamera,
                                            contentDescription = null,
                                            tint = colors.primary)
                                        Column(modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(text = stringResource(R.string.registro_evidence_title),
                                                style = MaterialTheme.typography.titleSmall)
                                            Text(text = stringResource(R.string.registro_evidence_pending),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = colors.onSurfaceVariant)
                                        }
                                    }
                                    uiState.mensajeError?.let { mensaje ->
                                        Text(text = mensaje, color = colors.error,
                                            style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (editable) {
                                        Button(onClick = onGuardar,
                                            enabled = camposHabilitados,
                                            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            if (uiState.cargando) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = colors.primary,
                                                    strokeWidth = 2.dp)
                                                Spacer(Modifier.width(8.dp))
                                            }
                                            Text(text = stringResource(
                                                when {uiState.cargando -> R.string.registro_saving
                                                    uiState.esEdicion -> R.string.registro_save_changes
                                                    else -> R.string.registro_save_new }))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    salidaPendiente?.let { destino ->
        AlertDialog(onDismissRequest = { salidaPendiente = null },
            title = { Text(stringResource(R.string.registro_discard_title)) },
            text = { Text(stringResource(R.string.registro_discard_description)) },
            confirmButton = { TextButton(
                    enabled = !uiState.cargando,
                    onClick = { salidaPendiente = null
                        ejecutarSalida(destino) }
                ) { Text(stringResource(R.string.registro_discard_confirm)) }
            },
            dismissButton = { TextButton(onClick = { salidaPendiente = null }) {
                    Text(stringResource(R.string.registro_keep_editing))
                }
            }
        )
    }
}

/* Estado de espera durante la consulta inicial del expediente.
Permite volver sin esperar a que termine la consulta. */
@Composable
private fun CargaInicialRegistro(
    onVolver: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator()
        Text(text = stringResource(R.string.navigation_loading_form),
            style = MaterialTheme.typography.bodyLarge)
        TextButton(onClick = onVolver) {
            Text(text = stringResource(R.string.registro_back_dashboard))
        }
    }
}

// Muestra el error de carga y delega el reintento al ViewModel.
@Composable
private fun ErrorCargaRegistro(
    mensaje: String,
    onReintentar: () -> Unit,
    onVolver: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(R.string.navigation_form_error),
            style = MaterialTheme.typography.titleLarge
        )
        Text(text = mensaje,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onReintentar,
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.dashboard_retry)) }
        TextButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.registro_back_dashboard))
        }
    }
}

@Composable
private fun SeccionRegistro(
    titulo: String,
    contenido: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        contenido()
    }
}

/* Utiliza las mismas entradas en una o dos columnas.
La distribución depende del ancho, no de la orientación.*/
@Composable
private fun CamposAdaptables(
    primero: @Composable () -> Unit,
    segundo: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()
    ) {
        if (maxWidth >= 560.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.weight(1f)) { primero() }
                Box(Modifier.weight(1f)) { segundo() }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                primero()
                segundo()
            }
        }
    }
}

/* Etiqueta externa y campo Material 3.
El botón de limpiar conserva un área táctil estándar. */
@Composable
private fun CampoRegistroTexto(
    @StringRes etiqueta: Int,
    valor: String,
    onCambio: (String) -> Unit,
    habilitado: Boolean,
    error: String? = null,
    obligatorio: Boolean = false,
    multilinea: Boolean = false,
    tipo: KeyboardType = KeyboardType.Text
) {
    val nombre = stringResource(etiqueta)
    val textoEtiqueta = if (obligatorio) {
        stringResource(R.string.registro_required_label, nombre)
    } else {
        nombre
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = textoEtiqueta,
            style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(
            value = valor, onValueChange = onCambio,
            enabled = habilitado, modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small, singleLine = !multilinea,
            minLines = if (multilinea) 3 else 1, isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = tipo),
            trailingIcon = {
                if (valor.isNotEmpty() && habilitado) {
                    IconButton(onClick = { onCambio("") }) {
                        Icon(imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.registro_clear_field, nombre))
                    }
                }
            }
        )

        if (error != null) {
            Text(text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun FechaRegistro(
    valor: String,
    error: String?,
    habilitado: Boolean,
    onCambio: (String) -> Unit
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = stringResource(R.string.registro_required_label,
            stringResource(R.string.dashboard_entry_date)),
            style = MaterialTheme.typography.labelLarge)
        OutlinedButton(enabled = habilitado,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            onClick = {
                val calendario = Calendar.getInstance()
                FormularioIngreso.interpretarFecha(valor)?.let { calendario.time = it }
                DatePickerDialog(
                    context, { _, anio, mes, dia ->
                        val seleccionada = Calendar.getInstance().apply {
                            clear()
                            set(anio, mes, dia)
                        }
                        onCambio(FormularioIngreso.formatearFecha(seleccionada.timeInMillis))
                    },
                    calendario.get(Calendar.YEAR),
                    calendario.get(Calendar.MONTH),
                    calendario.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        ) {
            Icon(imageVector = Icons.Default.DateRange, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(text = valor.ifBlank { stringResource(R.string.registro_select_date) })
        }
        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ResultadoRegistro(
    uiState: RegistroUiState,
    onNuevaCaptura: () -> Unit,
    onVolver: () -> Unit,
    onVerIngreso: ((Long) -> Unit)?
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Text(text = stringResource(
                if (uiState.esEdicion) { R.string.registro_updated_success
                } else { R.string.registro_created_success }),
            style = MaterialTheme.typography.headlineSmall)
        uiState.idRegistrado?.let { id ->
            Text(text = stringResource(R.string.registro_saved_identifier, id))
            onVerIngreso?.let { abrir ->
                Button(enabled = !uiState.cargando,
                    onClick = { abrir(id) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.dashboard_card_view_detail)) }
            }
        }

        if (!uiState.esEdicion) {
            OutlinedButton(enabled = !uiState.cargando,
                onClick = onNuevaCaptura, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.registro_another))
            }
        }

        TextButton(enabled = !uiState.cargando,
            onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.registro_back_dashboard))
        }
    }
}

@Preview(
    name = "Registro · teléfono",
    showBackground = true,
    widthDp = 400,
    heightDp = 850
)
@Preview(
    name = "Registro · ancho",
    showBackground = true,
    widthDp = 800,
    heightDp = 850
)
@Composable
private fun RegistroClaroPreview() {
    MyApplicationTheme(darkTheme = false) {
        RegistroContent(
            uiState = RegistroUiState(
                placa = "ABC-123",
                nombreCliente = "Cliente de ejemplo",
                modelo = "Honda Civic",
                telefonoCliente = "8100000000"
            ),
            onCampoChange = { _, _ -> },
            onGuardar = {},
            onNuevaCaptura = {},
            onReintentarCarga = {},
            onLogout = {},
            onMenuSeleccionado = {}
        )
    }
}

@Preview(
    name = "Registro · oscuro",
    showBackground = true,
    widthDp = 400,
    heightDp = 2000
)
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
            onMenuSeleccionado = {}
        )
    }
}

@Preview(
    name = "Edición · cargando",
    showBackground = true,
    widthDp = 400,
    heightDp = 850
)
@Composable
private fun RegistroCargandoPreview() {
    MyApplicationTheme(darkTheme = false) {
        RegistroContent(
            uiState = RegistroUiState(
                ingresoIdEnEdicion = 1L,
                cargandoIngreso = true
            ),
            onCampoChange = { _, _ -> },
            onGuardar = {},
            onNuevaCaptura = {},
            onReintentarCarga = {},
            onLogout = {},
            onMenuSeleccionado = {}
        )
    }
}

@Preview(
    name = "Edición · error de carga",
    showBackground = true,
    widthDp = 400,
    heightDp = 850
)
@Composable
private fun RegistroErrorCargaPreview() {
    MyApplicationTheme(darkTheme = true) {
        RegistroContent(
            uiState = RegistroUiState(
                ingresoIdEnEdicion = 1L,
                errorCarga = "No fue posible cargar el ingreso."
            ),
            onCampoChange = { _, _ -> },
            onGuardar = {},
            onNuevaCaptura = {},
            onReintentarCarga = {},
            onLogout = {},
            onMenuSeleccionado = {}
        )
    }
}