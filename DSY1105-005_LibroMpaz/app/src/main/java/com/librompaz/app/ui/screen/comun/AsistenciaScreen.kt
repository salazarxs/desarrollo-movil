package com.librompaz.app.ui.screen.comun

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.model.EstadoAsistencia
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.ui.components.CampoFecha
import com.librompaz.app.ui.components.MensajeAnimado
import com.librompaz.app.util.hoy
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Módulo "Asistencia": registro diario del curso con cuatro estados (P presente, A ausente,
 * T atrasado, J justificado). Pensada para resolverse en menos de un minuto: todos parten
 * "presente" y solo se toca a quienes no lo están. La usan el Docente (su curso) e Inspectoría
 * (cualquier curso).
 *
 * Pide confirmación antes de guardar porque puede sobrescribir un registro (RNF del caso).
 */
@Composable
fun AsistenciaScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val cursoId by viewModel.cursoSeleccionadoId.collectAsState()
    val todas by viewModel.asistencias.collectAsState()
    val curso = viewModel.cursoPorId(cursoId)
    val estudiantes = viewModel.estudiantesDelCurso(cursoId)

    var fecha by remember { mutableStateOf(hoy()) }
    val guardada = remember(todas, cursoId, fecha) { viewModel.asistenciaGuardada(cursoId.orEmpty(), fecha) }
    var estados by remember(guardada) { mutableStateOf(guardada) }
    var confirmando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var mensajeEsError by remember { mutableStateOf(false) }
    val errorFecha = viewModel.validarFecha(fecha)

    Scaffold(topBar = { BarraSuperior("Asistencia · ${curso?.nombre.orEmpty()}", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            CampoFecha(
                valor = fecha,
                onCambio = {
                    fecha = it
                    mensaje = null
                },
                error = errorFecha,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            Text(
                "P = presente · A = ausente · T = atrasado · J = justificado",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            if (guardada.isNotEmpty()) {
                Text(
                    "Ya hay asistencia registrada en esta fecha. Si guardas, se sobrescribe.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(estudiantes) { estudiante ->
                    val actual = estados[estudiante.id] ?: EstadoAsistencia.PRESENTE
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(estudiante.nombreFicticio, modifier = Modifier.weight(1f))
                        EstadoAsistencia.entries.forEach { opcion ->
                            BotonEstado(
                                estado = opcion,
                                seleccionado = actual == opcion,
                                onClick = { estados = estados + (estudiante.id to opcion) }
                            )
                        }
                    }
                }
            }

            MensajeAnimado(mensaje, esError = mensajeEsError)

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                enabled = estudiantes.isNotEmpty(),
                onClick = {
                    if (errorFecha != null) {
                        mensajeEsError = true
                        mensaje = "Corrige la fecha antes de guardar."
                    } else {
                        confirmando = true
                    }
                }
            ) { Text("Guardar asistencia") }
        }
    }

    if (confirmando) {
        AlertDialog(
            onDismissRequest = { confirmando = false },
            title = { Text("¿Guardar asistencia?") },
            text = { Text("Si ya existía un registro para esta fecha, se va a sobrescribir.") },
            confirmButton = {
                TextButton(onClick = {
                    // Los estudiantes que no se tocaron quedan como PRESENTE.
                    val completo = estudiantes.associate { it.id to (estados[it.id] ?: EstadoAsistencia.PRESENTE) }
                    val guardo = viewModel.guardarAsistencia(cursoId.orEmpty(), fecha, completo)
                    confirmando = false
                    mensajeEsError = !guardo
                    mensaje = if (guardo) "Asistencia guardada." else "No se pudo guardar la asistencia."
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmando = false }) { Text("Cancelar") }
            }
        )
    }
}

/** Botón compacto de un estado (relleno si está elegido). */
@Composable
private fun BotonEstado(estado: EstadoAsistencia, seleccionado: Boolean, onClick: () -> Unit) {
    val colorEstado = when (estado) {
        EstadoAsistencia.PRESENTE -> MaterialTheme.colorScheme.primary
        EstadoAsistencia.AUSENTE -> MaterialTheme.colorScheme.error
        EstadoAsistencia.ATRASADO -> MaterialTheme.colorScheme.tertiary
        EstadoAsistencia.JUSTIFICADO -> MaterialTheme.colorScheme.secondary
    }
    val fondo by animateColorAsState(if (seleccionado) colorEstado else Color.Transparent, label = "fondo")
    val texto by animateColorAsState(if (seleccionado) Color.White else colorEstado, label = "texto")
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .padding(start = 4.dp)
            .size(40.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = fondo, contentColor = texto)
    ) { Text(estado.abreviatura) }
}
