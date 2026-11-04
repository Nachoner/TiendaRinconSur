package com.example.rinconsur.view.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = AzulMarca,
    onPrimary = Color.White,
    primaryContainer = CelesteSuave,
    onPrimaryContainer = AzulNocheProfundo,
    secondary = CelesteMarca,
    onSecondary = Color.White,
    secondaryContainer = CelesteMuyClaro,
    onSecondaryContainer = AzulNocheProfundo,
    tertiary = AzulPetroleo,
    onTertiary = Color.White,
    tertiaryContainer = PetroleoSuave,
    onTertiaryContainer = AzulNocheProfundo,
    background = BlancoAzulado,
    onBackground = AzulNoche,
    surface = BlancoAzulado,
    onSurface = AzulNoche,
    surfaceVariant = GrisAzulado,
    onSurfaceVariant = GrisAzuladoTexto,
    outline = GrisAzuladoBorde,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

// Solo tema claro, para mantener la identidad de color. Se quitó el color dinámico
// de Android 12+, porque reemplazaría esta paleta por los colores del fondo de pantalla.
@Composable
fun RinconSurTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = Typography,
        content = content
    )
}