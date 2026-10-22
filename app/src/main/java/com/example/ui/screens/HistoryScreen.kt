package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SaarRecap
import com.example.ui.clay.ClayCardShapeLarge
import com.example.ui.clay.clayClickable
import com.example.ui.components.*
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.LocalClayColors
import com.example.ui.theme.NunitoFontFamily

@Composable
fun HistoryScreen(
    recaps: List<SaarRecap>,
    onSelectRecap: (SaarRecap) -> Unit,
    onToggleFavorite: (String) -> Unit,
    favoritesOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    val clayColors = LocalClayColors.current
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyFavs by remember { mutableStateOf(favoritesOnly) }

    val displayedRecaps = remember(recaps, searchQuery, showOnlyFavs) {
        recaps.filter {
            (!showOnlyFavs || it.isFavorite) &&
            (searchQuery.isBlank() ||
             it.title.contains(searchQuery, ignoreCase = true) ||
             it.channel.contains(searchQuery, ignoreCase = true) ||
             it.category.contains(searchQuery, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(clayColors.background)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (showOnlyFavs) "Favorite Recaps" else "My Recaps",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = clayColors.textPrimary
                    )
                    Text(
                        text = "${displayedRecaps.size} video notes saved on device",
                        fontFamily = NunitoFontFamily,
                        fontSize = 13.sp,
                        color = clayColors.textSecondary
                    )
                }

                ClayPill(
                    text = if (showOnlyFavs) "Favorites ★" else "All Recaps",
                    isSelected = showOnlyFavs,
                    onClick = { showOnlyFavs = !showOnlyFavs },
                    tintColor = clayColors.butter
                )
            }
        }

        // Search Input
        item {
            ClayInputPill(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                onSubmit = {},
                placeholder = "Search notes by title, author or category...",
                buttonText = "Filter"
            )
        }

        if (displayedRecaps.isEmpty()) {
            item {
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = clayColors.card,
                    shape = ClayCardShapeLarge,
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ClayStarMascotImage(size = 72.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Nothing here yet ✨",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = clayColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Paste a YouTube link on Home to create your first visual notes!",
                            fontFamily = NunitoFontFamily,
                            fontSize = 13.sp,
                            color = clayColors.textSecondary
                        )
                    }
                }
            }
        } else {
            items(displayedRecaps) { recap ->
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = clayColors.card,
                    shape = RoundedCornerShape(24.dp),
                    elevation = 5.dp,
                    onClick = { onSelectRecap(recap) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 96.dp, height = 68.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(clayColors.background),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = recap.thumbnailUrl,
                                contentDescription = recap.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = recap.title,
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = clayColors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${recap.channel} • ${recap.durationText}",
                                fontFamily = NunitoFontFamily,
                                fontSize = 12.sp,
                                color = clayColors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ClaySparkleBadge(
                                text = recap.category,
                                backgroundColor = clayColors.mint.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        ClayIconButton(
                            icon = if (recap.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            onClick = { onToggleFavorite(recap.id) },
                            tint = if (recap.isFavorite) clayColors.coral else clayColors.textSecondary,
                            size = 38.dp,
                            contentDescription = "Favorite"
                        )
                    }
                }
            }
        }
    }
}
