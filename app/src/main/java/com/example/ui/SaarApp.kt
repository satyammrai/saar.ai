package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AllSupportedLanguages
import com.example.data.model.SaarRecap
import com.example.data.repository.SaarRepository
import com.example.ui.clay.ClayCardShapeLarge
import com.example.ui.clay.clayClickable
import com.example.ui.clay.clayClickableSurface
import com.example.ui.clay.claySurface
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.LocalClayColors
import com.example.ui.theme.NunitoFontFamily
import com.example.ui.theme.SaarTheme
import com.example.ui.theme.SaarThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AppDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    RECAPS("My Recaps", Icons.Rounded.LibraryBooks),
    FAVORITES("Favorites", Icons.Rounded.Favorite),
    QUIZZES("Quizzes", Icons.Rounded.Psychology),
    SETTINGS("Settings", Icons.Rounded.Settings)
}

@Composable
fun SaarApp() {
    var themeMode by remember { mutableStateOf(SaarThemeMode.MINT_CREAM) }

    SaarTheme(themeMode = themeMode) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val clayColors = LocalClayColors.current

        val recaps by SaarRepository.recaps.collectAsState()
        val stats by SaarRepository.studyStats.collectAsState()
        val currentLanguage by SaarRepository.selectedLanguage.collectAsState()

        var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
        var activeRecap by remember { mutableStateOf<SaarRecap?>(null) }
        var isGenerating by remember { mutableStateOf(false) }
        var showLanguageDialog by remember { mutableStateOf(false) }
        var loadingStepIndex by remember { mutableStateOf(0) }

        val loadingSteps = listOf(
            "Opening the video 🎬",
            "Listening carefully 🎧",
            "Thinking & distilling 🧠",
            "Drawing visual notes & diagrams ✏️"
        )

        // Loading animation cycle
        LaunchedEffect(isGenerating) {
            if (isGenerating) {
                loadingStepIndex = 0
                while (isGenerating) {
                    delay(800)
                    loadingStepIndex = (loadingStepIndex + 1) % loadingSteps.size
                }
            }
        }

        fun handleSummarize(url: String) {
            isGenerating = true
            coroutineScope.launch {
                val result = SaarRepository.summarizeVideo(url, currentLanguage)
                delay(1200) // Give user a satisfying view of the 4-step progress
                isGenerating = false
                result.onSuccess { recap ->
                    activeRecap = recap
                    Toast.makeText(context, "Saar notes ready! ✨", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "Oops, this one slipped away. Check the link and try again?", Toast.LENGTH_LONG).show()
                }
            }
        }

        // BackHandler: Return to Home or close active recap
        BackHandler(enabled = activeRecap != null || currentDestination != AppDestination.HOME) {
            if (activeRecap != null) {
                activeRecap = null
            } else {
                currentDestination = AppDestination.HOME
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(clayColors.background)
        ) {
            // MAIN CONTENT PANE
            when {
                activeRecap != null -> {
                    val recap = activeRecap!!
                    RecapResultScreen(
                        recap = recap,
                        onBack = { activeRecap = null },
                        onToggleFavorite = {
                            SaarRepository.toggleFavorite(recap.id)
                            activeRecap = SaarRepository.getRecapById(recap.id)
                        }
                    )
                }
                currentDestination == AppDestination.HOME -> {
                    HomeScreen(
                        stats = stats,
                        recentRecaps = recaps,
                        currentLanguage = currentLanguage,
                        onSummarize = ::handleSummarize,
                        onSelectRecap = { activeRecap = it },
                        onOpenLanguagePicker = { showLanguageDialog = true },
                        onToggleTheme = {
                            themeMode = when (themeMode) {
                                SaarThemeMode.MINT_CREAM -> SaarThemeMode.LAVENDER
                                SaarThemeMode.LAVENDER -> SaarThemeMode.WARM_COCOA
                                SaarThemeMode.WARM_COCOA -> SaarThemeMode.MINT_CREAM
                            }
                        },
                        onOpenRevision = {
                            currentDestination = AppDestination.QUIZZES
                        }
                    )
                }
                currentDestination == AppDestination.RECAPS -> {
                    HistoryScreen(
                        recaps = recaps,
                        onSelectRecap = { activeRecap = it },
                        onToggleFavorite = { SaarRepository.toggleFavorite(it) },
                        favoritesOnly = false
                    )
                }
                currentDestination == AppDestination.FAVORITES -> {
                    HistoryScreen(
                        recaps = recaps,
                        onSelectRecap = { activeRecap = it },
                        onToggleFavorite = { SaarRepository.toggleFavorite(it) },
                        favoritesOnly = true
                    )
                }
                currentDestination == AppDestination.QUIZZES -> {
                    QuizFlashcardsScreen(
                        recaps = recaps,
                        onToggleMastery = { rId, cId ->
                            SaarRepository.toggleFlashcardMastery(rId, cId)
                        }
                    )
                }
                currentDestination == AppDestination.SETTINGS -> {
                    SettingsScreen(
                        currentTheme = themeMode,
                        onSelectTheme = { themeMode = it },
                        currentLanguage = currentLanguage,
                        onSelectLanguage = { SaarRepository.setLanguage(it) }
                    )
                }
            }

            // BOTTOM CLAY NAVIGATION BAR (hidden if viewing recap details)
            if (activeRecap == null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .navigationBarsPadding()
                ) {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = clayColors.card,
                        shape = CircleShape,
                        elevation = 10.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppDestination.values().forEach { destination ->
                                val isSelected = currentDestination == destination

                                Box(
                                    modifier = Modifier
                                        .clayClickableSurface(
                                            shape = CircleShape,
                                            backgroundColor = if (isSelected) clayColors.mint else Color.Transparent,
                                            elevation = if (isSelected) 4.dp else 0.dp,
                                            borderWidth = if (isSelected) 1.dp else 0.dp,
                                            hasHighlight = isSelected,
                                            onClick = { currentDestination = destination }
                                        )
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = destination.icon,
                                            contentDescription = destination.label,
                                            tint = if (isSelected) clayColors.coral else clayColors.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = destination.label,
                                                fontFamily = FredokaFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = clayColors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4-STEP LOADING OVERLAY
            if (isGenerating) {
                Dialog(onDismissRequest = {}) {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        backgroundColor = clayColors.card,
                        shape = ClayCardShapeLarge,
                        elevation = 16.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(26.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ClayStarMascotImage(size = 90.dp, isAnimated = true)

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Hang tight! 🍿",
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = clayColors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "We're watching it so you don't have to.",
                                fontFamily = NunitoFontFamily,
                                fontSize = 13.sp,
                                color = clayColors.textSecondary
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // 4-step progress list
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                loadingSteps.forEachIndexed { sIndex, stepText ->
                                    val isCurrent = sIndex == loadingStepIndex
                                    val isDone = sIndex < loadingStepIndex

                                    val bg = when {
                                        isCurrent -> clayColors.peach
                                        isDone -> clayColors.mint
                                        else -> clayColors.background.copy(alpha = 0.5f)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(CircleShape)
                                            .background(bg)
                                            .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isDone) Icons.Rounded.CheckCircle else if (isCurrent) Icons.Rounded.HourglassBottom else Icons.Rounded.RadioButtonUnchecked,
                                                contentDescription = null,
                                                tint = if (isDone) clayColors.deepGreen else if (isCurrent) clayColors.coral else clayColors.textSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = stepText,
                                                fontFamily = FredokaFontFamily,
                                                fontSize = 13.sp,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                color = clayColors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // LANGUAGE PICKER DIALOG
            if (showLanguageDialog) {
                Dialog(onDismissRequest = { showLanguageDialog = false }) {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(0.95f),
                        backgroundColor = clayColors.card,
                        shape = ClayCardShapeLarge,
                        elevation = 14.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Choose Study Language",
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = clayColors.textPrimary
                            )
                            Text(
                                text = "Notes, flowcharts, audio & quiz will be natively synthesized",
                                fontFamily = NunitoFontFamily,
                                fontSize = 12.sp,
                                color = clayColors.textSecondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                AllSupportedLanguages.forEach { lang ->
                                    val isSelected = currentLanguage.equals(lang.name, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(CircleShape)
                                            .background(if (isSelected) clayColors.mint else clayColors.background)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) clayColors.coral else Color.White,
                                                shape = CircleShape
                                            )
                                            .clayClickable {
                                                SaarRepository.setLanguage(lang.name)
                                                showLanguageDialog = false
                                            }
                                            .padding(horizontal = 16.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = lang.name,
                                                fontFamily = FredokaFontFamily,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 14.sp,
                                                color = clayColors.textPrimary
                                            )
                                            Text(
                                                text = lang.nativeName,
                                                fontFamily = NunitoFontFamily,
                                                fontSize = 13.sp,
                                                color = clayColors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
