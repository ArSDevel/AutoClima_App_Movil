package com.example.myapplication.ui.usuarios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.LocalAutoClimasDark

// Los cuatro campos para crear un usuario y las reglas de la contraseña.
// Los comparten el primer uso y la pantalla de nuevo usuario.
@Composable
fun CamposUsuario(
    estado: NuevoUsuarioUiState,
    onNombreChange: (String) -> Unit,
    onUsuarioChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmacionChange: (String) -> Unit,
    onEnviar: () -> Unit,
    modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current
    val habilitado = !estado.guardando && estado.creado == null
    // Un solo botón muestra u oculta las dos contraseñas.
    var passwordVisible by remember { mutableStateOf(false) }
    val siguiente = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CampoUsuario(
            etiqueta = stringResource(R.string.usuarios_field_name),
            valor = estado.nombre, onCambio = onNombreChange,
            sugerencia = stringResource(R.string.usuarios_field_name_hint),
            icono = Icons.Filled.Person, habilitado = habilitado,
            error = estado.errorNombre?.let { stringResource(it) },
            teclado = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            acciones = siguiente)
        CampoUsuario(
            etiqueta = stringResource(R.string.usuarios_field_user),
            valor = estado.usuario, onCambio = onUsuarioChange,
            sugerencia = stringResource(R.string.usuarios_field_user_hint),
            icono = Icons.Outlined.AccountCircle, habilitado = habilitado,
            error = estado.errorUsuario?.let { stringResource(it) },
            ayuda = stringResource(R.string.usuarios_field_user_help),
            teclado = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
            acciones = siguiente)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            CampoUsuario(
                etiqueta = stringResource(R.string.usuarios_field_password),
                valor = estado.password, onCambio = onPasswordChange,
                sugerencia = stringResource(R.string.usuarios_field_password_hint),
                icono = Icons.Filled.Lock, habilitado = habilitado,
                esPassword = true, passwordVisible = passwordVisible,
                onAlternarPassword = { passwordVisible = !passwordVisible },
                teclado = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                acciones = siguiente)
            ReglaPassword(stringResource(R.string.usuarios_rule_length), estado.password.isEmpty(), estado.passwordLarga)
            ReglaPassword(stringResource(R.string.usuarios_rule_different),
                estado.password.isEmpty(), estado.passwordDistinta) }
        CampoUsuario(
            etiqueta = stringResource(R.string.usuarios_field_confirm),
            valor = estado.confirmacion, onCambio = onConfirmacionChange,
            sugerencia = stringResource(R.string.usuarios_field_confirm_hint),
            icono = Icons.Filled.Lock, habilitado = habilitado,
            esPassword = true, passwordVisible = passwordVisible,
            error = if (estado.hayConfirmacionIncorrecta) stringResource(R.string.usuarios_error_confirm) else null,
            teclado = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            acciones = KeyboardActions(onDone = {
                focusManager.clearFocus()
                onEnviar() })) } }

// Un campo con su etiqueta, ayuda y mensaje de error.
@Composable
internal fun CampoUsuario(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    sugerencia: String,
    icono: ImageVector,
    habilitado: Boolean,
    teclado: KeyboardOptions,
    acciones: KeyboardActions,
    error: String? = null,
    ayuda: String? = null,
    esPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onAlternarPassword: (() -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = etiqueta, style = typography.labelLarge, color = colors.onSurface)
        Spacer(modifier = Modifier.padding(top = 6.dp))
        OutlinedTextField(
            value = valor, onValueChange = onCambio, enabled = habilitado,
            textStyle = typography.bodyLarge,
            placeholder = { Text(text = sugerencia, style = typography.bodyLarge) },
            leadingIcon = { Icon(icono, contentDescription = null) },
            trailingIcon = if (onAlternarPassword != null) {
                {
                    // El ojo no entra en el recorrido del teclado: "Siguiente" pasa directo al campo que sigue.
                    IconButton(
                        enabled = habilitado, onClick = onAlternarPassword,
                        modifier = Modifier.focusProperties { canFocus = false }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = stringResource(
                                if (passwordVisible) R.string.login_hide_password else R.string.login_show_password)) } }
            } else null,
            visualTransformation = if (esPassword && !passwordVisible) PasswordVisualTransformation()
                else VisualTransformation.None,
            singleLine = true, isError = error != null,
            keyboardOptions = teclado, keyboardActions = acciones,
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                errorContainerColor = colors.surface,
                disabledContainerColor = colors.surfaceVariant,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outlineVariant,
                errorBorderColor = colors.error,
                disabledBorderColor = colors.outlineVariant,
                focusedTextColor = colors.onBackground,
                unfocusedTextColor = colors.onBackground,
                errorTextColor = colors.onBackground,
                disabledTextColor = colors.onSurfaceVariant,
                cursorColor = colors.primary,
                errorCursorColor = colors.error),
            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)
                .semantics { contentDescription = etiqueta })
        if (error != null) {
            Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(text = error, color = colors.error, style = typography.bodySmall,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        } else if (ayuda != null) {
            Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(text = ayuda, color = colors.onSurfaceVariant, style = typography.bodySmall) } } }

// Regla de la contraseña. Sin texto es neutral; al escribir se marca como cumplida o pendiente.
// El color siempre va con un icono distinto y con un estado para lectores de pantalla.
@Composable
internal fun ReglaPassword(texto: String, neutral: Boolean, cumplida: Boolean) {
    val colors = MaterialTheme.colorScheme
    val color = when {
        neutral -> colors.onSurfaceVariant
        cumplida -> colorCumplido()
        else -> colors.error }
    val estado = stringResource(if (cumplida) R.string.usuarios_rule_met else R.string.usuarios_rule_unmet)
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 2.dp)
            .semantics(mergeDescendants = true) { if (!neutral) stateDescription = estado },
        verticalAlignment = Alignment.CenterVertically) {
        if (neutral) {
            Spacer(modifier = Modifier.size(16.dp))
        } else {
            Icon(imageVector = if (cumplida) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null, tint = color, modifier = Modifier.size(16.dp)) }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = texto, style = MaterialTheme.typography.bodySmall, color = color) } }

// Verde de las reglas cumplidas; en tema oscuro se aclara para conservar el contraste.
@Composable
internal fun colorCumplido(): Color {
    return if (LocalAutoClimasDark.current) Color(0xFF4ADE80) else Color(0xFF15803D) }
