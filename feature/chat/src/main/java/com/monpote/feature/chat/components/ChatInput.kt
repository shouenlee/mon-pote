package com.monpote.feature.chat.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monpote.core.ui.theme.OnSurface
import com.monpote.core.ui.theme.Primary
import com.monpote.core.ui.theme.SuccessGreen
import com.monpote.core.ui.theme.SurfaceVariant
import com.monpote.core.ui.theme.TextMuted

@Composable
fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onLongPress: () -> Unit = {},
    isChecking: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    var checkPressed by remember { mutableStateOf(false) }
    val checkScale by animateFloatAsState(
        targetValue = if (checkPressed) 0.9f else 1f,
        animationSpec = tween(100),
        finishedListener = { checkPressed = false },
        label = "checkScale",
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        text = "Écrire un message...",
                        color = TextMuted,
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceVariant,
                    unfocusedContainerColor = SurfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface,
                    cursorColor = Primary,
                ),
                shape = RoundedCornerShape(22.dp),
                singleLine = false,
                maxLines = 4,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
            )

            if (text.isNotBlank()) {
                FloatingActionButton(
                    onClick = {
                        checkPressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    },
                    shape = CircleShape,
                    containerColor = if (isChecking) SuccessGreen.copy(alpha = 0.4f) else SuccessGreen,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 2.dp,
                    ),
                    modifier = Modifier
                        .size(38.dp)
                        .scale(checkScale),
                ) {
                    Text(text = "✓", color = Color.White, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))
            }

            FloatingActionButton(
                onClick = onSend,
                shape = CircleShape,
                containerColor = if (text.isNotBlank()) Primary else Primary.copy(alpha = 0.4f),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                ),
                modifier = Modifier.size(38.dp),
            ) {
                Text(text = "➤", color = Color.White)
            }
        }
    }
}
