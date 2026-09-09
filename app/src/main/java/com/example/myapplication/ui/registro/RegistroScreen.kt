package com.example.myapplication.ui.registro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.ui.dashboard.DashboardMenuOption
import com.example.myapplication.ui.dashboard.components.DashboardDrawer
import com.example.myapplication.ui.theme.AutoClimasGradients
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel,
    onLogout: () -> Unit,
    onMenuSeleccionado: (DashboardMenuOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RegistroContent(
        uiState = uiState,
        onPlacaChange = viewModel::onPlacaChange,
        onModeloChange = viewModel::onModeloChange,
        onColorChange = viewModel::onColorChange,
        onNumeroSerieChange = viewModel::onNumeroSerieChange,
        onNombreClienteChange = viewModel::onNombreClienteChange,
        onTelefonoClienteChange = viewModel::onTelefonoClienteChange,
        onEmailClienteChange = viewModel::onEmailClienteChange,
        onFechaEntradaChange = viewModel::onFechaEntradaChange,
        onLimpiarPlaca = viewModel::limpiarPlaca,
        onLimpiarModelo = viewModel::limpiarModelo,
        onLimpiarColor = viewModel::limpiarColor,
        onLimpiarNumeroSerie = viewModel::limpiarNumeroSerie,
        onLimpiarNombreCliente = viewModel::limpiarNombreCliente,
        onLimpiarTelefonoCliente = viewModel::limpiarTelefonoCliente,
        onLimpiarEmailCliente = viewModel::limpiarEmailCliente,
        onLimpiarFechaEntrada = viewModel::limpiarFechaEntrada,
        onRegistrar = { viewModel.registrarIngreso() },
        onLogout = onLogout,
        onMenuSeleccionado = onMenuSeleccionado,
        onResetError = viewModel::resetMensajeError,
        onResetExito = viewModel::resetRegistroExitoso,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroContent(
    uiState: RegistroUiState,
    onPlacaChange: (String) -> Unit,
    onModeloChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onNumeroSerieChange: (String) -> Unit,
    onNombreClienteChange: (String) -> Unit,
    onTelefonoClienteChange: (String) -> Unit,
    onEmailClienteChange: (String) -> Unit,
    onFechaEntradaChange: (String) -> Unit,
    onLimpiarPlaca: () -> Unit,
    onLimpiarModelo: () -> Unit,
    onLimpiarColor: () -> Unit,
    onLimpiarNumeroSerie: () -> Unit,
    onLimpiarNombreCliente: () -> Unit,
    onLimpiarTelefonoCliente: () -> Unit,
    onLimpiarEmailCliente: () -> Unit,
    onLimpiarFechaEntrada: () -> Unit,
    onRegistrar: () -> Unit,
    onLogout: () -> Unit,
    onMenuSeleccionado: (DashboardMenuOption) -> Unit,
    onResetError: () -> Unit,
    onResetExito: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensajeError) {
        uiState.mensajeError?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onResetError()
        }
    }

    LaunchedEffect(uiState.registroExitoso) {
        if (uiState.registroExitoso) {
            snackbarHostState.showSnackbar("¡Ingreso registrado con éxito!")
            onResetExito()
        }
    }

    ModalNavigationDrawer(
        modifier = modifier.fillMaxSize(),
        drawerState = drawerState,
        drawerContent = {
            DashboardDrawer(
                opcionSeleccionada = DashboardMenuOption.REGISTRO,
                onOpcionSeleccionada = { opcion ->
                    scope.launch {
                        drawerState.close()
                        onMenuSeleccionado(opcion)
                    }
                },
                onLogout = onLogout
            )
        }
    ) {
        Scaffold(
            containerColor = colors.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "AutoClimas - Registro",
                            style = typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch { drawerState.open() }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.dashboard_open_menu)
                            )
                        }
                    },
                    actions = {
                        Image(
                            painter = painterResource(R.drawable.logo_autoclimas),
                            contentDescription = stringResource(R.string.dashboard_logo_description),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .width(72.dp)
                                .height(56.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1E3A8A),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AutoClimasGradients.loginBackground)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Tarjeta contenedora de formulario
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFB5D1F8)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Columna Izquierda: Datos del Vehículo
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                RegistroCampoInput(
                                    label = "Placa del Auto",
                                    placeholder = "Ej. ABC-123",
                                    valor = uiState.placa,
                                    onValorChange = onPlacaChange,
                                    onLimpiar = onLimpiarPlaca
                                )

                                RegistroCampoInput(
                                    label = "Modelo del Auto",
                                    placeholder = "Ej. Civic 2020",
                                    valor = uiState.modelo,
                                    onValorChange = onModeloChange,
                                    onLimpiar = onLimpiarModelo
                                )

                                RegistroCampoInput(
                                    label = "Color del Auto",
                                    placeholder = "Ej. Rojo",
                                    valor = uiState.color,
                                    onValorChange = onColorChange,
                                    onLimpiar = onLimpiarColor
                                )

                                RegistroCampoInput(
                                    label = "Numero de Serie",
                                    placeholder = "Ej. SERIE-1234",
                                    valor = uiState.numeroSerie,
                                    onValorChange = onNumeroSerieChange,
                                    onLimpiar = onLimpiarNumeroSerie
                                )
                            }

                            // Columna Derecha: Datos del Cliente y Fecha
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                RegistroCampoInput(
                                    label = "Nombre del Cliente",
                                    placeholder = "Ej. Juan Pérez",
                                    valor = uiState.nombreCliente,
                                    onValorChange = onNombreClienteChange,
                                    onLimpiar = onLimpiarNombreCliente
                                )

                                RegistroCampoInput(
                                    label = "Telefono del Cliente",
                                    placeholder = "Ej. 8112345678",
                                    valor = uiState.telefonoCliente,
                                    onValorChange = onTelefonoClienteChange,
                                    onLimpiar = onLimpiarTelefonoCliente
                                )

                                RegistroCampoInput(
                                    label = "Email del Cliente",
                                    placeholder = "Ej. mail@cliente.com",
                                    valor = uiState.emailCliente,
                                    onValorChange = onEmailClienteChange,
                                    onLimpiar = onLimpiarEmailCliente
                                )

                                RegistroCampoInput(
                                    label = "Fecha de Entrada",
                                    placeholder = "Ej. 09/09/2026",
                                    valor = uiState.fechaEntrada,
                                    onValorChange = onFechaEntradaChange,
                                    onLimpiar = onLimpiarFechaEntrada
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Botón destacado para Registrar Ingreso
                        Button(
                            onClick = onRegistrar,
                            enabled = !uiState.cargando,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(brush = AutoClimasGradients.primaryButton)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.cargando) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Registrar Ingreso",
                                        style = TextStyle(
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón visual de cámara ampliado (sin funcionalidad por el momento)
                val cameraGradient = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF62A8EA),
                        Color(0xFF2C7BD4)
                    )
                )

                Surface(
                    modifier = Modifier
                        .width(220.dp)
                        .height(115.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .clickable {
                            scope.launch {
                                snackbarHostState.showSnackbar("La función de captura de evidencia estará disponible próximamente.")
                            }
                        },
                    shape = RoundedCornerShape(28.dp),
                    color = Color.Transparent,
                    shadowElevation = 10.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brush = cameraGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Cámara de evidencia (Próximamente)",
                            tint = Color.Black,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Campo de texto personalizado con placeholder en una sola línea ajustada sin corte ni salto de renglón.
 */
@Composable
fun RegistroCampoInput(
    label: String,
    placeholder: String,
    valor: String,
    onValorChange: (String) -> Unit,
    onLimpiar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        // Caja principal del Input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .height(58.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFCDE0FD))
                .border(
                    width = 1.dp,
                    color = Color(0xFF627B9B),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(start = 10.dp, end = 2.dp, top = 16.dp, bottom = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BasicTextField(
                    value = valor,
                    onValueChange = onValorChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (valor.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color(0xFF64748B)
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                IconButton(
                    onClick = onLimpiar,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Limpiar $label",
                        tint = Color(0xFF4A5568),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Etiqueta flotante montada sobre el borde superior
        Surface(
            modifier = Modifier
                .padding(start = 10.dp)
                .align(Alignment.TopStart),
            color = Color.White,
            shape = RoundedCornerShape(4.dp),
            shadowElevation = 2.dp
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Preview(
    name = "Registro Screen Preview",
    showBackground = true,
    widthDp = 400,
    heightDp = 800
)
@Composable
private fun RegistroScreenPreview() {
    MyApplicationTheme(darkTheme = false) {
        RegistroContent(
            uiState = RegistroUiState(
                placa = "ABC-123",
                nombreCliente = "Juan Pérez",
                modelo = "Civic 2020",
                telefonoCliente = "8112345678"
            ),
            onPlacaChange = {},
            onModeloChange = {},
            onColorChange = {},
            onNumeroSerieChange = {},
            onNombreClienteChange = {},
            onTelefonoClienteChange = {},
            onEmailClienteChange = {},
            onFechaEntradaChange = {},
            onLimpiarPlaca = {},
            onLimpiarModelo = {},
            onLimpiarColor = {},
            onLimpiarNumeroSerie = {},
            onLimpiarNombreCliente = {},
            onLimpiarTelefonoCliente = {},
            onLimpiarEmailCliente = {},
            onLimpiarFechaEntrada = {},
            onRegistrar = {},
            onLogout = {},
            onMenuSeleccionado = {},
            onResetError = {},
            onResetExito = {}
        )
    }
}
