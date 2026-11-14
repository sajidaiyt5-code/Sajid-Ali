package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = SkyIndigo,
    onPrimary = Color(0xFF0A2459),
    primaryContainer = DeepIndigoContainer,
    onPrimaryContainer = Color(0xFFDCE6FF),
    secondary = MintThames,
    onSecondary = Color(0xFF00382F),
    secondaryContainer = DeepThamesContainer,
    onSecondaryContainer = Color(0xFFB5F4E6),
    tertiary = SoftCrimson,
    onTertiary = Color(0xFF5D0017),
    tertiaryContainer = DeepCrimsonContainer,
    onTertiaryContainer = Color(0xFFFFD9DE),
    background = MidnightBg,
    onBackground = Color(0xFFE7ECF7),
    surface = MidnightSurface,
    onSurface = Color(0xFFE7ECF7),
    surfaceVariant = MidnightSurfaceHigh,
    onSurfaceVariant = Color(0xFFBDC7DA),
    surfaceContainerLow = MidnightSurface,
    surfaceContainer = MidnightSurfaceHigh,
    surfaceContainerHigh = MidnightSurfaceHighest,
    outline = MidnightOutline,
    outlineVariant = MidnightOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalNavy,
    onPrimary = Color.White,
    primaryContainer = RoyalNavyContainer,
    onPrimaryContainer = OnRoyalNavyContainer,
    secondary = ThamesTeal,
    onSecondary = Color.White,
    secondaryContainer = ThamesTealContainer,
    onSecondaryContainer = OnThamesTealContainer,
    tertiary = GuardsCrimson,
    onTertiary = Color.White,
    tertiaryContainer = GuardsCrimsonContainer,
    onTertiaryContainer = OnGuardsCrimsonContainer,
    background = AlabasterBg,
    onBackground = Color(0xFF101828),
    surface = CrispSurface,
    onSurface = Color(0xFF101828),
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = Color(0xFF475467),
    surfaceContainerLow = Color(0xFFF9FAFD),
    surfaceContainer = Color(0xFFF0F4FA),
    surfaceContainerHigh = Color(0xFFE7EDF6),
    outline = SlateOutline,
    outlineVariant = SlateOutlineVariant
)

val UkPhoneShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun UkVirtualPhoneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = UkPhoneShapes,
        content = content
    )
}
