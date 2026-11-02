package com.duoc.prestaaula.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Identidad visual de PrestaAula: fondo azul noche y acentos azul cielo.
 * Siempre se aplica, independientemente del modo claro u oscuro del teléfono.
 */
private val PrestaAulaDarkColors = darkColorScheme(
    primary = Color(0xFF8ED0FF),
    onPrimary = Color(0xFF002F4D),
    primaryContainer = Color(0xFF153B59),
    onPrimaryContainer = Color(0xFFD8EDFF),
    inversePrimary = Color(0xFF21658B),

    secondary = Color(0xFFB1DFFF),
    onSecondary = Color(0xFF17354A),
    secondaryContainer = Color(0xFF244961),
    onSecondaryContainer = Color(0xFFD4EEFF),

    tertiary = Color(0xFF8BE5F5),
    onTertiary = Color(0xFF003942),
    tertiaryContainer = Color(0xFF1B515C),
    onTertiaryContainer = Color(0xFFC7F5FF),

    background = Color(0xFF0B1422),
    onBackground = Color(0xFFE8F3FF),
    surface = Color(0xFF111E2E),
    onSurface = Color(0xFFE8F3FF),
    surfaceVariant = Color(0xFF263A50),
    onSurfaceVariant = Color(0xFFBECFE0),
    surfaceTint = Color(0xFF8ED0FF),
    surfaceDim = Color(0xFF0B1422),
    surfaceBright = Color(0xFF32485D),
    surfaceContainerLowest = Color(0xFF0B1422),
    surfaceContainerLow = Color(0xFF162538),
    surfaceContainer = Color(0xFF1B2C40),
    surfaceContainerHigh = Color(0xFF23374C),
    surfaceContainerHighest = Color(0xFF2B4056),

    outline = Color(0xFF829CB5),
    outlineVariant = Color(0xFF3C536B),
    error = Color(0xFFFFB6AE),
    onError = Color(0xFF650F12),
    errorContainer = Color(0xFF73282D),
    onErrorContainer = Color(0xFFFFDAD7),
    inverseSurface = Color(0xFFE8F3FF),
    inverseOnSurface = Color(0xFF1B2C40),
    scrim = Color.Black
)

/** Tonos claros que permiten distinguir estados sobre superficies oscuras. */
object StatusColors {
    val Success = Color(0xFF8DE5B2)
    val Warning = Color(0xFFFFC97A)
    val Danger = Color(0xFFFFA6AC)
    val Info = Color(0xFF8ED0FF)
    val Neutral = Color(0xFFB8C5D4)
}

@Composable
fun PrestaAulaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PrestaAulaDarkColors,
        typography = Typography(),
        content = content
    )
}
