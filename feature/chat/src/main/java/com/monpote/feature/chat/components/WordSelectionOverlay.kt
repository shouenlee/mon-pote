package com.monpote.feature.chat.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monpote.core.ui.theme.Primary
import com.monpote.core.ui.theme.SurfaceVariant
import com.monpote.core.ui.theme.TextFaint
import com.monpote.core.ui.theme.TextMuted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordSelectionOverlay(
    messageContent: String,
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    val words = remember(messageContent) {
        messageContent.split(Regex("\\s+")).filter { it.isNotBlank() }
    }
    var selectedIndices by remember { mutableStateOf(setOf<Int>()) }
    var dragStartIndex by remember { mutableStateOf(-1) }
    var dragCurrentIndex by remember { mutableStateOf(-1) }
    val haptic = LocalHapticFeedback.current

    // Track bounds of each pill for drag hit-testing
    val pillBounds = remember { mutableMapOf<Int, Rect>() }

    // Find which pill index contains a given position
    fun hitTest(x: Float, y: Float): Int {
        for ((index, bounds) in pillBounds) {
            if (bounds.contains(androidx.compose.ui.geometry.Offset(x, y))) {
                return index
            }
        }
        return -1
    }

    Surface(
        shape = RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.7f,
                    stiffness = Spring.StiffnessMedium,
                ),
            ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "S\u00e9lectionne les mots",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "\u2715",
                    color = TextFaint,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .padding(4.dp),
                )
            }

            // Word pills with drag-to-select
            var flowRowCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 14.dp)
                    .onGloballyPositioned { flowRowCoords = it }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val idx = hitTest(offset.x, offset.y)
                                if (idx >= 0) {
                                    dragStartIndex = idx
                                    dragCurrentIndex = idx
                                    selectedIndices = selectedIndices + idx
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val idx = hitTest(change.position.x, change.position.y)
                                if (idx >= 0 && idx != dragCurrentIndex && dragStartIndex >= 0) {
                                    dragCurrentIndex = idx
                                    // Select all indices between start and current
                                    val rangeStart = minOf(dragStartIndex, idx)
                                    val rangeEnd = maxOf(dragStartIndex, idx)
                                    val rangeSet = (rangeStart..rangeEnd).toSet()
                                    selectedIndices = selectedIndices + rangeSet
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            },
                            onDragEnd = {
                                dragStartIndex = -1
                                dragCurrentIndex = -1
                            },
                            onDragCancel = {
                                dragStartIndex = -1
                                dragCurrentIndex = -1
                            },
                        )
                    },
            ) {
                words.forEachIndexed { index, word ->
                    val isSelected = index in selectedIndices
                    val isEmoji = word.matches(Regex("[\\p{So}\\p{Sc}\\p{Sk}\\p{Sm}]+"))

                    if (!isEmoji) {
                        Box(
                            modifier = Modifier
                                .onGloballyPositioned { coords ->
                                    pillBounds[index] = coords.boundsInParent()
                                }
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Primary else SurfaceVariant)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedIndices = if (isSelected) {
                                        selectedIndices - index
                                    } else {
                                        selectedIndices + index
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = word,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                )
                                if (isSelected) {
                                    Text(
                                        text = " ✓",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = word,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 2.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            // Save button
            val selectedCount = getSelectionGroups(selectedIndices).size
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedIndices.isNotEmpty()) Primary else Primary.copy(alpha = 0.3f))
                    .clickable(enabled = selectedIndices.isNotEmpty()) {
                        val groups = getSelectionGroups(selectedIndices)
                        val phrases = groups.map { group ->
                            group.map { words[it] }.joinToString(" ")
                        }
                        onSave(phrases)
                    }
                    .padding(vertical = 12.dp),
            ) {
                Text(
                    text = if (selectedIndices.isEmpty()) "Sauvegarder"
                    else "Sauvegarder ($selectedCount)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/**
 * Groups consecutive selected indices into phrase groups.
 * e.g., {0, 1, 2, 5, 6} -> [[0,1,2], [5,6]]
 */
private fun getSelectionGroups(indices: Set<Int>): List<List<Int>> {
    if (indices.isEmpty()) return emptyList()
    val sorted = indices.sorted()
    val groups = mutableListOf<MutableList<Int>>()
    var currentGroup = mutableListOf(sorted.first())

    for (i in 1 until sorted.size) {
        if (sorted[i] == sorted[i - 1] + 1) {
            currentGroup.add(sorted[i])
        } else {
            groups.add(currentGroup)
            currentGroup = mutableListOf(sorted[i])
        }
    }
    groups.add(currentGroup)
    return groups
}
