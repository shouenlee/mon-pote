package com.monpote.feature.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monpote.core.ui.theme.Primary
import com.monpote.core.ui.theme.SurfaceTint
import com.monpote.core.ui.theme.TextSecondary

@Composable
fun TranslationCard(
    translation: String?,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = Spring.StiffnessMediumLow,
            ),
        ),
        exit = shrinkVertically(
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = Spring.StiffnessMediumLow,
            ),
        ),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .padding(start = 24.dp, end = 60.dp, top = 0.dp, bottom = 4.dp)
                .clip(RoundedCornerShape(0.dp, 0.dp, 12.dp, 12.dp))
                .background(SurfaceTint)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = "EN",
                color = Primary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp, top = 1.dp),
            )

            Text(
                text = translation ?: "Traduction...",
                color = TextSecondary,
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                lineHeight = 17.sp,
            )
        }
    }
}
