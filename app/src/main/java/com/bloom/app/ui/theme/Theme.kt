package com.bloom.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = SurfaceSoft,
    primaryContainer = PinkPrimaryContainer,
    onPrimaryContainer = TextDark,
    secondary = LavenderSecondary,
    secondaryContainer = LavenderContainer,
    onSecondaryContainer = TextDark,
    tertiary = MintTertiary,
    tertiaryContainer = MintContainer,
    onTertiaryContainer = TextDark,
    background = BackgroundSoft,
    onBackground = TextDark,
    surface = SurfaceSoft,
    onSurface = TextDark,
    surfaceVariant = SurfaceVariantSoft,
    onSurfaceVariant = TextDark
)

private val DarkColors = darkColorScheme(
    primary = PinkPrimary,
    onPrimary = TextDark,
    primaryContainer = PinkPrimaryContainer.copy(alpha = 0.3f),
    onPrimaryContainer = TextLight,
    secondary = LavenderSecondary,
    tertiary = MintTertiary,
    background = BackgroundDark,
    onBackground = TextLight,
    surface = SurfaceDark,
    onSurface = TextLight
)

private val BloomTypography = Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp)
)

@Composable
fun BloomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = BloomTypography,
        content = content
    )
}
