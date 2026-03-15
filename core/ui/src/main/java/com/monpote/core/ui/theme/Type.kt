package com.monpote.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val MonPoteTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = TextPrimary,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = TextPrimary,
    ),
    bodyLarge = TextStyle(
        fontSize = 14.sp,
        color = TextPrimary,
        lineHeight = 20.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 13.sp,
        color = TextPrimary,
        lineHeight = 19.sp,
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        color = TextSecondary,
    ),
    labelSmall = TextStyle(
        fontSize = 10.sp,
        color = TextMuted,
    ),
)
