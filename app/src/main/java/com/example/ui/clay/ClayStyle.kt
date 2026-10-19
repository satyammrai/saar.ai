package com.example.ui.clay

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalClayColors

val ClayCardShape = RoundedCornerShape(28.dp)
val ClayCardShapeLarge = RoundedCornerShape(32.dp)
val ClayPillShape = CircleShape
val ClayBadgeShape = RoundedCornerShape(16.dp)

/**
 * Creates the signature Soft 3D Clay look:
 * - Soft layered drop shadow
 * - Glossy top highlight
 * - Bottom inner depth shade
 * - Subtle 1px translucent glossy edge
 */
@Composable
fun Modifier.claySurface(
    shape: Shape = ClayCardShape,
    backgroundColor: Color? = null,
    elevation: Dp = 8.dp,
    borderWidth: Dp = 1.dp,
    hasHighlight: Boolean = true
): Modifier {
    val clayColors = LocalClayColors.current
    val bg = backgroundColor ?: clayColors.card
    val shadowColor = clayColors.shadowColor

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
        .clip(shape)
        .background(bg)
        .drawBehind {
            if (hasHighlight) {
                // Top glossy reflection
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (clayColors.isDark) 0.14f else 0.55f),
                            Color.Transparent,
                            Color.Black.copy(alpha = if (clayColors.isDark) 0.20f else 0.06f)
                        ),
                        startY = 0f,
                        endY = size.height
                    )
                )
            }
        }
        .border(
            width = borderWidth,
            color = clayColors.glossyBorder,
            shape = shape
        )
}

/**
 * Pressable clay candy surface with spring physics, hover lift, Hand cursor, and visible ripple.
 */
@Composable
fun Modifier.clayClickableSurface(
    shape: Shape = ClayCardShape,
    backgroundColor: Color? = null,
    elevation: Dp = 8.dp,
    borderWidth: Dp = 1.dp,
    hasHighlight: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    val clayColors = LocalClayColors.current
    val bg = backgroundColor ?: clayColors.card
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.93f
            isHovered -> 1.03f
            else -> 1.0f
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "clayScale"
    )
    val translationY by animateDpAsState(
        targetValue = when {
            isPressed -> 2.5.dp
            isHovered -> (-2).dp
            else -> 0.dp
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "clayTranslationY"
    )
    val dynamicElevation = when {
        isPressed -> (elevation - 3.dp).coerceAtLeast(1.dp)
        isHovered -> elevation + 3.dp
        else -> elevation
    }

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.translationY = translationY.toPx()
        }
        .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
        .claySurface(
            shape = shape,
            backgroundColor = if (isHovered) bg.copy(alpha = 0.94f) else bg,
            elevation = dynamicElevation,
            borderWidth = borderWidth,
            hasHighlight = hasHighlight
        )
        .clip(shape)
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true, color = if (clayColors.isDark) Color.White else Color(0x33000000)),
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Clickable modifier for arbitrary components with spring physics, hand cursor, and ripple
 */
@Composable
fun Modifier.clayClickable(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.93f
            isHovered -> 1.04f
            else -> 1.0f
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "clayScale"
    )
    val translationY by animateDpAsState(
        targetValue = when {
            isPressed -> 2.5.dp
            isHovered -> (-2).dp
            else -> 0.dp
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "clayTranslationY"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.translationY = translationY.toPx()
        }
        .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true),
            enabled = enabled,
            onClick = onClick
        )
}
