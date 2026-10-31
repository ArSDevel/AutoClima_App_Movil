package com.example.myapplication.ui.usuarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.ui.components.EncabezadoAutoClimas

// Formulario para agregar un usuario con la sesión abierta. Al guardar avisa el usuario creado y la app regresa a la lista.
@Composable
fun NuevoUsuarioScreen(
    viewModel: NuevoUsuarioViewModel,
    onVolver: () -> Unit,
    onCreado: (usuario: String) -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val onCreadoActual by rememberUpdatedState(onCreado)
    val colors = MaterialTheme.colorScheme

    LaunchedEffect(estado.creado) {
        estado.creado?.let { onCreadoActual(it.usuario) } }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            EncabezadoAutoClimas(
                titulo = stringResource(R.string.usuarios_new_title),
                onVolver = onVolver,
                habilitado = !estado.guardando) },
        bottomBar = {
            Surface(color = colors.surface, shadowElevation = 8.dp) {
                BotonPrincipal(
                    texto = stringResource(if (estado.guardando) R.string.usuarios_saving else R.string.usuarios_new_create),
                    onClick = viewModel::crear,
                    habilitado = estado.puedeCrear, cargando = estado.guardando,
                    modifier = Modifier.navigationBarsPadding().imePadding().padding(horizontal = 16.dp, vertical = 12.dp)) } }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(colors.primaryContainer).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                    Icon(imageVector = Icons.Filled.Info, contentDescription = null, tint = colors.primary,
                        modifier = Modifier.size(20.dp))
                    Text(text = stringResource(R.string.usuarios_new_info), style = MaterialTheme.typography.bodyMedium,
                        color = colors.onPrimaryContainer) }
                CamposUsuario(
                    estado = estado,
                    onNombreChange = viewModel::onNombreChange,
                    onUsuarioChange = viewModel::onUsuarioChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onConfirmacionChange = viewModel::onConfirmacionChange,
                    onEnviar = viewModel::crear)
                estado.errorGeneral?.let { mensaje ->
                    Text(text = stringResource(mensaje), color = colors.error, style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) } } } } }
