package produkcja.meble.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// ============================================================================
// BAZOWE KOLORY APLIKACJI
// ============================================================================

private val LightColors = lightColorScheme(
    primary = Color(0xFF6E3E20),            // Ciepłe naturalne drewno
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDCC9),   // Jasny piasek
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

// ============================================================================
// STYL 1: CIEPŁE DREWNO (PRODUKTY) VS GRANAT / STALOWY BŁĘKIT (ZAMÓWIENIA)
// ============================================================================

// Produkty - Ciepłe drewno / Amber wood
val ProductWoodPrimaryLight = Color(0xFF6E3E20)
val ProductWoodPrimaryDark = Color(0xFFFFB68F)
val ProductWoodContainerLight = Color(0xFFFFE0D0)
val ProductWoodContainerDark = Color(0xFF532B12)
val ProductTopBarLight = Color(0xFFF5EAE1)
val ProductTopBarDark = Color(0xFF251A14)
val ProductListBackgroundLight = Color(0xFFF4ECE4)
val ProductListBackgroundDark = Color(0xFF1B130E)

// Zamówienia - Granat / Stalowy błękit (Deep Navy / Slate)
val OrderNavyPrimaryLight = Color(0xFF1A4971)
val OrderNavyPrimaryDark = Color(0xFFA1C7F4)
val OrderNavyContainerLight = Color(0xFFD7E5F6)
val OrderNavyContainerDark = Color(0xFF103353)
val OrderTopBarLight = Color(0xFFE2EAF4)
val OrderTopBarDark = Color(0xFF14202E)
val OrderListBackgroundLight = Color(0xFFEFF4F9)
val OrderListBackgroundDark = Color(0xFF0F1722)
val OrderCardBackgroundLight = Color(0xFFFFFFFF)
val OrderCardBackgroundDark = Color(0xFF182230)

@Immutable
data class SectionStyle(
    val primary: Color,
    val onPrimary: Color,
    val container: Color,
    val onContainer: Color,
    val topBarContainer: Color,
    val topBarContent: Color,
    val background: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val fabColor: Color,
    val fabContentColor: Color,
    val navIndicatorColor: Color,
    val navSelectedColor: Color,
)

val SectionStyle.accent: Color get() = primary
val SectionStyle.topBarTitle: Color get() = topBarContent
val SectionStyle.topBarAction: Color get() = topBarContent
val SectionStyle.fabContainer: Color get() = fabColor
val SectionStyle.fabContent: Color get() = fabContentColor
val SectionStyle.navBarContainer: Color get() = background
val SectionStyle.navBarIndicator: Color get() = navIndicatorColor
val SectionStyle.navBarSelectedItem: Color get() = navSelectedColor
val SectionStyle.navBarUnselectedItem: Color get() = onContainer.copy(alpha = 0.7f)

@Composable
@ReadOnlyComposable
fun productSectionStyle(darkTheme: Boolean = isSystemInDarkTheme()): SectionStyle {
    return if (darkTheme) {
        SectionStyle(
            primary = ProductWoodPrimaryDark,
            onPrimary = Color(0xFF4A1F05),
            container = ProductWoodContainerDark,
            onContainer = Color(0xFFFFDBCA),
            topBarContainer = ProductTopBarDark,
            topBarContent = ProductWoodPrimaryDark,
            background = ProductListBackgroundDark,
            cardBackground = Color(0xFF261D18),
            cardBorder = ProductWoodContainerDark,
            fabColor = ProductWoodPrimaryDark,
            fabContentColor = Color(0xFF4A1F05),
            navIndicatorColor = ProductWoodContainerDark,
            navSelectedColor = ProductWoodPrimaryDark,
        )
    } else {
        SectionStyle(
            primary = ProductWoodPrimaryLight,
            onPrimary = Color.White,
            container = ProductWoodContainerLight,
            onContainer = Color(0xFF401C06),
            topBarContainer = ProductTopBarLight,
            topBarContent = ProductWoodPrimaryLight,
            background = ProductListBackgroundLight,
            cardBackground = Color.White,
            cardBorder = Color(0xFFE8D7CA),
            fabColor = ProductWoodPrimaryLight,
            fabContentColor = Color.White,
            navIndicatorColor = ProductWoodContainerLight,
            navSelectedColor = ProductWoodPrimaryLight,
        )
    }
}

@Composable
@ReadOnlyComposable
fun orderSectionStyle(darkTheme: Boolean = isSystemInDarkTheme()): SectionStyle {
    return if (darkTheme) {
        SectionStyle(
            primary = OrderNavyPrimaryDark,
            onPrimary = Color(0xFF04284A),
            container = OrderNavyContainerDark,
            onContainer = Color(0xFFD6E4F6),
            topBarContainer = OrderTopBarDark,
            topBarContent = OrderNavyPrimaryDark,
            background = OrderListBackgroundDark,
            cardBackground = OrderCardBackgroundDark,
            cardBorder = Color(0xFF20364F),
            fabColor = OrderNavyPrimaryDark,
            fabContentColor = Color(0xFF04284A),
            navIndicatorColor = OrderNavyContainerDark,
            navSelectedColor = OrderNavyPrimaryDark,
        )
    } else {
        SectionStyle(
            primary = OrderNavyPrimaryLight,
            onPrimary = Color.White,
            container = OrderNavyContainerLight,
            onContainer = Color(0xFF072745),
            topBarContainer = OrderTopBarLight,
            topBarContent = OrderNavyPrimaryLight,
            background = OrderListBackgroundLight,
            cardBackground = OrderCardBackgroundLight,
            cardBorder = Color(0xFFCBDDF0),
            fabColor = OrderNavyPrimaryLight,
            fabContentColor = Color.White,
            navIndicatorColor = OrderNavyContainerLight,
            navSelectedColor = OrderNavyPrimaryLight,
        )
    }
}

@Composable
@ReadOnlyComposable
fun productListBackgroundColor(darkTheme: Boolean = isSystemInDarkTheme()): Color {
    return if (darkTheme) ProductListBackgroundDark else ProductListBackgroundLight
}

@Composable
@ReadOnlyComposable
fun orderListBackgroundColor(darkTheme: Boolean = isSystemInDarkTheme()): Color {
    return if (darkTheme) OrderListBackgroundDark else OrderListBackgroundLight
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
