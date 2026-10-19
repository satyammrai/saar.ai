package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FlashcardItem
import com.example.data.model.SaarRecap
import com.example.ui.clay.ClayCardShapeLarge
import com.example.ui.clay.clayClickable
import com.example.ui.components.*
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.LocalClayColors
import com.example.ui.theme.NunitoFontFamily

@Composable
fun QuizFlashcardsScreen(
    recaps: List<SaarRecap>,
    onToggleMastery: (recapId: String, cardId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clayColors = LocalClayColors.current

    val allCards = remember(recaps) {
        recaps.flatMap { r -> r.flashcards.map { card -> Pair(r.id, card) } }
    }

    val masteredCount = allCards.count { it.second.isMastered }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(clayColors.background)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Daily Quizzes & Flashcards",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = clayColors.textPrimary
                )
                Text(
                    text = "Spaced repetition and active recall from your video library",
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    color = clayColors.textSecondary
                )
            }
        }

        // Stats Banner
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = clayColors.peach,
                shape = ClayCardShapeLarge,
                elevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Deck Mastery Progress",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = clayColors.textPrimary
                        )
                        Text(
                            text = "$masteredCount / ${allCards.size} cards mastered ✨",
                            fontFamily = NunitoFontFamily,
                            fontSize = 13.sp,
                            color = clayColors.textSecondary
                        )
                    }

                    ClayStarMascotImage(size = 52.dp)
                }
            }
        }

        items(allCards) { (recapId, card) ->
            StandaloneFlashcard(
                card = card,
                onToggleMastered = { onToggleMastery(recapId, card.id) }
            )
        }
    }
}

@Composable
private fun StandaloneFlashcard(
    card: FlashcardItem,
    onToggleMastered: () -> Unit
) {
    val clayColors = LocalClayColors.current
    var isFlipped by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "standaloneFlip"
    )

    ClayCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        onClick = { isFlipped = !isFlipped },
        backgroundColor = if (card.isMastered) clayColors.mint.copy(alpha = 0.6f) else clayColors.card,
        shape = ClayCardShapeLarge,
        elevation = 5.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            if (rotation <= 90f) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ClaySparkleBadge(text = card.topic, backgroundColor = clayColors.butter)
                        if (card.isMastered) {
                            Text(
                                text = "Mastered 🏆",
                                fontFamily = FredokaFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = clayColors.deepGreen
                            )
                        }
                    }

                    Text(
                        text = card.front,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = clayColors.textPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tap to flip ✨",
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            color = clayColors.textSecondary
                        )

                        ClayIconButton(
                            icon = if (card.isMastered) Icons.Rounded.CheckCircle else Icons.Rounded.Replay,
                            onClick = onToggleMastered,
                            tint = if (card.isMastered) clayColors.deepGreen else clayColors.coral,
                            size = 36.dp
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Essence / Answer",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = clayColors.deepGreen
                    )

                    Text(
                        text = card.back,
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = clayColors.textPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tap to return",
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            color = clayColors.textSecondary
                        )

                        ClayButton(
                            text = if (card.isMastered) "Mark Review" else "Got It! ✓",
                            onClick = onToggleMastered,
                            backgroundColor = if (card.isMastered) clayColors.sky else clayColors.mint,
                            textColor = clayColors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
