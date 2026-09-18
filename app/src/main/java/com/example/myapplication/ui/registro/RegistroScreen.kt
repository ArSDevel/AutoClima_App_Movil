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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
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
    DASHBOARD,
    CHECKLIST,
    DIAGNOSTICO,
    CERRAR_SESION
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
            SalidaRegistro.DASHBOARD -> {
                onMenuSeleccionado(DashboardMenuOption.DASHBOARD) }
            SalidaRegistro.CHECKLIST -> {
                onMenuSeleccionado(DashboardMenuOption.CHECKLIST) }
            SalidaRegistro.DIAGNOSTICO -> {
                onMenuSeleccionado(DashboardMenuOption.DIAGNOSTICO) }
            SalidaRegistro.CERRAR_SESION -> {
                onLogout() } }
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
                                else -> snackbar.showSnackbar(mensajePendiente) } } }
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
                                        titulo = stringResource(R.string.registro_section_vehicle),
                                        descripcion = stringResource(R.string.registro_vehicle_description),
                                        icono = Icons.Default.DirectionsCar
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
                                        titulo = stringResource(R.string.registro_section_customer),
                                        descripcion = stringResource(R.string.registro_customer_description),
                                        icono = Icons.Default.Person
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
                                        titulo = stringResource(R.string.registro_section_reception),
                                        descripcion = stringResource(R.string.registro_reception_description),
                                        icono = Icons.Default.Assignment
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