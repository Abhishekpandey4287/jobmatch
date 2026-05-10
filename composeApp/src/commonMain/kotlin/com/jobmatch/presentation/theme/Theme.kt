package com.jobmatch.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Brand colours ─────────────────────────────────────────────────────────────
// Indigo-based palette — professional, trustworthy, distinct from commodity blue

private val BrandIndigo = Color(0xFF4F46E5)
private val BrandViolet = Color(0xFF7C3AED)
private val BrandMint   = Color(0xFF10B981)
private val BrandRose   = Color(0xFFEF4444)

// ── Light scheme ──────────────────────────────────────────────────────────────

internal val LightColors = lightColorScheme(
    primary              = BrandIndigo,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFEEF2FF),
    onPrimaryContainer   = Color(0xFF312E81),
    secondary            = BrandViolet,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFF5F3FF),
    onSecondaryContainer = Color(0xFF4C1D95),
    tertiary             = BrandMint,
    onTertiary           = Color.White,
    tertiaryContainer    = Color(0xFFD1FAE5),
    onTertiaryContainer  = Color(0xFF064E3B),
    background           = Color(0xFFF8F9FF),
    onBackground         = Color(0xFF1A1A2E),
    surface              = Color.White,
    onSurface            = Color(0xFF1A1A2E),
    surfaceVariant       = Color(0xFFF1F3FF),
    onSurfaceVariant     = Color(0xFF4A4A6A),
    error                = BrandRose,
    onError              = Color.White,
    outline              = Color(0xFFE0E0F0),
    outlineVariant       = Color(0xFFC7C7E0),
)

// ── Dark scheme ───────────────────────────────────────────────────────────────

internal val DarkColors = darkColorScheme(
    primary              = Color(0xFF818CF8),
    onPrimary            = Color(0xFF1E1B4B),
    primaryContainer     = Color(0xFF312E81),
    onPrimaryContainer   = Color(0xFFC7D2FE),
    secondary            = Color(0xFFA78BFA),
    onSecondary          = Color(0xFF2E1065),
    secondaryContainer   = Color(0xFF3B0764),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary             = Color(0xFF34D399),
    onTertiary           = Color(0xFF022C22),
    tertiaryContainer    = Color(0xFF064E3B),
    onTertiaryContainer  = Color(0xFFA7F3D0),
    background           = Color(0xFF0F0F1A),
    onBackground         = Color(0xFFE8E8F8),
    surface              = Color(0xFF1A1A2E),
    onSurface            = Color(0xFFE8E8F8),
    surfaceVariant       = Color(0xFF252540),
    onSurfaceVariant     = Color(0xFFB0B0D0),
    error                = Color(0xFFF87171),
    onError              = Color(0xFF450A0A),
    outline              = Color(0xFF3A3A55),
    outlineVariant       = Color(0xFF2E2E48),
)

// ── Typography ────────────────────────────────────────────────────────────────

val AppTypography = Typography(
    displayLarge   = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold,     letterSpacing = (-0.5).sp),
    displayMedium  = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold,     letterSpacing = (-0.3).sp),
    headlineLarge  = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall  = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
    titleLarge     = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    titleMedium    = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
    titleSmall     = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    bodyLarge      = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelLarge     = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelMedium    = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall     = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
)

// ── Theme composable ──────────────────────────────────────────────────────────

@Composable
fun JobMatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = AppTypography,
        content     = content,
    )
}