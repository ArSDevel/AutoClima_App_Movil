package com.example.myapplication.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.ui.theme.AutoClimasGradients
import com.example.myapplication.ui.theme.AutoClimasWhite
import com.example.myapplication.ui.theme.MyApplicationTheme

// Pantalla principal de Inicio de Sesión que conecta el ViewModel con la interfaz.
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Obtenemos el estado actual del ViewModel.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Mantiene actualizado el callback sin reiniciar el efecto.
    val onLoginSuccessActual by rememberUpdatedState(onLoginSuccess)
    // Notifica cuando la sesión es exitosa.
    // AutoClimaApp gestiona el cambio de ruta.
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) { onLoginSuccessActual() }
    }
    // Renderizado del contenido visual.
    LoginContent(uiState = uiState, onIdChanged = viewModel::onIdChanged,
        onPasswordChanged = viewModel::onPasswordChanged, onLoginClick = viewModel::login,
        modifier = modifier)
}

// Componente que construye los elementos visuales
// del formulario de Login.
@Composable
fun LoginContent(
    uiState: LoginUiState,
    onIdChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Colores normalizados, bordes y tipografía para la UI.
    val colors = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes
    val typography = MaterialTheme.typography
    val focusManager = LocalFocusManager.current
    // Estado local para alternar la visibilidad de la contraseña.
    var passwordVisible by remember { mutableStateOf(false) }
    // Bloquea el formulario durante la autenticación
    // y mientras se completa la navegación después del éxito.
    val formularioHabilitado = !uiState.cargando && !uiState.isLoggedIn
    val textoCargando = stringResource(R.string.login_loading)
    val etiquetaUsuario = stringResource(R.string.login_username_label)
    val etiquetaPassword = stringResource(R.string.login_password_label)

    // Fondo con degradado azul vertical.
    val backgroundGradient = AutoClimasGradients.loginBackground
    // Degradado horizontal para el botón de acción.
    val buttonGradient = AutoClimasGradients.primaryButton
    // Colores compartidos por ambos campos.
    val coloresCampos = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colors.background,
        unfocusedContainerColor = colors.background,
        errorContainerColor = colors.background,
        disabledContainerColor = colors.background,
        focusedBorderColor = colors.primary,
        unfocusedBorderColor = colors.outline,
        errorBorderColor = colors.error,
        disabledBorderColor = colors.outlineVariant,
        focusedTextColor = colors.onBackground,
        unfocusedTextColor = colors.onBackground,
        errorTextColor = colors.onBackground,
        disabledTextColor = colors.onSurfaceVariant,
        cursorColor = colors.primary,
        errorCursorColor = colors.error
    )

    // Oculta la contraseña y retira el foco al iniciar la operación.
    LaunchedEffect(uiState.cargando, uiState.isLoggedIn) {
        if (uiState.cargando || uiState.isLoggedIn) { passwordVisible = false
            focusManager.clearFocus() }
    }

    fun enviarFormulario() {
        if (!formularioHabilitado) return
        passwordVisible = false
        focusManager.clearFocus()
        onLoginClick()
    }

    Box(modifier = modifier
            .fillMaxSize().background(brush = backgroundGradient).safeDrawingPadding()
            .imePadding()
    ) {
        // Centra el formulario cuando hay espacio y permite
        // desplazarlo cuando aparece el teclado.
        Column(modifier = Modifier
                .fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tarjeta central redondeada.
            Card(shape = shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = colors.surfaceVariant,
                    contentColor = colors.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .widthIn(max = 480.dp).fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = shapes.extraLarge)
            ) { Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo AutoClimas.
                    Box(modifier = Modifier.size(104.dp).clip(CircleShape)
                            .border(width = 2.dp, color = colors.primary, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(painter = painterResource(R.drawable.logo_autoclimas),
                            contentDescription = stringResource(R.string.login_logo_description),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(72.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Título del formulario.
                    Text(text = stringResource(R.string.login_title),
                        style = typography.headlineMedium, color = colors.onSurface,
                        textAlign = TextAlign.Center)
                    // Línea decorativa.
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.width(48.dp).height(4.dp)
                        .clip(shapes.extraSmall).background(colors.secondary))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = stringResource(R.string.login_description),
                        style = typography.bodyMedium, color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(28.dp))
                    // Campo de entrada: ID de usuario.
                    Column(modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = etiquetaUsuario, style = typography.labelLarge, color = colors.onSurface)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = uiState.id, onValueChange = onIdChanged,
                            enabled = formularioHabilitado, textStyle = typography.bodyLarge,
                            placeholder = { Text(text = stringResource(R.string.login_username_placeholder),
                                    style = typography.bodyLarge) },
                            singleLine = true, isError = uiState.idError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { if (formularioHabilitado) { focusManager.moveFocus(FocusDirection.Next) } }),
                            shape = shapes.small,
                            colors = coloresCampos,
                            modifier = Modifier.fillMaxWidth().semantics { contentDescription = etiquetaUsuario }
                        )
                        uiState.idError?.let { mensaje ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = mensaje, color = colors.error,
                                style = typography.bodySmall, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Campo de entrada: Contraseña.
                    Column(modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = etiquetaPassword, style = typography.labelLarge, color = colors.onSurface)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = uiState.password, onValueChange = onPasswordChanged,
                            enabled = formularioHabilitado, textStyle = typography.bodyLarge,
                            placeholder = { Text(text = stringResource(R.string.login_password_placeholder),
                                    style = typography.bodyLarge) },
                            singleLine = true, isError = uiState.passwordError != null,
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None } else { PasswordVisualTransformation() },
                            trailingIcon = {
                                IconButton(enabled = formularioHabilitado,
                                    onClick = { passwordVisible = !passwordVisible}
                                ) {
                                    Icon(imageVector = if (passwordVisible) {
                                        Icons.Filled.Visibility
                                        } else { Icons.Filled.VisibilityOff },
                                        contentDescription = stringResource(
                                            if (passwordVisible) { R.string.login_hide_password
                                            } else { R.string.login_show_password }))
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { enviarFormulario() }),
                            shape = shapes.small, colors = coloresCampos,
                            modifier = Modifier.fillMaxWidth().semantics { contentDescription = etiquetaPassword }
                        )
                        uiState.passwordError?.let { mensaje ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = mensaje, color = colors.error,
                                style = typography.bodySmall,
                                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    // Botón principal de Iniciar Sesión.
                    // Solo usa el degradado cuando admite interacción.
                    Button(onClick = { enviarFormulario() },
                        enabled = formularioHabilitado, shape = shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = AutoClimasWhite,
                            disabledContainerColor = colors.surface,
                            disabledContentColor = colors.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).clip(shapes.small)
                            .then(if (formularioHabilitado) {
                                Modifier.background(brush = buttonGradient)
                                } else { Modifier }
                            )
                    ) { Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) { if (uiState.cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = colors.primary,
                            strokeWidth = 2.dp) }
                            Text(text = if (uiState.cargando) { textoCargando
                                } else { stringResource(R.string.login_button) },
                                style = typography.labelLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(
    name = "Login · claro",
    showBackground = true,
    widthDp = 400,
    heightDp = 850
)
@Composable
fun LoginScreenPreview() {
    MyApplicationTheme(darkTheme = false) {
        LoginContent(
            uiState = LoginUiState(),
            onIdChanged = {},
            onPasswordChanged = {},
            onLoginClick = {}
        )
    }
}

@Preview(
    name = "Login · oscuro",
    showBackground = true,
    widthDp = 400,
    heightDp = 850
)
@Composable
private fun LoginOscuroPreview() {
    MyApplicationTheme(darkTheme = true) {
        LoginContent(
            uiState = LoginUiState(),
            onIdChanged = {},
            onPasswordChanged = {},
            onLoginClick = {}
        )
    }
}

@Preview(
    name = "Login · cargando",
    showBackground = true,
    widthDp = 400,
    heightDp = 850
)
@Composable
private fun LoginCargandoPreview() {
    MyApplicationTheme(darkTheme = true) {
        LoginContent(
            uiState = LoginUiState(
                id = "erik",
                password = "1234",
                cargando = true
            ),
            onIdChanged = {},
            onPasswordChanged = {},
            onLoginClick = {}
        )
    }
}

@Preview(
    name = "Login · errores",
    showBackground = true,
    widthDp = 400,
    heightDp = 850
)
@Composable
private fun LoginErroresPreview() {
    MyApplicationTheme(darkTheme = false) {
        LoginContent(
            uiState = LoginUiState(
                idError = "El usuario no puede estar vacío",
                passwordError = "La contraseña no puede estar vacía"
            ),
            onIdChanged = {},
            onPasswordChanged = {},
            onLoginClick = {}
        )
    }
}

@Preview(
    name = "Login · horizontal",
    showBackground = true,
    widthDp = 850,
    heightDp = 400
)
@Composable
private fun LoginHorizontalPreview() {
    MyApplicationTheme(darkTheme = false) {
        LoginContent(
            uiState = LoginUiState(),
            onIdChanged = {},
            onPasswordChanged = {},
            onLoginClick = {}
        )
    }
}