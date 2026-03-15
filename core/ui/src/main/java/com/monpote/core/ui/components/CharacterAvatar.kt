package com.monpote.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monpote.core.ui.theme.KarimGradientEnd
import com.monpote.core.ui.theme.KarimGradientStart
import com.monpote.core.ui.theme.LucasGradientEnd
import com.monpote.core.ui.theme.LucasGradientStart
import com.monpote.core.ui.theme.SarahGradientEnd
import com.monpote.core.ui.theme.SarahGradientStart

@Composable
fun CharacterAvatar(
    initial: Char,
    color: Color,
    characterId: String? = null,
    size: Dp = 40.dp,
    fontSize: TextUnit = 18.sp,
    modifier: Modifier = Modifier,
) {
    val gradient = when (characterId) {
        "lucas" -> Brush.linearGradient(listOf(LucasGradientStart, LucasGradientEnd))
        "sarah" -> Brush.linearGradient(listOf(SarahGradientStart, SarahGradientEnd))
        "karim" -> Brush.linearGradient(listOf(KarimGradientStart, KarimGradientEnd))
        else -> null
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.4f))
            .then(
                if (gradient != null) {
                    Modifier.background(gradient)
                } else {
                    Modifier.background(color)
                }
            ),
    ) {
        Text(
            text = initial.toString(),
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
        )
    }
}
