package com.monpote.feature.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.monpote.core.model.Character
import com.monpote.core.ui.components.CharacterAvatar
import com.monpote.core.ui.theme.TextMuted
import com.monpote.core.ui.theme.TextSecondary

@Composable
fun OnboardingScreen(
    onCharacterSelected: (characterId: String, conversationId: Long) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val characters by viewModel.characters.collectAsState()

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
            Spacer(modifier = Modifier.height(48.dp))

            Text(text = "\uD83C\uDDEB\uD83C\uDDF7", fontSize = 28.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mon Pote Parisien",
                style = MaterialTheme.typography.headlineLarge,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choisis ton pote",
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(32.dp))

            characters.forEach { character ->
                CharacterCard(
                    character = character,
                    onClick = {
                        viewModel.selectCharacter(character.id) { conversationId ->
                            onCharacterSelected(character.id, conversationId)
                        }
                    },
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tu pourras changer de pote plus tard dans les paramètres",
                style = MaterialTheme.typography.labelSmall,
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
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            CharacterAvatar(
                initial = character.name.first(),
                color = Color(character.color),
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
                        color = Color(character.color).copy(alpha = 0.2f),
                    ) {
                        Text(
                            text = character.tag,
                            color = Color(character.color),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }

                Text(
                    text = character.location,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                    modifier = Modifier.padding(top = 2.dp),
                )

                Text(
                    text = character.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
