package com.librompaz.app.ui.screen.docente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.model.Evaluacion
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.ui.components.CampoFecha
import com.librompaz.app.ui.components.MensajeAnimado
import com.librompaz.app.ui.components.SelectorDesplegable
import com.librompaz.app.ui.components.soporteError
import com.librompaz.app.util.hoy
import com.librompaz.app.viewmodel.LibroMpazViewModel

@Composable
fun CalificacionesScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val cursoId by viewModel.cursoSeleccionadoId.collectAsState()
    val asignaturaId by viewModel.asignaturaSeleccionadaId.collectAsState()
    val todas by viewModel.evaluaciones.collectAsState()
    val evaluaciones = remember(todas, cursoId, asignaturaId) { viewModel.evaluacionesDe(cursoId, asignaturaId) }
    val estudiantes = viewModel.estudiantesDelCurso(cursoId)

    var evaluacion by remember { mutableStateOf<Evaluacion?>(null) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevaFecha by remember { mutableStateOf(hoy()) }
    var intentoCrear by remember { mutableStateOf(false) }
    var notas by remember(evaluacion) {
        mutableStateOf(viewModel.calificacionesDe(evaluacion?.id).associate { it.estudianteId to it.nota.toString().replace('.', ',') })
    }
    var confirmando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var mensajeEsError by remember { mutableStateOf(false) }

    val errorNombre = if (intentoCrear) viewModel.validarNombreInstrumento(nuevoNombre) else null
    val errorFecha = if (intentoCrear) viewModel.validarFecha(nuevaFecha) else null

    Scaffold(topBar = { BarraSuperior("Calificaciones", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Nueva evaluación", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = nuevoNombre,
                onValueChange = { nuevoNombre = it },
                label = { Text("Nombre del instrumento") },
                isError = errorNombre != null,
                supportingText = soporteError(errorNombre),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoFecha(
                    valor = nuevaFecha,
                    onCambio = { nuevaFecha = it },
                    etiqueta = "Fecha",
                    error = errorFecha,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = {
                        intentoCrear = true
                        val creada = viewModel.crearEvaluacion(cursoId.orEmpty(), asignaturaId.orEmpty(), nuevoNombre.trim(), nuevaFecha)
                        mensajeEsError = !creada
                        mensaje = if (creada) "Evaluación \"${nuevoNombre.trim()}\" creada." else "Revisa los datos de la evaluación."
                        if (creada) {
                            nuevoNombre = ""
                            intentoCrear = false
                        }
                    }
                ) { Text("Crear") }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SelectorDesplegable(
                etiqueta = "Evaluación",
                opciones = evaluaciones,
                seleccion = evaluacion,
                textoDe = { "${it.nombreInstrumento} (${it.fecha})" },
                onSeleccion = {
                    evaluacion = it
                    mensaje = null
                },
                textoVacio = "Elige una evaluación"
            )

            if (evaluacion != null) {
                estudiantes.forEach { estudiante ->
                    val texto = notas[estudiante.id].orEmpty()
                    val error = if (texto.isBlank()) null else viewModel.validarNota(texto)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(estudiante.nombreFicticio, modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            value = texto,
                            onValueChange = { notas = notas + (estudiante.id to it) },
                            label = { Text("Nota") },
                            singleLine = true,
                            isError = error != null,
                            supportingText = soporteError(error),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.width(160.dp)
                        )
                    }
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val errorPlanilla = viewModel.validarPlanillaNotas(notas)
                        if (errorPlanilla != null) {
                            mensajeEsError = true
                            mensaje = errorPlanilla
                        } else {
                            confirmando = true
                        }
                    }
                ) { Text("Guardar notas") }
            }

            MensajeAnimado(mensaje, esError = mensajeEsError)
        }
    }

    if (confirmando) {
        AlertDialog(
            onDismissRequest = { confirmando = false },
            title = { Text("¿Guardar notas?") },
            text = { Text("Las notas que ya existían para esta evaluación se van a sobrescribir.") },
            confirmButton = {
                TextButton(onClick = {
                    val guardo = evaluacion?.let { viewModel.guardarCalificaciones(it.id, notas) } ?: false
                    confirmando = false
                    mensajeEsError = !guardo
                    mensaje = if (guardo) "Notas guardadas." else "No se pudieron guardar las notas."
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmando = false }) { Text("Cancelar") }
            }
        )
    }
}
