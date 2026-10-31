package com.example.myapplication.ui.usuarios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.ui.components.EncabezadoAutoClimas

// Edita un usuario: nombre, usuario con que inicia sesión, contraseña y, en el perfil propio, el código de recuperación.
// Al guardar avisa lo ocurrido y la app regresa a la lista.
@Composable
fun EditarUsuarioScreen(
    viewModel: EditarUsuarioViewModel,
    onVolver: () -> Unit,
    onGuardado: (UsuarioEditado) -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val onGuardadoActual by rememberUpdatedState(onGuardado)
    val colors = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    // Un solo botón muestra u oculta las contraseñas de esta pantalla.
    var passwordVisible by remember { mutableStateOf(false) }
    val siguiente = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
    val habilitado = !estado.guardando && estado.guardado == null

    LaunchedEffect(estado.guardado) { estado.guardado?.let { onGuardadoActual(it) } }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            EncabezadoAutoClimas(
                titulo = stringResource(R.string.usuarios_edit_title),
                subtitulo = estado.nombreOriginal.ifBlank { null },
                onVolver = onVolver, habilitado = !estado.guardando) },
        bottomBar = {
            Surface(color = colors.surface, shadowElevation = 8.dp) {
                BotonPrincipal(
                    texto = stringResource(if (estado.guardando) R.string.usuarios_saving else R.string.usuarios_edit_save),
                    onClick = viewModel::guardar,
                    habilitado = estado.puedeGuardar, cargando = estado.guardando,
                    modifier = Modifier.navigationBarsPadding().imePadding().padding(horizontal = 16.dp, vertical = 12.dp)) } }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            when {
                estado.cargando -> CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                estado.noEncontrado -> Text(stringResource(R.string.usuarios_edit_not_found), color = colors.error,
                    modifier = Modifier.padding(24.dp))
                else -> Column(
                    modifier = Modifier.widthIn(max = 560.dp).fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    CampoUsuario(
                        etiqueta = stringResource(R.string.usuarios_field_name),
                        valor = estado.nombre, onCambio = viewModel::onNombreChange,
                        sugerencia = stringResource(R.string.usuarios_field_name_hint),
                        icono = Icons.Filled.Person, habilitado = habilitado,
                        error = estado.errorNombre?.let { stringResource(it) },
                        teclado = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        acciones = siguiente)
                    CampoUsuario(
                        etiqueta = stringResource(R.string.usuarios_field_user),
                        valor = estado.usuario, onCambio = viewModel::onUsuarioChange,
                        sugerencia = stringResource(R.string.usuarios_field_user_hint),
                        icono = Icons.Outlined.AccountCircle, habilitado = habilitado && !estado.esPropio,
                        error = estado.errorUsuario?.let { stringResource(it) },
                        ayuda = stringResource(
                            if (estado.esPropio) R.string.usuarios_edit_user_locked else R.string.usuarios_field_user_help),
                        teclado = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
                        acciones = siguiente)

                    HorizontalDivider(color = colors.outlineVariant)
                    Text(stringResource(R.string.usuarios_edit_password_section), style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface)
                    Text(
                        stringResource(if (estado.esPropio) R.string.usuarios_edit_password_own
                            else R.string.usuarios_edit_password_other),
                        style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    if (estado.esPropio) {
                        CampoUsuario(
                            etiqueta = stringResource(R.string.usuarios_field_password_current),
                            valor = estado.passwordActual, onCambio = viewModel::onPasswordActualChange,
                            sugerencia = stringResource(R.string.usuarios_field_password_current_hint),
                            icono = Icons.Filled.Lock, habilitado = habilitado,
                            esPassword = true, passwordVisible = passwordVisible,
                            onAlternarPassword = { passwordVisible = !passwordVisible },
                            error = estado.errorPasswordActual?.let { stringResource(it) },
                            teclado = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                            acciones = siguiente) }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        CampoUsuario(
                            etiqueta = stringResource(R.string.usuarios_field_password_new),
                            valor = estado.passwordNueva, onCambio = viewModel::onPasswordNuevaChange,
                            sugerencia = stringResource(R.string.usuarios_field_password_hint),
                            icono = Icons.Filled.Lock, habilitado = habilitado,
                            esPassword = true, passwordVisible = passwordVisible,
                            onAlternarPassword = if (estado.esPropio) null else { { passwordVisible = !passwordVisible } },
                            teclado = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                            acciones = siguiente)
                        ReglaPassword(stringResource(R.string.usuarios_rule_length), estado.passwordNueva.isEmpty(),
                            estado.passwordLarga)
                        ReglaPassword(stringResource(R.string.usuarios_rule_different), estado.passwordNueva.isEmpty(),
                            estado.passwordDistinta) }
                    CampoUsuario(
                        etiqueta = stringResource(R.string.usuarios_field_confirm),
                        valor = estado.confirmacion, onCambio = viewModel::onConfirmacionChange,
                        sugerencia = stringResource(R.string.usuarios_field_confirm_hint),
                        icono = Icons.Filled.Lock, habilitado = habilitado,
                        esPassword = true, passwordVisible = passwordVisible,
                        error = if (estado.hayConfirmacionIncorrecta) stringResource(R.string.usuarios_error_confirm) else null,
                        teclado = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        acciones = KeyboardActions(onDone = { focusManager.clearFocus() }))

                    if (estado.esPropio) {
                        HorizontalDivider(color = colors.outlineVariant)
                        Text(stringResource(R.string.usuarios_edit_code_section),
                            style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                        Text(
                            stringResource(if (estado.tieneRecuperacion) R.string.usuarios_edit_code_set
                                else R.string.usuarios_edit_code_unset),
                            style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                        OutlinedButton(onClick = viewModel::pedirCodigo, enabled = habilitado,
                            modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.usuarios_edit_code_generate)) } }

                    estado.errorGeneral?.let { mensaje ->
                        Text(text = stringResource(mensaje), color = colors.error, style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) } } } } }

    if (estado.pidiendoPasswordCodigo) {
        AlertDialog(
            onDismissRequest = viewModel::cancelarCodigo,
            title = { Text(stringResource(R.string.usuarios_code_ask_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.usuarios_code_ask_text))
                    CampoUsuario(
                        etiqueta = stringResource(R.string.usuarios_field_password_current),
                        valor = estado.passwordCodigo, onCambio = viewModel::onPasswordCodigoChange,
                        sugerencia = stringResource(R.string.usuarios_field_password_current_hint),
                        icono = Icons.Filled.Lock, habilitado = !estado.generandoCodigo,
                        esPassword = true, passwordVisible = false,
                        error = estado.errorPasswordCodigo?.let { stringResource(it) },
                        teclado = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        acciones = KeyboardActions(onDone = { viewModel.confirmarCodigo() })) } },
            confirmButton = {
                TextButton(onClick = viewModel::confirmarCodigo, enabled = !estado.generandoCodigo) {
                    Text(stringResource(R.string.usuarios_code_ask_confirm)) } },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarCodigo, enabled = !estado.generandoCodigo) {
                    Text(stringResource(R.string.usuarios_delete_cancel)) } }) }

    // El código no se cierra tocando fuera ni con Atrás: así no se pierde antes de anotarlo.
    estado.codigoNuevo?.let { codigo ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.usuarios_code_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.usuarios_code_text))
                    CodigoRecuperacionCard(codigo) } },
            confirmButton = {
                TextButton(onClick = viewModel::cerrarCodigo) { Text(stringResource(R.string.usuarios_code_done)) } }) } }
