package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.clay.*
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.LocalClayColors
import com.example.ui.theme.NunitoFontFamily

@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    shape: Shape = ClayCardShape,
    elevation: Dp = 8.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val appliedModifier = if (onClick != null) {
        modifier.clayClickableSurface(
            shape = shape,
            backgroundColor = backgroundColor,
            elevation = elevation,
            onClick = onClick
        )
    } else {
        modifier.claySurface(
            shape = shape,
            backgroundColor = backgroundColor,
            elevation = elevation
        )
    }

    Box(
        modifier = appliedModifier,
        content = content
    )
}

@Composable
fun ClayButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    backgroundColor: Color? = null,
    textColor: Color = Color.White,
    elevation: Dp = 6.dp,
    shape: Shape = CircleShape,
    testTag: String = "clay_button"
) {
    val clayColors = LocalClayColors.current
    val bg = backgroundColor ?: clayColors.coral

    Box(
        modifier = modifier
            .testTag(testTag)
            .clayClickableSurface(
                shape = shape,
                backgroundColor = bg,
                elevation = elevation,
                onClick = onClick
            )
            .padding(horizontal = 22.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = textColor
            )
        }
    }
}

@Composable
fun ClayIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    tint: Color? = null,
    size: Dp = 44.dp,
    elevation: Dp = 4.dp,
    contentDescription: String? = null,
    badgeText: String? = null
) {
    val clayColors = LocalClayColors.current
    val bg = backgroundColor ?: clayColors.card
    val iconTint = tint ?: clayColors.textPrimary

    Box(
        modifier = modifier
            .size(size)
            .clayClickableSurface(
                shape = CircleShape,
                backgroundColor = bg,
                elevation = elevation,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(size * 0.52f)
        )
        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .clip(CircleShape)
                    .background(clayColors.coral)
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    fontFamily = FredokaFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ClayPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tintColor: Color? = null
) {
    val clayColors = LocalClayColors.current
    val activeBg = tintColor ?: clayColors.mint
    val inactiveBg = clayColors.card

    Box(
        modifier = modifier
            .clayClickableSurface(
                shape = CircleShape,
                backgroundColor = if (isSelected) activeBg else inactiveBg,
                elevation = if (isSelected) 6.dp else 2.dp,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = clayColors.textPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontFamily = FredokaFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                color = clayColors.textPrimary
            )
        }
    }
}

@Composable
fun ClayInputPill(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    buttonText: String = "Make my notes",
    showPasteButton: Boolean = true
) {
    val clayColors = LocalClayColors.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current

    fun performPaste() {
        val clip = clipboardManager.getText()?.text
        if (!clip.isNullOrBlank()) {
            onValueChange(clip.trim())
            android.widget.Toast.makeText(context, "Pasted link from clipboard! 📋", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            android.widget.Toast.makeText(context, "Clipboard empty. Tap the GATE 2027 button below to test!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .claySurface(
                shape = CircleShape,
                backgroundColor = clayColors.card,
                elevation = 8.dp
            )
            .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = clayColors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontFamily = NunitoFontFamily,
                        fontSize = 13.sp,
                        color = clayColors.textSecondary.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    cursorBrush = SolidColor(clayColors.coral),
                    textStyle = TextStyle(
                        fontFamily = NunitoFontFamily,
                        fontSize = 13.sp,
                        color = clayColors.textPrimary,
                        fontWeight = FontWeight.Medium
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerHoverIcon(PointerIcon.Text)
                        .testTag("youtube_url_input")
                )
            }

            // Quick Clear Button if text is present
            if (value.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clayClickableSurface(
                            shape = CircleShape,
                            backgroundColor = clayColors.background,
                            elevation = 2.dp,
                            onClick = { onValueChange("") }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Clear input",
                        tint = clayColors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            } else if (showPasteButton) {
                // Quick Paste Button inside pill
                Box(
                    modifier = Modifier
                        .clayClickableSurface(
                            shape = CircleShape,
                            backgroundColor = clayColors.mint,
                            elevation = 3.dp,
                            onClick = { performPaste() }
                        )
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.ContentPaste,
                            contentDescription = "Paste",
                            tint = clayColors.deepGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Paste",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = clayColors.deepGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            ClayButton(
                text = buttonText,
                onClick = onSubmit,
                backgroundColor = clayColors.coral,
                textColor = Color.White,
                icon = Icons.Rounded.ArrowUpward,
                testTag = "submit_notes_button"
            )
        }
    }
}

@Composable
fun ClayStatTile(
    title: String,
    value: String,
    delta: String,
    tintColor: Color,
    iconContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val clayColors = LocalClayColors.current

    ClayCard(
        modifier = modifier,
        backgroundColor = tintColor,
        shape = RoundedCornerShape(24.dp),
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.65f))
                        .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    iconContent()
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = delta,
                        fontFamily = FredokaFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = clayColors.deepGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = value,
                fontFamily = FredokaFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = clayColors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontFamily = NunitoFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = clayColors.textSecondary
            )
        }
    }
}

@Composable
fun ClaySoundWave(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 16,
    color: Color = Color.White
) {
    val infiniteTransition = rememberInfiniteTransition(label = "soundWave")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val duration = 400 + (i % 5) * 120
            val heightPercent by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(duration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "barHeight$i"
            )

            val currentHeight = if (isPlaying) (8 + heightPercent * 24).dp else 6.dp

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(currentHeight)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
