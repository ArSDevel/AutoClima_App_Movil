package com.example.myapplication.ui.usuarios

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.ui.components.LogoAutoClimas

// "Olvidé mi contraseña": con el usuario y el código de recuperación se elige una contraseña nueva sin iniciar sesión.
// Al terminar se muestra el código nuevo, porque el anterior queda usado.
@Composable
fun RecuperarPasswordScreen(
    viewModel: RecuperarPasswordViewModel,
    onVolver: () -> Unit,
    onTerminar: (usuario: String) -> Unit,
    modifier: Modifier = Modifier) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    val siguiente = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
    val resultado = estado.resultado

    // Con el código nuevo a la vista, "Atrás" también lleva al inicio de sesión con el usuario escrito.
    BackHandler(enabled = !estado.guardando) {
        if (resultado != null) onTerminar(resultado.usuario) else onVolver() }

    TarjetaCentral(modifier = modifier) {
        LogoAutoClimas(
            descripcion = stringResource(R.string.login_logo_description),
            modifier = Modifier.size(width = 132.dp, height = 90.dp))
        Spacer(modifier = Modifier.height(12.dp))
        if (resultado != null) {
            TituloPrimerUso(
                titulo = stringResource(R.string.recuperar_done_title),
                subtitulo = stringResource(R.string.recuperar_done_text, resultado.usuario))
            Spacer(modifier = Modifier.height(16.dp))
            CodigoRecuperacionCard(resultado.codigoNuevo)
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.usuarios_code_text), style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(20.dp))
            BotonPrincipal(texto = stringResource(R.string.recuperar_done_button), onClick = { onTerminar(resultado.usuario) })
        } else {
            TituloPrimerUso(
                titulo = stringResource(R.string.recuperar_title),
                subtitulo = stringResource(R.string.recuperar_subtitle))
            Spacer(modifier = Modifier.height(18.dp))
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                CampoUsuario(
                    etiqueta = stringResource(R.string.usuarios_field_user),
                    valor = estado.usuario, onCambio = viewModel::onUsuarioChange,
                    sugerencia = stringResource(R.string.login_username_placeholder),
                    icono = Icons.Filled.Person, habilitado = !estado.guardando,
                    teclado = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
                    acciones = siguiente)
                CampoUsuario(
                    etiqueta = stringResource(R.string.recuperar_field_code),
                    valor = estado.codigo, onCambio = viewModel::onCodigoChange,
                    sugerencia = stringResource(R.string.recuperar_field_code_hint),
                    icono = Icons.Outlined.Key, habilitado = !estado.guardando,
                    teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
                    acciones = siguiente)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CampoUsuario(
                        etiqueta = stringResource(R.string.usuarios_field_password_new),
                        valor = estado.password, onCambio = viewModel::onPasswordChange,
                        sugerencia = stringResource(R.string.usuarios_field_password_hint),
                        icono = Icons.Filled.Lock, habilitado = !estado.guardando,
                        esPassword = true, passwordVisible = passwordVisible,
                        onAlternarPassword = { passwordVisible = !passwordVisible },
                        teclado = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        acciones = siguiente)
                    ReglaPassword(stringResource(R.string.usuarios_rule_length), estado.password.isEmpty(), estado.passwordLarga)
                    ReglaPassword(stringResource(R.string.usuarios_rule_different), estado.password.isEmpty(),
                        estado.passwordDistinta) }
                CampoUsuario(
                    etiqueta = stringResource(R.string.usuarios_field_confirm),
                    valor = estado.confirmacion, onCambio = viewModel::onConfirmacionChange,
                    sugerencia = stringResource(R.string.usuarios_field_confirm_hint),
                    icono = Icons.Filled.Lock, habilitado = !estado.guardando,
                    esPassword = true, passwordVisible = passwordVisible,
                    error = if (estado.hayConfirmacionIncorrecta) stringResource(R.string.usuarios_error_confirm) else null,
                    teclado = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    acciones = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        viewModel.restablecer() })) }
            estado.error?.let { mensaje ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = stringResource(mensaje), color = colors.error, style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) }
            Spacer(modifier = Modifier.height(20.dp))
            BotonPrincipal(
                texto = stringResource(if (estado.guardando) R.string.usuarios_saving else R.string.recuperar_button),
                onClick = viewModel::restablecer, habilitado = estado.puedeRestablecer, cargando = estado.guardando)
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(onClick = onVolver, enabled = !estado.guardando) {
                Text(stringResource(R.string.recuperar_back)) } } } }
