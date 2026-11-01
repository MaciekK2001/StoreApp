package produkcja.meble.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF5C3D2E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCA),
    onPrimaryContainer = Color(0xFF3C2014),
    secondary = Color(0xFF705B4F),
    secondaryContainer = Color(0xFFFBDCCB),
    background = Color(0xFFFFF8F5),
    surface = Color(0xFFFFF8F5),
    surfaceVariant = Color(0xFFF2DFD6),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB693),
    onPrimary = Color(0xFF55200B),
    primaryContainer = Color(0xFF71371F),
    secondary = Color(0xFFDEC2B3),
    background = Color(0xFF1A110E),
    surface = Color(0xFF241814),
    surfaceVariant = Color(0xFF35241D),
)

/**
 * Kolory tła dla zakładki produktów:
 * - Jasny motyw: ciepły piaskowy beż (odcień jasnego drewna), wyraźnie odróżniający się od kremowo-białego tła zamówień.
 * - Ciemny motyw: ciepły, głęboki odcień palonego drewna / espresso.
 */
val ProductListBackgroundLight = Color(0xFFF0E5DA)
val ProductListBackgroundDark = Color(0xFF1E1410)

@Composable
@ReadOnlyComposable
fun productListBackgroundColor(darkTheme: Boolean = isSystemInDarkTheme()): Color {
    return if (darkTheme) ProductListBackgroundDark else ProductListBackgroundLight
}

@Composable
fun MebleMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content,
    )
}
