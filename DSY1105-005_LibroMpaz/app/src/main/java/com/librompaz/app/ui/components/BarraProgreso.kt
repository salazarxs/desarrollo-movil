package com.librompaz.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun BarraProgreso(fraccion: Float, modifier: Modifier = Modifier) {
    var objetivo by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(fraccion) { objetivo = fraccion }
    val progreso by animateFloatAsState(targetValue = objetivo, animationSpec = tween(800), label = "progreso")
    LinearProgressIndicator(progress = { progreso }, modifier = modifier.fillMaxWidth())
}
