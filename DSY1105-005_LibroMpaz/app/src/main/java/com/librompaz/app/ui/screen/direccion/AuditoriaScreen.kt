package com.librompaz.app.ui.screen.direccion

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.ui.components.MensajeAnimado
import com.librompaz.app.util.hoy
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Registro de auditoría: quién creó o modificó qué, y cuándo. Regla de negocio: toda creación o
 * modificación queda auditada de forma automática e inalterable (por eso esta pantalla es solo
 * de lectura).
 */
@Composable
fun AuditoriaScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val registros by viewModel.auditoria.collectAsState()
    var mensaje by remember { mutableStateOf<String?>(null) }
    var mensajeEsError by remember { mutableStateOf(false) }

    val exportar = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            val guardo = viewModel.exportarCsv(uri, viewModel.csvAuditoria(), "Auditoría exportada a CSV")
            mensajeEsError = !guardo
            mensaje = if (guardo) "Archivo CSV guardado en el teléfono." else "No se pudo guardar el archivo."
        }
    }

    Scaffold(topBar = { BarraSuperior("Auditoría", navController) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { exportar.launch("auditoria_${hoy()}.csv") }) {
                    Icon(Icons.Filled.Download, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Exportar auditoría (CSV)")
                }
            }
            item { MensajeAnimado(mensaje, esError = mensajeEsError) }
            items(registros, key = { it.id }) { registro ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .animateItem()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${registro.accion} · ${registro.detalle}", style = MaterialTheme.typography.titleSmall)
                        Text("${registro.usuario} (${registro.perfil.etiqueta})", style = MaterialTheme.typography.bodySmall)
                        Text(registro.fechaHora, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
