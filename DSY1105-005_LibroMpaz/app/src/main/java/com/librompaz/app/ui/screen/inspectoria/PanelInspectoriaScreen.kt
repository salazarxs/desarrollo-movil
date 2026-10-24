package com.librompaz.app.ui.screen.inspectoria

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.navigation.Rutas
import com.librompaz.app.ui.components.BarraProgreso
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.viewmodel.LibroMpazViewModel

@Composable
fun PanelInspectoriaScreen(
    navController: NavHostController,
    viewModel: LibroMpazViewModel,
    onCerrarSesion: () -> Unit
) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val cursos by viewModel.cursos.collectAsState()
    val asistencias by viewModel.asistencias.collectAsState()

    Scaffold(topBar = { BarraSuperior("Panel Inspectoría", navController, onCerrarSesion) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Hola, ${usuario?.nombre.orEmpty()}", style = MaterialTheme.typography.titleLarge)

            Button(modifier = Modifier.fillMaxWidth(), onClick = { navController.navigate(Rutas.INSP_ALERTAS) }) {
                Text("Alertas: inasistencias y hoja de vida")
            }

            Text("Asistencia por curso", style = MaterialTheme.typography.titleSmall)
            cursos.forEach { curso ->
                val porcentaje = remember(asistencias, curso.id) { viewModel.porcentajeAsistencia(curso.id) }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(curso.nombre, style = MaterialTheme.typography.titleMedium)
                        Text("Asistencia: ${porcentaje?.let { "$it %" } ?: "—"}", style = MaterialTheme.typography.bodyMedium)
                        BarraProgreso((porcentaje ?: 0) / 100f)
                        OutlinedButton(onClick = {
                            viewModel.seleccionarCurso(curso.id)
                            navController.navigate(Rutas.ASISTENCIA)
                        }) { Text("Ver o gestionar asistencia") }
                    }
                }
            }
        }
    }
}
