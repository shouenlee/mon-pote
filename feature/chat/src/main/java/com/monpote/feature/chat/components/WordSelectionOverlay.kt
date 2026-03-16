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
import androidx.compose.ui.geometry.Offset
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

/**
 * A selection is an explicit group of word indices.
 * - Tap creates a single-index selection: Selection(setOf(3))
 * - Drag creates a range selection: Selection(setOf(3, 4, 5))
 * Two adjacent taps remain separate: [Selection({3}), Selection({4})]
 * One drag across 3→5 is one group: [Selection({3, 4, 5})]
 */
private data class Selection(val indices: Set<Int>)

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
    // Explicit list of selections — each is a group of indices (single word or phrase)
    var selections by remember { mutableStateOf(listOf<Selection>()) }
    var dragStartIndex by remember { mutableStateOf(-1) }
    var dragCurrentIndex by remember { mutableStateOf(-1) }
    // Track which indices are part of the current in-progress drag (not yet committed)
    var dragIndices by remember { mutableStateOf(setOf<Int>()) }
    val haptic = LocalHapticFeedback.current

    // All currently selected indices (committed + in-progress drag)
    val allSelectedIndices = selections.flatMap { it.indices }.toSet() + dragIndices

    // Track bounds of each pill for drag hit-testing
    val pillBounds = remember { mutableMapOf<Int, Rect>() }

    fun hitTest(x: Float, y: Float): Int {
        for ((index, bounds) in pillBounds) {
            if (bounds.contains(Offset(x, y))) return index
        }
        return -1
    }

    // Find which selection group contains an index (if any)
    fun findSelection(index: Int): Int {
        return selections.indexOfFirst { index in it.indices }
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
                    text = "Sélectionne les mots",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "✕",
                    color = TextFaint,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .padding(4.dp),
                )
            }

            // Word pills with drag-to-select
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 14.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val idx = hitTest(offset.x, offset.y)
                                if (idx >= 0) {
                                    dragStartIndex = idx
                                    dragCurrentIndex = idx
                                    dragIndices = setOf(idx)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val idx = hitTest(change.position.x, change.position.y)
                                if (idx >= 0 && dragStartIndex >= 0) {
                                    if (idx != dragCurrentIndex) {
                                        dragCurrentIndex = idx
                                        val rangeStart = minOf(dragStartIndex, idx)
                                        val rangeEnd = maxOf(dragStartIndex, idx)
                                        dragIndices = (rangeStart..rangeEnd).toSet()
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                }
                            },
                            onDragEnd = {
                                if (dragIndices.isNotEmpty()) {
                                    // Remove any existing selections that overlap with drag
                                    val cleaned = selections.filter { sel ->
                                        sel.indices.none { it in dragIndices }
                                    }
                                    selections = cleaned + Selection(dragIndices)
                                }
                                dragStartIndex = -1
                                dragCurrentIndex = -1
                                dragIndices = emptySet()
                            },
                            onDragCancel = {
                                dragStartIndex = -1
                                dragCurrentIndex = -1
                                dragIndices = emptySet()
                            },
                        )
                    },
            ) {
                words.forEachIndexed { index, word ->
                    val isSelected = index in allSelectedIndices
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
                                    val existingIdx = findSelection(index)
                                    if (existingIdx >= 0) {
                                        // Deselect: remove the entire selection group containing this index
                                        selections = selections.toMutableList().apply { removeAt(existingIdx) }
                                    } else {
                                        // Select: add as a new single-word selection
                                        selections = selections + Selection(setOf(index))
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
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selections.isNotEmpty()) Primary else Primary.copy(alpha = 0.3f))
                    .clickable(enabled = selections.isNotEmpty()) {
                        val phrases = selections.map { sel ->
                            sel.indices.sorted().map { words[it] }.joinToString(" ")
                        }
                        onSave(phrases)
                    }
                    .padding(vertical = 12.dp),
            ) {
                Text(
                    text = if (selections.isEmpty()) "Sauvegarder"
                    else "Sauvegarder (${selections.size})",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
