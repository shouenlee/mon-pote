package com.monpote.feature.chat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.monpote.core.model.Character
import com.monpote.core.model.Message
import com.monpote.core.model.Role
import com.monpote.core.ui.theme.Primary
import com.monpote.core.ui.theme.TextMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: Message,
    character: Character?,
    onLongPress: (() -> Unit)? = null,
) {
    val isUser = message.role == Role.USER
    val maxWidth = (LocalConfiguration.current.screenWidthDp * 0.8).dp
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    Column(
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp),
    ) {
        Surface(
            shape = if (isUser) {
                RoundedCornerShape(18.dp, 4.dp, 18.dp, 18.dp)
            } else {
                RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp)
            },
            color = if (isUser) Primary else MaterialTheme.colorScheme.surface,
            shadowElevation = if (isUser) 0.dp else 2.dp,
            modifier = Modifier
                .widthIn(max = maxWidth)
                .then(
                    if (!isUser) {
                        Modifier.border(
                            width = 0.5.dp,
                            color = Color(0xFFE8E8E8),
                            shape = RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp),
                        )
                    } else Modifier
                )
                .scale(scale.value)
                .then(
                    if (!isUser && onLongPress != null) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures(onLongPress = {
                                scope.launch {
                                    // Single bounce: squish → overshoot → settle
                                    scale.animateTo(0.96f, animationSpec = tween(80))
                                    scale.animateTo(1.02f, animationSpec = tween(100))
                                    scale.animateTo(1f, animationSpec = tween(80))
                                }
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongPress()
                            })
                        }
                    } else Modifier
                ),
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.padding(12.dp, 10.dp),
            )
        }

        // Timestamp outside bubble
        Text(
            text = formatTimestamp(message.timestamp),
            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
            modifier = Modifier.padding(
                start = if (!isUser) 4.dp else 0.dp,
                end = if (isUser) 4.dp else 0.dp,
                top = 2.dp,
            ),
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(Date(timestamp))
}
