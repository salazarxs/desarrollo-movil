package com.librompaz.app.ui.screen.docente

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.librompaz.app.ui.components.BarraSuperior
import com.librompaz.app.ui.components.CampoFecha
import com.librompaz.app.ui.components.MensajeAnimado
import com.librompaz.app.ui.components.TextoError
import com.librompaz.app.ui.components.soporteError
import com.librompaz.app.util.hoy
import com.librompaz.app.viewmodel.LibroMpazViewModel

/**
 * Módulo "Cobertura curricular" (docente): registrar los OA y contenidos trabajados en una
 * sesión o bloque, y ver las sesiones ya registradas.
 */
@Composable
fun RegistroClaseScreen(navController: NavHostController, viewModel: LibroMpazViewModel) {
    val cursoId by viewModel.cursoSeleccionadoId.collectAsState()
    val asignaturaId by viewModel.asignaturaSeleccionadaId.collectAsState()
    val todas by viewModel.sesiones.collectAsState()
    val objetivos = viewModel.objetivosDe(asignaturaId)
    val registradas = remember(todas, cursoId, asignaturaId) { viewModel.sesionesDe(cursoId, asignaturaId) }

    var fecha by remember { mutableStateOf(hoy()) }
    var bloque by remember { mutableStateOf("") }
    var seleccionados by remember { mutableStateOf(setOf<String>()) }
    var contenido by remember { mutableStateOf("") }
    var intentado by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var mensajeEsError by remember { mutableStateOf(false) }

    val errorFecha = if (intentado) viewModel.validarFecha(fecha) else null
    val errorBloque = if (intentado) viewModel.validarBloque(bloque) else null
    val errorOas = if (intentado) viewModel.validarOas(seleccionados) else null
    val errorContenido = if (intentado) viewModel.validarContenido(contenido) else null

    Scaffold(topBar = { BarraSuperior("Registro de clase", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoFecha(
                    valor = fecha,
                    onCambio = { fecha = it },
                    etiqueta = "Fecha",
                    error = errorFecha,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bloque,
                    onValueChange = { bloque = it },
                    label = { Text("Bloque") },
                    isError = errorBloque != null,
                    supportingText = soporteError(errorBloque),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Objetivos de Aprendizaje trabajados", style = MaterialTheme.typography.titleSmall)
            objetivos.forEach { oa ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = oa.id in seleccionados,
                            role = Role.Checkbox,
                            onValueChange = { marcado ->
                                seleccionados = if (marcado) seleccionados + oa.id else seleccionados - oa.id
                            }
                        )
                        .padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = oa.id in seleccionados,
                        onCheckedChange = null,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Column {
                        Text(oa.codigo, style = MaterialTheme.typography.labelMedium)
                        Text(oa.descripcion, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (errorOas != null) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.error) {
                    TextoError(errorOas)
                }
            }

            OutlinedTextField(
                value = contenido,
                onValueChange = { contenido = it },
                label = { Text("Contenidos abordados") },
                isError = errorContenido != null,
                supportingText = soporteError(errorContenido),
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            MensajeAnimado(mensaje, esError = mensajeEsError)

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    intentado = true
                    val guardo = viewModel.guardarSesion(cursoId.orEmpty(), asignaturaId.orEmpty(), fecha, bloque.trim(), seleccionados.toList(), contenido.trim())
                    mensajeEsError = !guardo
                    mensaje = if (guardo) "Clase guardada." else "Revisa los campos marcados."
                    if (guardo) {
                        bloque = ""
                        seleccionados = emptySet()
                        contenido = ""
                        intentado = false
                    }
                }
            ) { Text("Guardar clase") }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Clases registradas (${registradas.size})", style = MaterialTheme.typography.titleSmall)
            registradas.forEach { sesion ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${sesion.fecha} · ${sesion.bloque}", style = MaterialTheme.typography.titleSmall)
                        Text(sesion.contenido, style = MaterialTheme.typography.bodySmall)
                        Text("OA: ${sesion.oaIds.size} · Registró: ${sesion.registradoPor}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
