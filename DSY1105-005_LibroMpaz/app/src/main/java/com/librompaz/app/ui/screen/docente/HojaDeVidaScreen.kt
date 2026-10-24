package com.librompaz.app.ui.screen.docente

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.model.Estudiante
import com.librompaz.app.model.TipoAnotacion
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.ui.components.MensajeAnimado
import com.librompaz.app.ui.components.SelectorDesplegable
import com.librompaz.app.ui.components.soporteError
import com.librompaz.app.viewmodel.LibroMpazViewModel

@Composable
fun HojaDeVidaScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val cursoId by viewModel.cursoSeleccionadoId.collectAsState()
    val usuario by viewModel.usuarioActivo.collectAsState()
    val todas by viewModel.anotaciones.collectAsState()
    val estudiantes = viewModel.estudiantesDelCurso(cursoId)

    var estudiante by remember { mutableStateOf<Estudiante?>(null) }
    var tipo by remember { mutableStateOf<TipoAnotacion?>(null) }
    var descripcion by remember { mutableStateOf("") }
    var intentado by remember { mutableStateOf(false) }
    var confirmando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    val historial = remember(todas, estudiante) { viewModel.anotacionesDe(estudiante?.id) }

    val errorEstudiante = if (intentado) viewModel.validarEstudiante(estudiante) else null
    val errorTipo = if (intentado) viewModel.validarTipoAnotacion(tipo) else null
    val errorDescripcion = if (intentado) viewModel.validarDescripcion(descripcion) else null

    Scaffold(topBar = { BarraSuperior("Hoja de vida", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SelectorDesplegable(
                etiqueta = "Estudiante",
                opciones = estudiantes,
                seleccion = estudiante,
                textoDe = { it.nombreFicticio },
                onSeleccion = {
                    estudiante = it
                    mensaje = null
                },
                textoVacio = "Elige un estudiante",
                error = errorEstudiante
            )
            SelectorDesplegable(
                etiqueta = "Tipo de anotación",
                opciones = TipoAnotacion.entries,
                seleccion = tipo,
                textoDe = { it.etiqueta },
                onSeleccion = { tipo = it },
                error = errorTipo
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                isError = errorDescripcion != null,
                supportingText = soporteError(errorDescripcion),
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Firma: ${usuario?.nombre.orEmpty()} · la fecha y hora se registran automáticamente.",
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    intentado = true
                    val sinErrores = viewModel.validarEstudiante(estudiante) == null &&
                        viewModel.validarTipoAnotacion(tipo) == null &&
                        viewModel.validarDescripcion(descripcion) == null
                    if (sinErrores) confirmando = true
                }
            ) { Text("Registrar anotación") }

            MensajeAnimado(mensaje)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Historial del estudiante (${historial.size})", style = MaterialTheme.typography.titleSmall)
            historial.forEach { anotacion ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(anotacion.tipo.etiqueta, style = MaterialTheme.typography.titleSmall)
                        Text(anotacion.descripcion, style = MaterialTheme.typography.bodySmall)
                        Text("${anotacion.fechaHora} · ${anotacion.firmadoPor}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }

    if (confirmando) {
        AlertDialog(
            onDismissRequest = { confirmando = false },
            title = { Text("¿Registrar anotación?") },
            text = { Text("Quedará firmada con tu usuario, fecha y hora, y no se podrá modificar.") },
            confirmButton = {
                TextButton(onClick = {
                    val registro = viewModel.registrarAnotacion(estudiante, tipo, descripcion.trim())
                    confirmando = false
                    if (registro) {
                        descripcion = ""
                        intentado = false
                        mensaje = "Anotación registrada y firmada."
                    }
                }) { Text("Registrar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmando = false }) { Text("Cancelar") }
            }
        )
    }
}
