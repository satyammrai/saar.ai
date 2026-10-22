package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.FlashcardItem
import com.example.data.model.QuizItem
import com.example.data.model.SaarRecap
import com.example.ui.clay.ClayCardShape
import com.example.ui.clay.ClayCardShapeLarge
import com.example.ui.clay.clayClickable
import com.example.ui.components.*
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.LocalClayColors
import com.example.ui.theme.NunitoFontFamily
import java.util.*
import kotlinx.coroutines.launch

enum class RecapTab(val title: String) {
    SUMMARY("Summary"),
    NOTES("Notes"),
    FLOWCHART("Flowchart"),
    TABLE("Table"),
    LISTEN("Listen"),
    QUIZ("Quiz")
}

@Composable
fun RecapResultScreen(
    recap: SaarRecap,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clayColors = LocalClayColors.current
    var selectedTab by remember { mutableStateOf(RecapTab.SUMMARY) }
    var currentSeekSec by remember { mutableStateOf<Int?>(null) }
    var copiedLabel by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // TTS Setup
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsPlaying by remember { mutableStateOf(false) }
    var ttsSpeed by remember { mutableStateOf(1.0f) }

    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH
            }
        }
        ttsEngine = tts
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    fun speakAudio(text: String) {
        ttsEngine?.let { engine ->
            if (isTtsPlaying) {
                engine.stop()
                isTtsPlaying = false
            } else {
                engine.setSpeechRate(ttsSpeed)
                engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "saar_tts")
                isTtsPlaying = true
            }
        }
    }

    fun openYouTubeAt(seconds: Int) {
        currentSeekSec = seconds
        val urlWithTime = "https://www.youtube.com/watch?v=${recap.videoId}&t=${seconds}s"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlWithTime))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Jumping to $seconds seconds", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(text: String, label: String = "Notes") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        copiedLabel = label
        Toast.makeText(context, "$label copied to clipboard! ✨", Toast.LENGTH_SHORT).show()
        coroutineScope.launch {
            kotlinx.coroutines.delay(2200)
            if (copiedLabel == label) {
                copiedLabel = null
            }
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
        // TOP NAV BAR
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ClayIconButton(
                        icon = Icons.Rounded.ArrowBack,
                        onClick = onBack,
                        size = 42.dp,
                        contentDescription = "Back"
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Saar Notes",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = clayColors.textPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClayIconButton(
                        icon = if (recap.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        onClick = onToggleFavorite,
                        tint = if (recap.isFavorite) clayColors.coral else clayColors.textPrimary,
                        size = 42.dp,
                        contentDescription = "Favorite"
                    )
                    ClayIconButton(
                        icon = Icons.Rounded.Share,
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "✨ Saar Notes for ${recap.title}:\n\nTL;DR: ${recap.tldr}\n\nWatch here: ${recap.videoUrl}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Saar Notes"))
                        },
                        size = 42.dp,
                        contentDescription = "Share"
                    )
                }
            }
        }

        // STICKY VIDEO CARD IN CLAY FRAME
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = clayColors.card,
                shape = ClayCardShapeLarge,
                elevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(clayColors.background)
                            .border(1.5.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(22.dp))
                            .clayClickable { openYouTubeAt(currentSeekSec ?: 0) },
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
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(clayColors.coral)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = "Play Video",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Duration Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = recap.durationText,
                                fontFamily = FredokaFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = recap.title,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        color = clayColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${recap.channel} • ${recap.category}",
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = clayColors.textSecondary
                        )

                        Text(
                            text = if (currentSeekSec != null) "Jumped to ${currentSeekSec}s" else "Tap video to play",
                            fontFamily = NunitoFontFamily,
                            fontSize = 11.sp,
                            color = clayColors.coral
                        )
                    }
                }
            }
        }

        // PUFFY SEGMENTED TABS
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(RecapTab.values()) { tab ->
                    ClayPill(
                        text = tab.title,
                        isSelected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        tintColor = when (tab) {
                            RecapTab.SUMMARY -> clayColors.peach
                            RecapTab.NOTES -> clayColors.mint
                            RecapTab.FLOWCHART -> clayColors.butter
                            RecapTab.TABLE -> clayColors.sky
                            RecapTab.LISTEN -> clayColors.lavender
                            RecapTab.QUIZ -> clayColors.coral
                        }
                    )
                }
            }
        }

        // TAB CONTENT
        when (selectedTab) {
            RecapTab.SUMMARY -> {
                // TL;DR Card
                item {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = clayColors.butter.copy(alpha = 0.5f),
                        shape = ClayCardShapeLarge,
                        elevation = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ClaySparkleBadge(text = "TL;DR (The Saar) 💡")

                                ClayButton(
                                    text = if (copiedLabel == "Summary") "Copied! ✓" else "Copy Summary",
                                    onClick = { copyToClipboard(recap.tldr, "Summary") },
                                    icon = if (copiedLabel == "Summary") Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                                    backgroundColor = if (copiedLabel == "Summary") clayColors.deepGreen else clayColors.mint,
                                    textColor = if (copiedLabel == "Summary") Color.White else clayColors.textPrimary,
                                    elevation = 2.dp,
                                    shape = CircleShape
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = recap.tldr,
                                fontFamily = NunitoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = clayColors.textPrimary
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Key Takeaways",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = clayColors.textPrimary
                    )
                }

                itemsIndexed(recap.keyTakeaways) { index, takeaway ->
                    val tint = listOf(clayColors.mint, clayColors.peach, clayColors.sky, clayColors.butter, clayColors.lavender)[index % 5]
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = tint.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(20.dp),
                        elevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(tint),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontFamily = FredokaFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = clayColors.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = takeaway,
                                fontFamily = NunitoFontFamily,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = clayColors.textPrimary
                            )
                        }
                    }
                }
            }

            RecapTab.NOTES -> {
                items(recap.notes) { section ->
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
                                Text(
                                    text = section.heading,
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = clayColors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )

                                // Clickable Timestamp Pill
                                Box(
                                    modifier = Modifier
                                        .clayClickable { openYouTubeAt(section.timestampSec) }
                                        .clip(CircleShape)
                                        .background(clayColors.mint)
                                        .border(1.dp, Color.White, CircleShape)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.PlayArrow,
                                            contentDescription = null,
                                            tint = clayColors.deepGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = section.timestampFormatted,
                                            fontFamily = FredokaFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = clayColors.deepGreen
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            section.bullets.forEach { bullet ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 7.dp)
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(clayColors.coral)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = bullet,
                                        fontFamily = NunitoFontFamily,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        color = clayColors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    ClayButton(
                        text = if (copiedLabel == "Markdown Notes") "Markdown Copied! ✓" else "Copy Full Notes (Markdown)",
                        onClick = {
                            val md = buildString {
                                appendLine("# ${recap.title}")
                                appendLine("Channel: ${recap.channel} | Video: ${recap.videoUrl}\n")
                                appendLine("## TL;DR\n${recap.tldr}\n")
                                appendLine("## Notes")
                                recap.notes.forEach { s ->
                                    appendLine("\n### ${s.heading} [${s.timestampFormatted}]")
                                    s.bullets.forEach { b -> appendLine("- $b") }
                                }
                            }
                            copyToClipboard(md, "Markdown Notes")
                        },
                        icon = if (copiedLabel == "Markdown Notes") Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                        backgroundColor = if (copiedLabel == "Markdown Notes") clayColors.mint else clayColors.deepGreen,
                        textColor = if (copiedLabel == "Markdown Notes") clayColors.deepGreen else Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            RecapTab.FLOWCHART -> {
                item {
                    DiagramView(data = recap.flowchart)
                }
            }

            RecapTab.TABLE -> {
                items(recap.tables) { table ->
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = clayColors.card,
                        shape = ClayCardShapeLarge,
                        elevation = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = table.title,
                                    fontFamily = FredokaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = clayColors.textPrimary
                                )

                                ClayIconButton(
                                    icon = if (copiedLabel == "CSV Data") Icons.Rounded.Check else Icons.Rounded.Download,
                                    onClick = {
                                        val csv = buildString {
                                            appendLine(table.columns.joinToString(","))
                                            table.rows.forEach { row ->
                                                appendLine(row.joinToString(",") { "\"$it\"" })
                                            }
                                        }
                                        copyToClipboard(csv, "CSV Data")
                                    },
                                    size = 36.dp,
                                    backgroundColor = if (copiedLabel == "CSV Data") clayColors.mint else clayColors.sky,
                                    tint = if (copiedLabel == "CSV Data") clayColors.deepGreen else clayColors.textPrimary,
                                    contentDescription = "Copy CSV"
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(clayColors.sky)
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                table.columns.forEach { col ->
                                    Text(
                                        text = col,
                                        fontFamily = FredokaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = clayColors.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Table Rows with Zebra Tint
                            table.rows.forEachIndexed { rIndex, row ->
                                val zebraBg = if (rIndex % 2 == 0) clayColors.background.copy(alpha = 0.5f) else Color.Transparent
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(zebraBg)
                                        .padding(vertical = 10.dp, horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    row.forEach { cell ->
                                        Text(
                                            text = cell,
                                            fontFamily = NunitoFontFamily,
                                            fontSize = 12.sp,
                                            color = clayColors.textPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            RecapTab.LISTEN -> {
                item {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = clayColors.lavender.copy(alpha = 0.55f),
                        shape = ClayCardShapeLarge,
                        elevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ClayStarMascotImage(size = 80.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Audio Essence Summary",
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = clayColors.textPrimary
                            )
                            Text(
                                text = "Conversational audio written to be heard",
                                fontFamily = NunitoFontFamily,
                                fontSize = 12.sp,
                                color = clayColors.textSecondary
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Animated sound wave
                            ClaySoundWave(
                                isPlaying = isTtsPlaying,
                                color = clayColors.coral,
                                modifier = Modifier.height(36.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Play / Pause Candy Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ClayButton(
                                    text = if (isTtsPlaying) "Pause Audio" else "Play Audio Summary",
                                    onClick = { speakAudio(recap.audioScript) },
                                    icon = if (isTtsPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    backgroundColor = clayColors.coral,
                                    textColor = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Speed Selector
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                    ClayPill(
                                        text = "${speed}x",
                                        isSelected = ttsSpeed == speed,
                                        onClick = {
                                            ttsSpeed = speed
                                            ttsEngine?.setSpeechRate(speed)
                                        },
                                        tintColor = clayColors.mint
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Transcript text
                            ClayCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = Color.White.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(20.dp),
                                elevation = 2.dp
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Audio Script:",
                                        fontFamily = FredokaFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = clayColors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = recap.audioScript,
                                        fontFamily = NunitoFontFamily,
                                        fontSize = 14.sp,
                                        lineHeight = 21.sp,
                                        color = clayColors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            RecapTab.QUIZ -> {
                // Interactive MCQs
                item {
                    Text(
                        text = "5-Question Active Recall Quiz",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = clayColors.textPrimary
                    )
                }

                itemsIndexed(recap.quiz) { qIndex, quizItem ->
                    InteractiveQuizCard(quizItem = quizItem, questionNumber = qIndex + 1)
                }

                // Interactive 3D Flashcards Deck
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Interactive Flashcards (${recap.flashcards.size})",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = clayColors.textPrimary
                    )
                }

                items(recap.flashcards) { card ->
                    Interactive3DFlashcard(card = card)
                }
            }
        }
    }
}

@Composable
private fun InteractiveQuizCard(
    quizItem: QuizItem,
    questionNumber: Int
) {
    val clayColors = LocalClayColors.current
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = clayColors.card,
        shape = ClayCardShapeLarge,
        elevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ClaySparkleBadge(
                    text = "Q$questionNumber",
                    backgroundColor = clayColors.butter
                )
                if (selectedOptionIndex != null) {
                    val isCorrect = selectedOptionIndex == quizItem.answerIndex
                    Text(
                        text = if (isCorrect) "Correct! 🎉" else "Not quite 💭",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isCorrect) clayColors.deepGreen else clayColors.coral
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = quizItem.question,
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                color = clayColors.textPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            quizItem.options.forEachIndexed { optIndex, optText ->
                val isSelected = selectedOptionIndex == optIndex
                val isCorrectAnswer = optIndex == quizItem.answerIndex

                val optBg = when {
                    selectedOptionIndex == null -> Color.White.copy(alpha = 0.8f)
                    isSelected && isCorrectAnswer -> clayColors.mint
                    isSelected && !isCorrectAnswer -> clayColors.peach
                    !isSelected && isCorrectAnswer -> clayColors.mint.copy(alpha = 0.5f)
                    else -> Color.White.copy(alpha = 0.5f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(optBg)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) clayColors.coral else Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clayClickable {
                            if (selectedOptionIndex == null) {
                                selectedOptionIndex = optIndex
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) clayColors.coral else clayColors.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${('A' + optIndex)}",
                                fontFamily = FredokaFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else clayColors.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = optText,
                            fontFamily = NunitoFontFamily,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = clayColors.textPrimary
                        )
                    }
                }
            }

            // Explanation
            AnimatedVisibility(visible = selectedOptionIndex != null) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(clayColors.background)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "💡 Why: ${quizItem.why}",
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            color = clayColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Interactive3DFlashcard(card: FlashcardItem) {
    val clayColors = LocalClayColors.current
    var isFlipped by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardFlip"
    )

    ClayCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        onClick = { isFlipped = !isFlipped },
        backgroundColor = if (isFlipped) clayColors.mint.copy(alpha = 0.5f) else clayColors.card,
        shape = ClayCardShapeLarge,
        elevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // FRONT
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ClaySparkleBadge(text = card.topic, backgroundColor = clayColors.butter)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = card.front,
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = clayColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to flip ✨",
                        fontFamily = NunitoFontFamily,
                        fontSize = 11.sp,
                        color = clayColors.textSecondary
                    )
                }
            } else {
                // BACK (mirrored back so text is normal)
                Column(
                    modifier = Modifier.graphicsLayer { rotationY = 180f },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Answer / Essence",
                        fontFamily = FredokaFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = clayColors.deepGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = card.back,
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = clayColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to flip back",
                        fontFamily = NunitoFontFamily,
                        fontSize = 11.sp,
                        color = clayColors.textSecondary
                    )
                }
            }
        }
    }
}
