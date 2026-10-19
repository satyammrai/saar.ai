package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class SaarThemeMode {
    MINT_CREAM,
    LAVENDER,
    WARM_COCOA
}

data class ClayColors(
    val background: Color,
    val card: Color,
    val mint: Color,
    val peach: Color,
    val butter: Color,
    val sky: Color,
    val lavender: Color,
    val coral: Color,
    val deepGreen: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val glossyBorder: Color,
    val shadowColor: Color,
    val isDark: Boolean = false
)

val MintCreamClayColors = ClayColors(
    background = ClayCream,
    card = ClayCard,
    mint = ClayMint,
    peach = ClayPeach,
    butter = ClayButter,
    sky = ClaySky,
    lavender = ClayLavender,
    coral = ClayCoral,
    deepGreen = ClayDeepGreen,
    textPrimary = ClayInk,
    textSecondary = ClayInkSoft,
    glossyBorder = ClayBorderLight,
    shadowColor = ClayShadowOuter,
    isDark = false
)

val LavenderClayColors = ClayColors(
    background = ClayLavenderBg,
    card = ClayLavenderCard,
    mint = ClayMint,
    peach = ClayPeach,
    butter = ClayButter,
    sky = ClaySky,
    lavender = ClayLavenderAccent,
    coral = ClayCoral,
    deepGreen = ClayDeepGreen,
    textPrimary = ClayInk,
    textSecondary = ClayInkSoft,
    glossyBorder = ClayBorderLight,
    shadowColor = Color(0x22624D7A),
    isDark = false
)

val CocoaClayColors = ClayColors(
    background = ClayCocoaBg,
    card = ClayCocoaCard,
    mint = ClayCocoaMint,
    peach = ClayCocoaPeach,
    butter = Color(0xFFD6C084),
    sky = Color(0xFF98AEC4),
    lavender = Color(0xFFAC9BCA),
    coral = ClayCoral,
    deepGreen = Color(0xFF4A8F78),
    textPrimary = ClayCocoaInk,
    textSecondary = ClayCocoaInkSoft,
    glossyBorder = Color(0x26FFFFFF),
    shadowColor = Color(0x66000000),
    isDark = true
)

val LocalClayColors = staticCompositionLocalOf { MintCreamClayColors }

@Composable
fun SaarTheme(
    themeMode: SaarThemeMode = SaarThemeMode.MINT_CREAM,
    content: @Composable () -> Unit
) {
    val clayColors = when (themeMode) {
        SaarThemeMode.MINT_CREAM -> MintCreamClayColors
        SaarThemeMode.LAVENDER -> LavenderClayColors
        SaarThemeMode.WARM_COCOA -> CocoaClayColors
    }

    val colorScheme = if (clayColors.isDark) {
        darkColorScheme(
            primary = clayColors.coral,
            secondary = clayColors.mint,
            tertiary = clayColors.peach,
            background = clayColors.background,
            surface = clayColors.card,
            onPrimary = Color.White,
            onSecondary = clayColors.textPrimary,
            onTertiary = clayColors.textPrimary,
            onBackground = clayColors.textPrimary,
            onSurface = clayColors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = clayColors.coral,
            secondary = clayColors.mint,
            tertiary = clayColors.peach,
            background = clayColors.background,
            surface = clayColors.card,
            onPrimary = Color.White,
            onSecondary = clayColors.textPrimary,
            onTertiary = clayColors.textPrimary,
            onBackground = clayColors.textPrimary,
            onSurface = clayColors.textPrimary
        )
    }

    CompositionLocalProvider(LocalClayColors provides clayColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
