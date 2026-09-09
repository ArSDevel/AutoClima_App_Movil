package com.example.myapplication.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.AutoClimasGradients
import androidx.compose.ui.res.stringResource
import com.example.myapplication.R
import com.example.myapplication.ui.theme.AutoClimasWhite

/**
 * Pantalla principal de Inicio de Sesión que conecta el ViewModel con la interfaz.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Obtenemos el estado actual del ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Redirige al Dashboard cuando la sesión es exitosa
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    // Renderizado del contenido visual
    LoginContent(
        uiState = uiState,
        onIdChanged = viewModel::onIdChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onLoginClick = viewModel::login,
        modifier = modifier
    )
}

/**
 * Componente que construye los elementos visuales del formulario de Login.
 */
@Composable
fun LoginContent(
    uiState: LoginUiState,
    onIdChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Colores normalizados, bordes y tipografia para la UI
    val colors = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes
    val typography = MaterialTheme.typography
    // Estado local para alternar la visibilidad del texto de la contraseña
    var passwordVisible by remember { mutableStateOf(false) }

    // Fondo con degradado azul vertical
    val backgroundGradient = AutoClimasGradients.loginBackground

    // Degradado horizontal para el botón de acción
    val buttonGradient = AutoClimasGradients.primaryButton

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        // Tarjeta central redondeada
        Card(
            shape = shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceVariant,
                contentColor = colors.onSurface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .shadow(elevation = 12.dp, shape = shapes.extraLarge)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título del formulario
                Text(
                    text = stringResource(R.string.login_title),
                    style = typography.headlineMedium,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center
                )

                // Linea decorativa
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(4.dp)
                        .clip(shapes.extraSmall)
                        .background(colors.secondary)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.login_description),
                    style = typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Campo de entrada: ID de usuario
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.login_username_label),
                        style = typography.labelLarge,
                        color = colors.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = uiState.id,
                        onValueChange = onIdChanged,
                        textStyle = typography.bodyLarge,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.login_username_placeholder),
                                style = typography.bodyLarge,
                                color = colors.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        isError = uiState.idError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        shape = shapes.small,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.background,
                            unfocusedContainerColor = colors.background,
                            errorContainerColor = colors.background,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline,
                            errorBorderColor = colors.error,
                            focusedTextColor = colors.onBackground,
                            unfocusedTextColor = colors.onBackground,
                            errorTextColor = colors.onBackground,
                            cursorColor = colors.primary,
                            errorCursorColor = colors.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.idError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.idError,
                            color = colors.error,
                            style = typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de entrada: Contraseña
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.login_password_label),
                        style = typography.labelLarge,
                        color = colors.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = onPasswordChanged,
                        textStyle = typography.bodyLarge,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.login_password_placeholder),
                                style = typography.bodyLarge,
                                color = colors.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        isError = uiState.passwordError != null,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = stringResource(if (passwordVisible) { R.string.login_hide_password } else { R.string.login_show_password }),
                                    tint = colors.onSurfaceVariant
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        shape = shapes.small,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.background,
                            unfocusedContainerColor = colors.background,
                            errorContainerColor = colors.background,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline,
                            errorBorderColor = colors.error,
                            focusedTextColor = colors.onBackground,
                            unfocusedTextColor = colors.onBackground,
                            errorTextColor = colors.onBackground,
                            cursorColor = colors.primary,
                            errorCursorColor = colors.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.passwordError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.passwordError,
                            color = colors.error,
                            style = typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Botón principal de Iniciar Sesión
                Button(
                    onClick = onLoginClick,
                    shape = shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = AutoClimasWhite
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(shapes.small)
                        .background(brush = buttonGradient)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.login_button),
                            style = typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * Vista previa para la vista de diseño en Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MyApplicationTheme {
        LoginContent(
            uiState = LoginUiState(
                id = "01",
                password = "",
                idError = null,
                passwordError = "La contraseña no puede estar vacía"
            ),
            onIdChanged = {},
            onPasswordChanged = {},
            onLoginClick = {}
        )
    }
}
