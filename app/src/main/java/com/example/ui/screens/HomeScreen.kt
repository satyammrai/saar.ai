package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DailyStudy
import com.example.data.model.SaarRecap
import com.example.data.model.StudyStats
import com.example.data.model.TopicShare
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.ui.clay.ClayCardShape
import com.example.ui.clay.ClayCardShapeLarge
import com.example.ui.clay.clayClickable
import com.example.ui.clay.clayClickableSurface
import com.example.ui.components.*
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.LocalClayColors
import com.example.ui.theme.NunitoFontFamily
import com.example.util.SaarExportManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    stats: StudyStats,
    recentRecaps: List<SaarRecap>,
    currentLanguage: String,
    onSummarize: (String) -> Unit,
    onSelectRecap: (SaarRecap) -> Unit,
    onOpenLanguagePicker: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenRevision: (SaarRecap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var exportDialogResult by remember { mutableStateOf<SaarExportManager.ExportResult?>(null) }
    val clayColors = LocalClayColors.current
    var urlInput by remember { mutableStateOf("") }
    var searchKeyword by remember { mutableStateOf("") }

    val filteredRecaps = remember(recentRecaps, searchKeyword) {
        if (searchKeyword.isBlank()) recentRecaps
        else recentRecaps.filter {
            it.title.contains(searchKeyword, ignoreCase = true) ||
            it.channel.contains(searchKeyword, ignoreCase = true) ||
            it.category.contains(searchKeyword, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(clayColors.background)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // TOP BAR: Mascot greeting, title, search, language, theme toggle
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ClayStudentAvatar(size = 50.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Hi, Student! 👋",
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = clayColors.textPrimary
                            )
                        }
                        Text(
                            text = "Watch less. Understand more.",
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = clayColors.textSecondary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Language Chip
                    ClayPill(
                        text = currentLanguage,
                        isSelected = true,
                        onClick = onOpenLanguagePicker,
                        icon = Icons.Rounded.Translate,
                        tintColor = clayColors.mint
                    )

                    // Theme toggle
                    ClayIconButton(
                        icon = if (clayColors.isDark) Icons.Rounded.LightMode else Icons.Rounded.Palette,
                        onClick = onToggleTheme,
                        size = 40.dp,
                        contentDescription = "Toggle Theme"
                    )

                    // Notification Bell
                    ClayIconButton(
                        icon = Icons.Rounded.Notifications,
                        onClick = { /* Clear or show notifications */ },
                        size = 40.dp,
                        badgeText = "3",
                        contentDescription = "Notifications"
                    )
                }
            }
        }

        // BIG PEACH HERO CARD: Prompt bar, Mascot on left, Plant on right
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = clayColors.peach,
                shape = ClayCardShapeLarge,
                elevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ClayStarMascotImage(size = 64.dp)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ClaySparkleBadge(text = "Saar Essence AI 🌟")
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Paste a YouTube link and let's learn!",
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                lineHeight = 23.sp,
                                color = clayColors.textPrimary
                            )
                            Text(
                                text = "Summaries, visual flowcharts, tables, quizzes & audio.",
                                fontFamily = NunitoFontFamily,
                                fontSize = 12.sp,
                                color = clayColors.textSecondary
                            )
                        }

                        ClayPlantImage(size = 60.dp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Giant Clay Input Pill
                    ClayInputPill(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        onSubmit = {
                            if (urlInput.isNotBlank()) {
                                onSummarize(urlInput)
                            }
                        },
                        placeholder = "Paste YouTube link (e.g. youtu.be/...)"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mouse Quick Paste & 1-Tap Demo Row
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. One-Click GATE 2027 Action
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .clayClickableSurface(
                                    shape = CircleShape,
                                    backgroundColor = clayColors.coral,
                                    elevation = 5.dp,
                                    onClick = {
                                        val gateUrl = "https://www.youtube.com/live/TA43DMEkLXI?si=gmnrLpvj1lgtk_7F"
                                        urlInput = gateUrl
                                        android.widget.Toast.makeText(context, "Generating GATE 2027 Notes! 🚀", android.widget.Toast.LENGTH_SHORT).show()
                                        onSummarize(gateUrl)
                                    }
                                )
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🔥 1-Tap GATE 2027",
                                    fontFamily = FredokaFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // 2. Mouse / Clipboard Paste Action
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .clayClickableSurface(
                                    shape = CircleShape,
                                    backgroundColor = Color.White,
                                    elevation = 4.dp,
                                    onClick = {
                                        val clip = clipboardManager.getText()?.text
                                        if (!clip.isNullOrBlank()) {
                                            urlInput = clip.trim()
                                            android.widget.Toast.makeText(context, "Pasted from clipboard! 📋", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            // Fallback to GATE link if clipboard is empty
                                            val defaultTestUrl = "https://www.youtube.com/live/TA43DMEkLXI?si=gmnrLpvj1lgtk_7F"
                                            urlInput = defaultTestUrl
                                            android.widget.Toast.makeText(context, "Pasted GATE 2027 Live link! 🚀", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentPaste,
                                    contentDescription = "Paste",
                                    tint = clayColors.deepGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Paste Link",
                                    fontFamily = FredokaFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = clayColors.deepGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick topic chips
                    Text(
                        text = "Or tap any pre-analyzed topic:",
                        fontFamily = FredokaFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = clayColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(recentRecaps.take(4)) { recap ->
                            Box(
                                modifier = Modifier
                                    .clayClickableSurface(
                                        shape = CircleShape,
                                        backgroundColor = Color.White.copy(alpha = 0.85f),
                                        elevation = 3.dp,
                                        onClick = { onSelectRecap(recap) }
                                    )
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = recap.title.take(28) + if (recap.title.length > 28) "…" else "",
                                    fontFamily = NunitoFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = clayColors.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4 PASTEL STAT CARDS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ClayStatTile(
                    title = "Videos Summarised",
                    value = "${stats.videosSummarized}",
                    delta = "+3 this week",
                    tintColor = clayColors.mint,
                    iconContent = {
                        Icon(
                            Icons.Rounded.PlayCircle,
                            contentDescription = null,
                            tint = clayColors.deepGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                ClayStatTile(
                    title = "Notes Saved",
                    value = "${stats.notesSaved}",
                    delta = "+8 this week",
                    tintColor = clayColors.peach,
                    iconContent = {
                        Icon(
                            Icons.Rounded.Description,
                            contentDescription = null,
                            tint = clayColors.coral,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ClayStatTile(
                    title = "Hours Saved",
                    value = "${stats.hoursSaved}h",
                    delta = "+5.2 this week",
                    tintColor = clayColors.butter,
                    iconContent = {
                        Icon(
                            Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = Color(0xFFC78B1E),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                ClayStatTile(
                    title = "Study Streak",
                    value = "${stats.streakDays} days",
                    delta = "🔥 in a row",
                    tintColor = clayColors.sky,
                    iconContent = {
                        Icon(
                            Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFE25C38),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // CHARTS: Study Overview & Top Topics
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Study Overview Bar Chart
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = clayColors.card,
                    shape = ClayCardShapeLarge,
                    elevation = 6.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Study Overview",
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = clayColors.textPrimary
                                )
                                Text(
                                    text = "Hours saved through Saar this week",
                                    fontFamily = NunitoFontFamily,
                                    fontSize = 12.sp,
                                    color = clayColors.textSecondary
                                )
                            }
                            ClayPill(
                                text = "This Week",
                                isSelected = true,
                                onClick = {},
                                tintColor = clayColors.mint
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Custom Pastel Bar Chart
                        StudyBarChart(
                            daily = stats.weeklyOverview,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        )
                    }
                }

                // Top Topics Donut Chart
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = clayColors.card,
                    shape = ClayCardShapeLarge,
                    elevation = 6.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Top Topics Recapped",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = clayColors.textPrimary
                        )
                        Text(
                            text = "Distribution across your video library",
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            color = clayColors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DonutChart(
                                topics = stats.topTopics,
                                modifier = Modifier.size(110.dp)
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                stats.topTopics.forEach { topic ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(android.graphics.Color.parseColor(topic.colorHex)))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = topic.name,
                                                fontFamily = NunitoFontFamily,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = clayColors.textPrimary
                                            )
                                        }
                                        Text(
                                            text = "${topic.percentage}%",
                                            fontFamily = FredokaFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
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

        // RECENTLY RECAPPED
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recently Recapped",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = clayColors.textPrimary
                )
                Text(
                    text = "${filteredRecaps.size} videos",
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = clayColors.textSecondary
                )
            }
        }

        items(filteredRecaps) { recap ->
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
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Video Thumbnail
                    Box(
                        modifier = Modifier
                            .size(width = 96.dp, height = 66.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(clayColors.background)
                            .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = recap.channel,
                                fontFamily = NunitoFontFamily,
                                fontSize = 12.sp,
                                color = clayColors.textSecondary
                            )
                            Text(
                                text = " • ${recap.durationText}",
                                fontFamily = NunitoFontFamily,
                                fontSize = 11.sp,
                                color = clayColors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ClayIconButton(
                            icon = Icons.Rounded.Download,
                            onClick = {
                                Toast.makeText(context, "Downloading PDF for ${recap.title}... 📑", Toast.LENGTH_SHORT).show()
                                coroutineScope.launch(Dispatchers.IO) {
                                    val res = SaarExportManager.exportToPdf(context, recap)
                                    withContext(Dispatchers.Main) {
                                        if (res.success) {
                                            exportDialogResult = res
                                            Toast.makeText(context, "PDF saved to phone Downloads! 🎉", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Export error: ${res.error}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            size = 36.dp,
                            backgroundColor = clayColors.mint,
                            tint = clayColors.deepGreen,
                            contentDescription = "Download PDF"
                        )

                        ClayIconButton(
                            icon = Icons.Rounded.ArrowForward,
                            onClick = { onSelectRecap(recap) },
                            size = 36.dp,
                            backgroundColor = clayColors.peach
                        )
                    }
                }
            }
        }

        // DAILY REVISION CARD
        val firstRecap = recentRecaps.firstOrNull()
        if (firstRecap != null) {
            item {
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = clayColors.lavender.copy(alpha = 0.4f),
                    shape = ClayCardShapeLarge,
                    elevation = 6.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(clayColors.lavender),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Psychology,
                                        contentDescription = null,
                                        tint = clayColors.textPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Daily Revision",
                                        fontFamily = FredokaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = clayColors.textPrimary
                                    )
                                    Text(
                                        text = "Quick active recall flashcard",
                                        fontFamily = NunitoFontFamily,
                                        fontSize = 12.sp,
                                        color = clayColors.textSecondary
                                    )
                                }
                            }

                            ClayButton(
                                text = "Review Deck",
                                onClick = { onOpenRevision(firstRecap) },
                                backgroundColor = clayColors.coral,
                                shape = CircleShape
                            )
                        }

                        val sampleCard = firstRecap.flashcards.firstOrNull()
                        if (sampleCard != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            ClayCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = Color.White.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(20.dp),
                                elevation = 2.dp
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Q: ${sampleCard.front}",
                                        fontFamily = FredokaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = clayColors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = sampleCard.back,
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

        // BOTTOM DISCOVERY BANNER
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = clayColors.mint,
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        ClayStarMascotImage(size = 46.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Discover smarter study ✨",
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = clayColors.textPrimary
                            )
                            Text(
                                text = "Made just for you with Saar",
                                fontFamily = NunitoFontFamily,
                                fontSize = 12.sp,
                                color = clayColors.textSecondary
                            )
                        }
                    }

                    ClayButton(
                        text = "Explore Now",
                        onClick = {
                            if (recentRecaps.isNotEmpty()) {
                                onSelectRecap(recentRecaps.first())
                            }
                        },
                        backgroundColor = clayColors.deepGreen,
                        textColor = Color.White
                    )
                }
            }
        }
    }

    if (exportDialogResult != null) {
        val res = exportDialogResult!!
        AlertDialog(
            onDismissRequest = { exportDialogResult = null },
            shape = ClayCardShapeLarge,
            containerColor = clayColors.card,
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(clayColors.mint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PictureAsPdf,
                        contentDescription = null,
                        tint = clayColors.deepGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "PDF Downloaded! 🎉",
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = clayColors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = res.fileName,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = clayColors.textPrimary
                    )
                    Text(
                        text = "Saved in your phone's Downloads directory:\n${res.filePathDesc}",
                        fontFamily = NunitoFontFamily,
                        fontSize = 12.sp,
                        color = clayColors.textSecondary
                    )
                }
            },
            confirmButton = {
                ClayButton(
                    text = "Open PDF 📄",
                    onClick = {
                        res.fileUri?.let { uri ->
                            SaarExportManager.openFile(context, uri, res.mimeType)
                        }
                        exportDialogResult = null
                    },
                    backgroundColor = clayColors.coral,
                    textColor = Color.White
                )
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClayButton(
                        text = "Share 📤",
                        onClick = {
                            res.fileUri?.let { uri ->
                                SaarExportManager.shareFile(context, uri, res.mimeType, res.fileName)
                            }
                        },
                        backgroundColor = clayColors.sky,
                        textColor = clayColors.textPrimary
                    )
                    ClayButton(
                        text = "Done",
                        onClick = { exportDialogResult = null },
                        backgroundColor = clayColors.background,
                        textColor = clayColors.textPrimary
                    )
                }
            }
        )
    }
}

@Composable
private fun StudyBarChart(
    daily: List<DailyStudy>,
    modifier: Modifier = Modifier
) {
    val clayColors = LocalClayColors.current
    val maxHours = 6f

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        daily.forEach { item ->
            val barHeightFraction = (item.hours / maxHours).coerceIn(0.15f, 1.0f)
            val barColor = Color(android.graphics.Color.parseColor(item.colorHex))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${item.hours}h",
                    fontFamily = FredokaFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = clayColors.textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .fillMaxHeight(barHeightFraction)
                        .clip(RoundedCornerShape(topStart = 11.dp, topEnd = 11.dp))
                        .background(barColor)
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.8f),
                            RoundedCornerShape(topStart = 11.dp, topEnd = 11.dp)
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.day,
                    fontFamily = NunitoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = clayColors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun DonutChart(
    topics: List<TopicShare>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 24.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        var startAngle = -90f
        topics.forEach { topic ->
            val sweepAngle = (topic.percentage / 100f) * 360f
            val color = Color(android.graphics.Color.parseColor(topic.colorHex))

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle - 3f, // Soft gap
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += sweepAngle
        }
    }
}
