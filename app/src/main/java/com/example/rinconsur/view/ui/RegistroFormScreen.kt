package com.example.rinconsur.view.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rinconsur.view.utils.CampoFormulario
import com.example.rinconsur.view.utils.MensajeError
import com.example.rinconsur.view.utils.vibrarConfirmacion
import com.example.rinconsur.view.utils.vibrarError
import com.example.rinconsur.viewmodels.RegistroViewModel
import com.example.rinconsur.viewmodels.TurnoViewModel
import com.example.rinconsur.viewmodels.Validaciones

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegistroFormScreen(
    registroViewModel: RegistroViewModel,
    turnoViewModel: TurnoViewModel,
    registroId: Long,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val form by registroViewModel.form.collectAsStateWithLifecycle()
    val turno by turnoViewModel.turnoActivo.collectAsStateWithLifecycle()
    val mensaje by registroViewModel.message.collectAsStateWithLifecycle()
    val editando = registroId != 0L

    // Al entrar, el formulario parte vacío (nuevo) o con los datos del registro (editar)
    LaunchedEffect(registroId) {
        registroViewModel.nuevo()
        if (editando) registroViewModel.cargar(registroId)
    }

    // El teléfono vibra dos veces cuando hay errores en el formulario
    LaunchedEffect(form.rangoError, form.adultosError, form.ninosError, form.comproError) {
        if (form.rangoError != null || form.adultosError != null ||
            form.ninosError != null || form.comproError != null
        ) vibrarError(context)
    }

    // Tamaño del grupo en vivo, solo cuando las cantidades son válidas
    val adultos = form.adultos.trim().toIntOrNull()?.takeIf { it in 0..Validaciones.MAX_ACOMPANANTES }
    val ninos = form.ninos.trim().toIntOrNull()?.takeIf { it in 0..Validaciones.MAX_ACOMPANANTES }
    val totalGrupo = if (adultos != null && ninos != null) 1 + adultos + ninos else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (editando) "Editar salida" else "Nueva salida",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Registra a la persona que sale y a quienes van con ella.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        MensajeError(mensaje)

        // Rango etario
        Spacer(Modifier.height(4.dp))
        Text("Rango etario de la persona", style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Validaciones.RANGOS_ETARIOS.forEach { rango ->
                val seleccionado = form.rangoEtario == rango
                FilterChip(
                    selected = seleccionado,
                    onClick = { registroViewModel.onRangoChange(rango) },
                    label = { Text(rango) },
                    leadingIcon = if (seleccionado) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null
                )
            }
        }
        MensajeError(form.rangoError)

        // Acompañantes
        Spacer(Modifier.height(4.dp))
        Text("Acompañantes", style = MaterialTheme.typography.titleMedium)
        CampoFormulario(
            valor = form.adultos,
            onCambio = registroViewModel::onAdultosChange,
            etiqueta = "Otros adultos",
            icono = Icons.Default.Group,
            error = form.adultosError,
            teclado = KeyboardType.Number
        )
        CampoFormulario(
            valor = form.ninos,
            onCambio = registroViewModel::onNinosChange,
            etiqueta = "Niños",
            icono = Icons.Default.ChildCare,
            error = form.ninosError,
            teclado = KeyboardType.Number,
            imeAction = ImeAction.Done
        )

        // Tamaño del grupo, con número animado
        AnimatedVisibility(
            visible = totalGrupo != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Group, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    AnimatedContent(targetState = totalGrupo ?: 0, label = "grupo") { total ->
                        Text(
                            text = "Grupo de $total ${if (total == 1) "persona" else "personas"}",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }

        // Con bolsa / sin bolsa
        Spacer(Modifier.height(4.dp))
        Text("¿Sale con bolsa de la tienda?", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OpcionBolsa(
                texto = "Con bolsa",
                icono = Icons.Default.ShoppingBag,
                seleccionado = form.compro == true,
                onClick = { registroViewModel.onComproChange(true) },
                modifier = Modifier.weight(1f)
            )
            OpcionBolsa(
                texto = "Sin bolsa",
                icono = Icons.Default.RemoveShoppingCart,
                seleccionado = form.compro == false,
                onClick = { registroViewModel.onComproChange(false) },
                modifier = Modifier.weight(1f)
            )
        }
        MensajeError(form.comproError)

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                turno?.let { turnoActual ->
                    registroViewModel.guardar(turnoActual) {
                        vibrarConfirmacion(context)
                        onDone()
                    }
                }
            },
            enabled = turno != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (editando) "Guardar cambios" else "Guardar salida")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OpcionBolsa(
    texto: String,
    icono: ImageVector,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animación: el botón cambia de color suavemente al seleccionarse
    val fondo by animateColorAsState(
        targetValue = if (seleccionado) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "fondo"
    )
    val contenido by animateColorAsState(
        targetValue = if (seleccionado) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "contenido"
    )

    Surface(
        onClick = onClick,
        color = fondo,
        contentColor = contenido,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(76.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icono, contentDescription = null)
            Spacer(Modifier.height(4.dp))
            Text(text = texto, style = MaterialTheme.typography.labelLarge)
        }
    }
}