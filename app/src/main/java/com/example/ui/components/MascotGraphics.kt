package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.clay.ClayCardShape
import com.example.ui.clay.clayClickable
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.LocalClayColors

@Composable
fun ClayStudentAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 58.dp,
    borderWidth: Dp = 2.5.dp,
    onClick: (() -> Unit)? = null
) {
    val clayColors = LocalClayColors.current

    val baseMod = if (onClick != null) {
        modifier.clayClickable(onClick = onClick)
    } else {
        modifier
    }

    Box(
        modifier = baseMod
            .size(size)
            .shadow(6.dp, CircleShape, ambientColor = clayColors.shadowColor, spotColor = clayColors.shadowColor)
            .clip(CircleShape)
            .background(clayColors.card)
            .border(borderWidth, Color.White.copy(alpha = 0.9f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.clay_student),
            contentDescription = "Student Mascot Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ClayStarMascotImage(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    isAnimated: Boolean = true
) {
    val clayColors = LocalClayColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "starBobbing")

    val bobbingY by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = -3f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bobY"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Box(
        modifier = modifier
            .graphicsLayer { translationY = bobbingY }
            .size(size)
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = clayColors.shadowColor)
            .clip(RoundedCornerShape(20.dp))
            .background(clayColors.card)
            .border(1.5.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.saar_icon_fg),
            contentDescription = "Saar Star Mascot",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ClayPlantImage(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp
) {
    val clayColors = LocalClayColors.current

    Box(
        modifier = modifier
            .size(size)
            .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = clayColors.shadowColor)
            .clip(RoundedCornerShape(22.dp))
            .background(clayColors.card)
            .border(1.5.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.clay_plant),
            contentDescription = "Potted Plant Illustration",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ClaySparkleBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null
) {
    val clayColors = LocalClayColors.current
    val bg = backgroundColor ?: clayColors.butter

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = FredokaFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = clayColors.textPrimary
        )
    }
}
