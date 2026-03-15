package com.monpote.feature.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.monpote.core.model.Character
import com.monpote.core.ui.components.CharacterAvatar
import com.monpote.core.ui.theme.KarimTagBg
import com.monpote.core.ui.theme.LucasTagBg
import com.monpote.core.ui.theme.SarahTagBg
import com.monpote.core.ui.theme.TextFaint
import com.monpote.core.ui.theme.TextMuted
import com.monpote.core.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    onCharacterSelected: (characterId: String, conversationId: Long) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val characters by viewModel.characters.collectAsState()
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Text(text = "\uD83C\uDDEB\uD83C\uDDF7", fontSize = 32.sp)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Mon Pote Parisien",
                style = MaterialTheme.typography.headlineLarge,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choisis ton pote",
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                fontSize = 13.sp,
            )

            Spacer(modifier = Modifier.height(32.dp))

            characters.forEachIndexed { index, character ->
                // Stagger entrance animation
                val offsetY = remember { Animatable(60f) }
                val alpha = remember { Animatable(0f) }

                LaunchedEffect(character.id) {
                    delay(index * 80L)
                    offsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = 0.7f,
                            stiffness = Spring.StiffnessMedium,
                        ),
                    )
                }

                LaunchedEffect(character.id) {
                    delay(index * 80L)
                    alpha.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    )
                }

                CharacterCard(
                    character = character,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.selectCharacter(character.id) { conversationId ->
                            onCharacterSelected(character.id, conversationId)
                        }
                    },
                    modifier = Modifier
                        .offset { IntOffset(0, offsetY.value.toInt()) }
                        .alpha(alpha.value),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tu pourras changer de pote plus tard",
                style = MaterialTheme.typography.labelSmall.copy(color = TextFaint),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun CharacterCard(
    character: Character,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.97f else 1f

    val tagBg = when (character.id) {
        "lucas" -> LucasTagBg
        "sarah" -> SarahTagBg
        "karim" -> KarimTagBg
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            CharacterAvatar(
                initial = character.name.first(),
                color = Color(character.color),
                characterId = character.id,
                size = 52.dp,
                fontSize = 22.sp,
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = tagBg,
                    ) {
                        Text(
                            text = character.tag,
                            color = Color(character.color),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        )
                    }
                }

                Text(
                    text = character.location,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 3.dp),
                )

                Text(
                    text = character.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
