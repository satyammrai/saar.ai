package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AllSupportedLanguages
import com.example.ui.clay.ClayCardShapeLarge
import com.example.ui.components.*
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.LocalClayColors
import com.example.ui.theme.NunitoFontFamily
import com.example.ui.theme.SaarThemeMode

@Composable
fun SettingsScreen(
    currentTheme: SaarThemeMode,
    onSelectTheme: (SaarThemeMode) -> Unit,
    currentLanguage: String,
    onSelectLanguage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clayColors = LocalClayColors.current

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
                    text = "Settings & Preferences",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = clayColors.textPrimary
                )
                Text(
                    text = "Personalize your 3D clay study dashboard",
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    color = clayColors.textSecondary
                )
            }
        }

        // GO PREMIUM PROMO CARD
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = clayColors.peach,
                shape = ClayCardShapeLarge,
                elevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(clayColors.butter),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.EmojiEvents,
                                        contentDescription = null,
                                        tint = clayColors.coral,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Go Premium 👑",
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = clayColors.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Unlock unlimited recaps, high-res mindmap downloads, and offline voice packs.",
                                fontFamily = NunitoFontFamily,
                                fontSize = 13.sp,
                                color = clayColors.textSecondary
                            )
                        }

                        ClayStarMascotImage(size = 54.dp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    ClayButton(
                        text = "Upgrade to Pro",
                        onClick = {},
                        backgroundColor = clayColors.coral,
                        textColor = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // THEME PICKER
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = clayColors.card,
                shape = ClayCardShapeLarge,
                elevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Theme Palette",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = clayColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClayPill(
                            text = "Mint & Cream",
                            isSelected = currentTheme == SaarThemeMode.MINT_CREAM,
                            onClick = { onSelectTheme(SaarThemeMode.MINT_CREAM) },
                            tintColor = clayColors.mint
                        )

                        ClayPill(
                            text = "Lavender",
                            isSelected = currentTheme == SaarThemeMode.LAVENDER,
                            onClick = { onSelectTheme(SaarThemeMode.LAVENDER) },
                            tintColor = clayColors.lavender
                        )

                        ClayPill(
                            text = "Warm Cocoa",
                            isSelected = currentTheme == SaarThemeMode.WARM_COCOA,
                            onClick = { onSelectTheme(SaarThemeMode.WARM_COCOA) },
                            tintColor = clayColors.butter
                        )
                    }
                }
            }
        }

        // LANGUAGE PICKER
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = clayColors.card,
                shape = ClayCardShapeLarge,
                elevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Output Language",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = clayColors.textPrimary
                    )
                    Text(
                        text = "Natively translates summaries, notes, audio & flowchart nodes",
                        fontFamily = NunitoFontFamily,
                        fontSize = 12.sp,
                        color = clayColors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AllSupportedLanguages.chunked(3).forEach { rowLangs ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowLangs.forEach { lang ->
                                    val isSelected = currentLanguage.equals(lang.name, ignoreCase = true)
                                    ClayPill(
                                        text = "${lang.name} (${lang.nativeName})",
                                        isSelected = isSelected,
                                        onClick = { onSelectLanguage(lang.name) },
                                        tintColor = clayColors.mint,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ABOUT SAAR
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = clayColors.mint.copy(alpha = 0.4f),
                shape = ClayCardShapeLarge,
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "About Saar (सार)",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = clayColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Saar means essence. Designed as a soft 3D clay study companion so students can watch less, understand more, and excel in their studies.",
                        fontFamily = NunitoFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = clayColors.textSecondary
                    )
                }
            }
        }
    }
}
