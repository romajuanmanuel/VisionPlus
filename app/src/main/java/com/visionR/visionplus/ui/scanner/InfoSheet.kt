package com.visionR.visionplus.ui.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoSheet(state: InfoState, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (state) {
                is InfoState.Loading -> {
                    Text(state.name, style = MaterialTheme.typography.headlineSmall)
                    CircularProgressIndicator()
                }

                is InfoState.Loaded -> {
                    val info = state.info
                    Text(info.title, style = MaterialTheme.typography.headlineSmall)
                    info.description?.let {
                        Text(it, style = MaterialTheme.typography.titleSmall)
                    }
                    Text(info.summary, style = MaterialTheme.typography.bodyMedium)
                    if (info.language != "es") {
                        Text(
                            "Resumen disponible solo en inglés.",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Text("Fuente: Wikipedia (CC BY-SA)", style = MaterialTheme.typography.labelSmall)
                    info.pageUrl?.let { url ->
                        val uriHandler = LocalUriHandler.current
                        TextButton(onClick = { uriHandler.openUri(url) }) {
                            Text("Ver en Wikipedia")
                        }
                    }
                }

                is InfoState.NotFound -> {
                    Text(state.name, style = MaterialTheme.typography.headlineSmall)
                    Text("No encontré información sobre este objeto.")
                }

                is InfoState.Error -> {
                    Text(state.name, style = MaterialTheme.typography.headlineSmall)
                    Text("No se pudo consultar Wikipedia. Revisa tu conexión a internet e inténtalo de nuevo.")
                }
            }
        }
    }
}
