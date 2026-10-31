package com.example.myapplication.ui.usuarios

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.data.local.model.UsuarioListado
import com.example.myapplication.ui.components.EncabezadoAutoClimas
import com.example.myapplication.ui.dashboard.DashboardMenuOption
import com.example.myapplication.ui.dashboard.components.DashboardDrawer
import kotlinx.coroutines.launch

// Mensaje que deja la pantalla anterior al volver a la lista.
sealed interface AvisoUsuarios {
    data class Creado(val usuario: String) : AvisoUsuarios
    data class Editado(val usuario: String) : AvisoUsuarios }

// Lista de usuarios con acceso a la app. Permite agregar usuarios, editarlos y eliminar a los que no tienen registros.
// aviso trae lo que pasó en la pantalla anterior (usuario creado o editado) para mostrarlo con un mensaje.
@Composable
fun UsuariosScreen(
    viewModel: UsuariosViewModel,
    aviso: AvisoUsuarios?,
    onAvisoMostrado: () -> Unit,
    onNuevoUsuario: () -> Unit,
    onEditarUsuario: (Long) -> Unit,
    onVolver: () -> Unit,
    onMenuSeleccionado: (DashboardMenuOption) -> Unit,
    onLogout: () -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val textoAviso = when (aviso) {
        is AvisoUsuarios.Creado -> stringResource(R.string.usuarios_created, aviso.usuario)
        is AvisoUsuarios.Editado -> stringResource(R.string.usuarios_saved, aviso.usuario)
        null -> null }
    val textoEliminado = estado.usuarioEliminado?.let { stringResource(R.string.usuarios_deleted, it) }
    val textoError = if (estado.errorEliminar) stringResource(R.string.usuarios_error_delete) else null

    // Cada aviso se muestra una vez y se retira para que no reaparezca al girar la pantalla.
    LaunchedEffect(textoAviso) {
        if (textoAviso != null) {
            snackbarHostState.showSnackbar(textoAviso)
            onAvisoMostrado() } }
    LaunchedEffect(textoEliminado, textoError) {
        val mensaje = textoEliminado ?: textoError
        if (mensaje != null) {
            snackbarHostState.showSnackbar(mensaje)
            viewModel.avisoMostrado() } }

    BackHandler {
        if (drawerState.isOpen) { scope.launch { drawerState.close() } } else { onVolver() } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DashboardDrawer(
                opcionSeleccionada = DashboardMenuOption.USUARIOS,
                onOpcionSeleccionada = { opcion ->
                    scope.launch {
                        drawerState.close()
                        if (opcion != DashboardMenuOption.USUARIOS) onMenuSeleccionado(opcion) } },
                onLogout = { scope.launch { drawerState.close(); onLogout() } }) }) {
        Scaffold(
            containerColor = colors.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                EncabezadoAutoClimas(
                    titulo = stringResource(R.string.usuarios_title),
                    subtitulo = if (estado.cargando) null
                        else pluralStringResource(R.plurals.usuarios_subtitle, estado.usuarios.size, estado.usuarios.size),
                    onMenu = { scope.launch { drawerState.open() } }) },
            bottomBar = {
                Surface(color = colors.surface, shadowElevation = 8.dp) {
                    BotonPrincipal(
                        texto = stringResource(R.string.usuarios_new_button),
                        onClick = onNuevoUsuario,
                        modifier = Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) } }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                LazyColumn(
                    modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item(key = "intro") {
                        Text(
                            text = stringResource(R.string.usuarios_intro) + " " + stringResource(R.string.usuarios_intro_edit),
                            style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant) }
                    // Quien aún no tiene código de recuperación lo ve aquí hasta que lo genera.
                    if (estado.usuarios.any { it.tecnicoId == viewModel.tecnicoActualId && !it.tieneRecuperacion }) {
                        item(key = "sin_codigo") {
                            AvisoSinCodigo(onGenerar = { onEditarUsuario(viewModel.tecnicoActualId) }) } }
                    if (estado.cargando) {
                        item(key = "carga") {
                            Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(Modifier.size(24.dp))
                                Text(stringResource(R.string.usuarios_loading)) } } }
                    if (estado.hayError) {
                        item(key = "error") {
                            Text(stringResource(R.string.usuarios_error_load), color = colors.error) } }
                    items(estado.usuarios, key = { it.tecnicoId }) { usuario ->
                        TarjetaUsuario(
                            usuario = usuario,
                            esActual = usuario.tecnicoId == viewModel.tecnicoActualId,
                            onEditar = { onEditarUsuario(usuario.tecnicoId) },
                            onEliminar = { viewModel.pedirEliminar(usuario) }) } } } } }

    estado.pendienteEliminar?.let { usuario ->
        ConfirmarEliminar(
            usuario = usuario, eliminando = estado.eliminando,
            onConfirmar = viewModel::confirmarEliminar, onCancelar = viewModel::cancelarEliminar) }
    estado.bloqueado?.let { usuario ->
        NoSePuedeEliminar(usuario = usuario, onEntendido = viewModel::cerrarBloqueado) } }

// Un usuario: inicial, nombre, usuario y cantidad de ingresos. El de la sesión lleva "Tú" y los demás el botón de eliminar.
@Composable
private fun TarjetaUsuario(
    usuario: UsuarioListado,
    esActual: Boolean,
    onEditar: () -> Unit,
    onEliminar: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shape = MaterialTheme.shapes.large
    Row(
        modifier = Modifier.fillMaxWidth().shadow(elevation = 1.dp, shape = shape).clip(shape)
            .background(colors.surface)
            .clickable(role = Role.Button,
                onClickLabel = stringResource(R.string.usuarios_edit_description, usuario.usuario), onClick = onEditar)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(colors.primaryContainer),
            contentAlignment = Alignment.Center) {
            Text(text = usuario.nombre.firstOrNull()?.uppercase().orEmpty(), style = typography.titleLarge,
                color = colors.primary) }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = usuario.nombre, style = typography.titleMedium, color = colors.onSurface, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Text(text = stringResource(R.string.usuarios_user_line, usuario.usuario), style = typography.bodyMedium,
                color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = if (usuario.ingresos == 0) stringResource(R.string.usuarios_no_entries)
                    else pluralStringResource(R.plurals.usuarios_entries, usuario.ingresos, usuario.ingresos),
                style = typography.bodySmall, color = colors.onSurfaceVariant) }
        if (esActual) {
            Surface(shape = CircleShape, color = colors.primaryContainer) {
                Text(text = stringResource(R.string.usuarios_current_chip),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = typography.labelMedium, color = colors.onPrimaryContainer) }
        } else {
            IconButton(
                onClick = onEliminar,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = colors.errorContainer, contentColor = colors.onErrorContainer)) {
                Icon(imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.usuarios_delete_description, usuario.usuario)) } } } }

@Composable
private fun ConfirmarEliminar(
    usuario: UsuarioListado,
    eliminando: Boolean,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onCancelar,
        icon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = colors.error) },
        title = { Text(stringResource(R.string.usuarios_delete_title, usuario.nombre)) },
        text = { Text(stringResource(R.string.usuarios_delete_text, usuario.usuario)) },
        confirmButton = {
            TextButton(
                onClick = onConfirmar, enabled = !eliminando,
                colors = ButtonDefaults.textButtonColors(contentColor = colors.error)) {
                Text(stringResource(R.string.usuarios_delete_confirm)) } },
        dismissButton = {
            TextButton(onClick = onCancelar, enabled = !eliminando) {
                Text(stringResource(R.string.usuarios_delete_cancel)) } }) }

// Explica por qué no se puede eliminar a un usuario que ya tiene registros.
@Composable
private fun NoSePuedeEliminar(usuario: UsuarioListado, onEntendido: () -> Unit) {
    val recursos = LocalContext.current.resources
    val texto = if (usuario.ingresos > 0) {
        recursos.getQuantityString(R.plurals.usuarios_blocked_text, usuario.ingresos, usuario.usuario, usuario.ingresos)
    } else { stringResource(R.string.usuarios_blocked_text_activity, usuario.usuario) }
    AlertDialog(
        onDismissRequest = onEntendido,
        icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
        title = { Text(stringResource(R.string.usuarios_blocked_title, usuario.nombre)) },
        text = { Text(texto) },
        confirmButton = { TextButton(onClick = onEntendido) { Text(stringResource(R.string.usuarios_blocked_ok)) } }) }

// Invita a generar el código de recuperación cuando el usuario de la sesión todavía no tiene uno.
@Composable
private fun AvisoSinCodigo(onGenerar: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = MaterialTheme.shapes.large, color = colors.surface, border = BorderStroke(1.dp, colors.primary)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = stringResource(R.string.usuarios_banner_no_code), style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface)
            TextButton(onClick = onGenerar) { Text(stringResource(R.string.usuarios_banner_action)) } } } }
