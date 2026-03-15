package com.monpote.feature.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monpote.core.ui.theme.ErrorRed
import com.monpote.core.ui.theme.Primary
import com.monpote.core.ui.theme.SurfaceVariant
import com.monpote.feature.chat.CorrectionState
import com.monpote.feature.correction.CorrectionError
import com.monpote.feature.correction.CorrectionResult
import kotlinx.coroutines.delay

private val GrammarOrange = Color(0xFFE67E22)
private val SuccessGreen = Color(0xFF2ECC71)

@Composable
fun FeedbackPanel(
    correctionState: CorrectionState,
    correctionResult: CorrectionResult?,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = correctionState != CorrectionState.IDLE,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        when (correctionState) {
            CorrectionState.LOADING -> LoadingBanner()
            CorrectionState.SUCCESS -> {
                if (correctionResult == null || correctionResult.errors.isEmpty()) {
                    SuccessBanner(onDismiss = onDismiss)
                } else {
                    ErrorPanel(
                        errors = correctionResult.errors,
                        onDismiss = onDismiss,
                    )
                }
            }
            CorrectionState.ERROR -> ErrorBanner(onDismiss = onDismiss)
            CorrectionState.IDLE -> {}
        }
    }
}

@Composable
private fun LoadingBanner() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceVariant)
            .padding(12.dp, 10.dp),
    ) {
        CircularProgressIndicator(
            color = Color.Gray,
            strokeWidth = 2.dp,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = "Vérification en cours...",
            color = Color.Gray,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun SuccessBanner(onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onDismiss()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(SuccessGreen.copy(alpha = 0.13f))
            .padding(12.dp, 10.dp),
    ) {
        Text(text = "✓", color = SuccessGreen, fontSize = 16.sp)
        Text(
            text = "Parfait !",
            color = SuccessGreen,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Aucune erreur détectée",
            color = SuccessGreen.copy(alpha = 0.7f),
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ErrorBanner(onDismiss: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(ErrorRed.copy(alpha = 0.13f))
            .clickable(onClick = onDismiss)
            .padding(12.dp, 10.dp),
    ) {
        Text(text = "✕", color = ErrorRed, fontSize = 13.sp)
        Text(
            text = "Vérification indisponible",
            color = ErrorRed,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ErrorPanel(
    errors: List<CorrectionError>,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // Red top border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(ErrorRed),
        )

        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text(text = "⚠", color = ErrorRed, fontSize = 14.sp)
            Text(
                text = " ${errors.size} correction${if (errors.size > 1) "s" else ""}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "✕",
                color = Color.Gray,
                fontSize = 18.sp,
                modifier = Modifier.clickable(onClick = onDismiss),
            )
        }

        // Error cards
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .heightIn(max = 220.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
        ) {
            errors.forEach { error ->
                ErrorCard(error = error)
            }
        }
    }
}

@Composable
private fun ErrorCard(error: CorrectionError) {
    val borderColor = when (error.type) {
        "orthographe" -> ErrorRed
        "grammaire" -> GrammarOrange
        "style" -> Primary
        else -> Color.Gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceVariant),
    ) {
        // Left color border
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(borderColor),
        )

        Column(modifier = Modifier.padding(10.dp, 8.dp)) {
            Text(
                text = error.type.replaceFirstChar { it.uppercase() },
                color = borderColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text(
                    text = error.original,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    textDecoration = TextDecoration.LineThrough,
                )
                Text(
                    text = " → ",
                    color = Color.Gray,
                    fontSize = 13.sp,
                )
                Text(
                    text = error.correction,
                    color = SuccessGreen,
                    fontSize = 13.sp,
                )
            }

            Text(
                text = error.explanation,
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
