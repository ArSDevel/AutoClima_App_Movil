package com.example.myapplication.ui.usuarios

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.ui.components.LogoAutoClimas
import com.example.myapplication.ui.theme.AutoClimasGradients
import com.example.myapplication.ui.theme.AutoClimasWhite
import com.example.myapplication.ui.theme.MyApplicationTheme

// Pasos del primer uso: 0 bienvenida, 1 crear usuario, 2 cuenta creada.
private const val PASO_BIENVENIDA = 0
private const val PASO_FORMULARIO = 1
private const val PASO_CREADA = 2

// Recorrido de la primera vez que se abre la app, cuando todavía no existe ningún usuario.
// Al terminar entrega el usuario creado para que el inicio de sesión lo muestre ya escrito.
@Composable
fun PrimerUsoFlow(
    viewModel: NuevoUsuarioViewModel,
    onTerminar: (usuario: String) -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var paso by rememberSaveable { mutableIntStateOf(PASO_BIENVENIDA) }

    // Pasa a la confirmación en cuanto se guarda el usuario.
    LaunchedEffect(estado.creado) {
        if (estado.creado != null) paso = PASO_CREADA }
    // Si el sistema recrea la app en la confirmación y se perdió el usuario, se sigue al inicio de sesión.
    LaunchedEffect(paso, estado.creado) {
        if (paso == PASO_CREADA && estado.creado == null) onTerminar("") }
    BackHandler(enabled = paso == PASO_FORMULARIO && !estado.guardando) { paso = PASO_BIENVENIDA }

    when (paso) {
        PASO_BIENVENIDA -> BienvenidaScreen(onComenzar = { paso = PASO_FORMULARIO })
        PASO_FORMULARIO -> CrearPrimerUsuarioScreen(
            estado = estado,
            onNombreChange = viewModel::onNombreChange,
            onUsuarioChange = viewModel::onUsuarioChange,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmacionChange = viewModel::onConfirmacionChange,
            onCrear = viewModel::crear)
        else -> CuentaCreadaScreen(
            usuario = estado.creado?.usuario.orEmpty(),
            codigoRecuperacion = estado.creado?.codigoRecuperacion,
            onEntrar = { onTerminar(estado.creado?.usuario.orEmpty()) }) } }

// Pantalla de bienvenida con lo que hace la app.
@Composable
fun BienvenidaScreen(onComenzar: () -> Unit, modifier: Modifier = Modifier) {
    TarjetaCentral(modifier = modifier) {
        LogoAutoClimas(
            descripcion = stringResource(R.string.login_logo_description),
            modifier = Modifier.size(width = 220.dp, height = 150.dp))
        Spacer(modifier = Modifier.height(18.dp))
        TituloPrimerUso(
            titulo = stringResource(R.string.bienvenida_title),
            subtitulo = stringResource(R.string.bienvenida_subtitle))
        Spacer(modifier = Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Funcion(Icons.Outlined.CheckBox, R.string.bienvenida_feature_register_title,
                R.string.bienvenida_feature_register_text)
            Funcion(Icons.Outlined.PhotoCamera, R.string.bienvenida_feature_evidence_title,
                R.string.bienvenida_feature_evidence_text)
            Funcion(Icons.Outlined.Description, R.string.bienvenida_feature_delivery_title,
                R.string.bienvenida_feature_delivery_text) }
        Spacer(modifier = Modifier.height(24.dp))
        BotonPrincipal(texto = stringResource(R.string.bienvenida_start), onClick = onComenzar)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = stringResource(R.string.bienvenida_next_step),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant) } }

// Pantalla para crear el primer usuario.
@Composable
fun CrearPrimerUsuarioScreen(
    estado: NuevoUsuarioUiState,
    onNombreChange: (String) -> Unit,
    onUsuarioChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmacionChange: (String) -> Unit,
    onCrear: () -> Unit,
    modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    TarjetaCentral(modifier = modifier) {
        LogoAutoClimas(
            descripcion = stringResource(R.string.login_logo_description),
            modifier = Modifier.size(width = 150.dp, height = 102.dp))
        Spacer(modifier = Modifier.height(12.dp))
        TituloPrimerUso(
            titulo = stringResource(R.string.primer_uso_title),
            subtitulo = stringResource(R.string.primer_uso_subtitle))
        Spacer(modifier = Modifier.height(18.dp))
        CamposUsuario(
            estado = estado,
            onNombreChange = onNombreChange,
            onUsuarioChange = onUsuarioChange,
            onPasswordChange = onPasswordChange,
            onConfirmacionChange = onConfirmacionChange,
            onEnviar = onCrear)
        estado.errorGeneral?.let { mensaje ->
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = stringResource(mensaje), color = colors.error, style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
        Spacer(modifier = Modifier.height(20.dp))
        BotonPrincipal(
            texto = stringResource(if (estado.guardando) R.string.usuarios_saving else R.string.primer_uso_create),
            onClick = onCrear, habilitado = estado.puedeCrear, cargando = estado.guardando) } }

// Confirmación de que el usuario quedó creado.
@Composable
fun CuentaCreadaScreen(
    usuario: String,
    onEntrar: () -> Unit,
    modifier: Modifier = Modifier,
    codigoRecuperacion: String? = null) {
    val colors = MaterialTheme.colorScheme
    val verde = colorCumplido()
    TarjetaCentral(modifier = modifier) {
        LogoAutoClimas(
            descripcion = stringResource(R.string.login_logo_description),
            modifier = Modifier.size(width = 132.dp, height = 90.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape).background(verde.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = verde,
                modifier = Modifier.size(40.dp)) }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.primer_uso_created_title),
            style = MaterialTheme.typography.headlineSmall, color = colors.onSurface, textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        Spacer(modifier = Modifier.height(8.dp))
        // El usuario se resalta en negritas dentro de la frase.
        val frase = stringResource(R.string.primer_uso_created_text, usuario)
        Text(
            text = buildAnnotatedString {
                append(frase)
                val inicio = frase.indexOf(usuario)
                if (usuario.isNotEmpty() && inicio >= 0) {
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), inicio, inicio + usuario.length) } },
            style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        if (codigoRecuperacion != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.primer_uso_code_title), style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            CodigoRecuperacionCard(codigoRecuperacion)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.primer_uso_code_text), style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant, textAlign = TextAlign.Center) }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(colors.primaryContainer).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top) {
            Icon(imageVector = Icons.Filled.Info, contentDescription = null, tint = colors.primary,
                modifier = Modifier.size(20.dp))
            Text(text = stringResource(R.string.primer_uso_created_hint), style = MaterialTheme.typography.bodyMedium,
                color = colors.onPrimaryContainer) }
        Spacer(modifier = Modifier.height(24.dp))
        BotonPrincipal(texto = stringResource(R.string.primer_uso_enter), onClick = onEntrar) } }

// Fondo degradado con una tarjeta centrada, igual que el inicio de sesión. Se desplaza si el teclado tapa contenido.
@Composable
internal fun TarjetaCentral(modifier: Modifier = Modifier, contenido: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes
    Box(modifier = modifier.fillMaxSize().background(brush = AutoClimasGradients.loginBackground)
        .safeDrawingPadding().imePadding()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Card(
                shape = shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = colors.surface, contentColor = colors.onSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = shapes.extraLarge)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    contenido() } } } } }

@Composable
internal fun TituloPrimerUso(titulo: String, subtitulo: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = titulo, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center)
        Text(text = subtitulo, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center) } }

// Una de las tres funciones que se presentan en la bienvenida.
@Composable
private fun Funcion(icono: ImageVector, titulo: Int, texto: Int) {
    val colors = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(colors.primaryContainer),
            contentAlignment = Alignment.Center) {
            Icon(imageVector = icono, contentDescription = null, tint = colors.primary, modifier = Modifier.size(24.dp)) }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = stringResource(titulo), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
            Text(text = stringResource(texto), style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant) } } }

// Botón de ancho completo con el degradado de la app; si no está habilitado se ve apagado.
@Composable
internal fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cargando: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes
    Button(
        onClick = onClick, enabled = habilitado, shape = shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = AutoClimasWhite,
            disabledContainerColor = colors.surfaceVariant,
            disabledContentColor = colors.onSurfaceVariant),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp).clip(shapes.small)
            .then(if (habilitado) Modifier.background(brush = AutoClimasGradients.primaryButton) else Modifier)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colors.primary, strokeWidth = 2.dp) }
            Text(text = texto, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center) } } }

@Preview(name = "Bienvenida", showBackground = true, widthDp = 400, heightDp = 850)
@Composable
private fun BienvenidaPreview() {
    MyApplicationTheme(darkTheme = false) { BienvenidaScreen(onComenzar = {}) } }

@Preview(name = "Crear usuario · errores", showBackground = true, widthDp = 400, heightDp = 850)
@Composable
private fun CrearPrimerUsuarioPreview() {
    MyApplicationTheme(darkTheme = false) {
        CrearPrimerUsuarioScreen(
            estado = NuevoUsuarioUiState(nombre = "Erik Hernández", usuario = "erik", password = "1234",
                confirmacion = "12345"),
            onNombreChange = {}, onUsuarioChange = {}, onPasswordChange = {}, onConfirmacionChange = {},
            onCrear = {}) } }

@Preview(name = "Cuenta creada · oscuro", showBackground = true, widthDp = 400, heightDp = 850)
@Composable
private fun CuentaCreadaPreview() {
    MyApplicationTheme(darkTheme = true) {
        CuentaCreadaScreen(usuario = "erik", onEntrar = {}, codigoRecuperacion = "K7QM-2XPD-9TVC") } }
