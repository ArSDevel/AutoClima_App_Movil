package com.example.myapplication.ui.usuarios

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import kotlinx.coroutines.launch

// Muestra un código de recuperación grande y legible, con un botón para copiarlo.
@Composable
internal fun CodigoRecuperacionCard(codigo: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val portapapeles = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var copiado by remember(codigo) { mutableStateOf(false) }
    val etiqueta = stringResource(R.string.usuarios_code_title)
    Column(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(colors.primaryContainer)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SelectionContainer {
            Text(text = codigo, color = colors.onPrimaryContainer, textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)) }
        OutlinedButton(onClick = {
            scope.launch {
                portapapeles.setClipEntry(ClipEntry(ClipData.newPlainText(etiqueta, codigo)))
                copiado = true } }) {
            Icon(imageVector = if (copiado) Icons.Filled.Check else Icons.Outlined.ContentCopy,
                contentDescription = null, modifier = Modifier.size(18.dp))
            Text(text = stringResource(if (copiado) R.string.usuarios_code_copied else R.string.usuarios_code_copy),
                modifier = Modifier.padding(start = 8.dp)) } } }
