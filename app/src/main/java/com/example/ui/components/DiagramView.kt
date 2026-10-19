package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiagramNode
import com.example.data.model.DiagramType
import com.example.data.model.FlowchartData
import com.example.ui.clay.ClayCardShape
import com.example.ui.clay.clayClickable
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.LocalClayColors
import com.example.ui.theme.NunitoFontFamily

@Composable
fun DiagramView(
    data: FlowchartData,
    modifier: Modifier = Modifier
) {
    val clayColors = LocalClayColors.current
    var selectedType by remember { mutableStateOf(data.diagramType) }
    var selectedNode by remember { mutableStateOf<DiagramNode?>(data.nodes.firstOrNull()) }

    var scale by remember { mutableStateOf(1.0f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    fun getTint(tintName: String): Color {
        return when (tintName.lowercase()) {
            "mint" -> clayColors.mint
            "peach" -> clayColors.peach
            "butter" -> clayColors.butter
            "sky" -> clayColors.sky
            "lavender" -> clayColors.lavender
            else -> clayColors.mint
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clayCardBackground(clayColors.card)
            .padding(18.dp)
    ) {
        // Top Toolbar: Diagram Type Switcher & Zoom Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    DiagramType.FLOWCHART to "Flowchart",
                    DiagramType.MINDMAP to "Mindmap",
                    DiagramType.TIMELINE to "Timeline"
                ).forEach { (type, label) ->
                    ClayPill(
                        text = label,
                        isSelected = selectedType == type,
                        onClick = { selectedType = type },
                        tintColor = clayColors.lavender
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ClayIconButton(
                    icon = Icons.Rounded.Remove,
                    onClick = { scale = (scale - 0.15f).coerceAtLeast(0.6f) },
                    size = 32.dp,
                    contentDescription = "Zoom out"
                )
                ClayIconButton(
                    icon = Icons.Rounded.Add,
                    onClick = { scale = (scale + 0.15f).coerceAtMost(2.2f) },
                    size = 32.dp,
                    contentDescription = "Zoom in"
                )
                ClayIconButton(
                    icon = Icons.Rounded.Refresh,
                    onClick = {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    },
                    size = 32.dp,
                    contentDescription = "Reset view"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Visual Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(clayColors.background.copy(alpha = 0.5f))
                .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.6f, 2.2f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
        ) {
            when (selectedType) {
                DiagramType.FLOWCHART -> FlowchartLayout(
                    nodes = data.nodes,
                    getTint = ::getTint,
                    selectedNode = selectedNode,
                    onNodeClick = { selectedNode = it }
                )
                DiagramType.MINDMAP -> MindmapLayout(
                    nodes = data.nodes,
                    getTint = ::getTint,
                    selectedNode = selectedNode,
                    onNodeClick = { selectedNode = it }
                )
                DiagramType.TIMELINE -> TimelineLayout(
                    nodes = data.nodes,
                    getTint = ::getTint,
                    selectedNode = selectedNode,
                    onNodeClick = { selectedNode = it }
                )
            }
        }

        // Selected Node Inspection Card
        AnimatedVisibility(visible = selectedNode != null) {
            val node = selectedNode
            if (node != null) {
                Spacer(modifier = Modifier.height(12.dp))
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = getTint(node.pastelTint).copy(alpha = 0.6f),
                    shape = RoundedCornerShape(20.dp),
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = node.label,
                                fontFamily = FredokaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = clayColors.textPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.7f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = node.nodeType.name,
                                    fontFamily = FredokaFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = clayColors.textPrimary
                                )
                            }
                        }
                        if (node.detail.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = node.detail,
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

@Composable
private fun FlowchartLayout(
    nodes: List<DiagramNode>,
    getTint: (String) -> Color,
    selectedNode: DiagramNode?,
    onNodeClick: (DiagramNode) -> Unit
) {
    val clayColors = LocalClayColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        nodes.forEachIndexed { index, node ->
            val isSelected = selectedNode?.id == node.id
            ClayNodePill(
                node = node,
                tint = getTint(node.pastelTint),
                isSelected = isSelected,
                onClick = { onNodeClick(node) },
                stepNumber = index + 1
            )
            if (index < nodes.size - 1) {
                // Soft arrow connector
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(18.dp)
                        .background(clayColors.coral.copy(alpha = 0.6f), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun MindmapLayout(
    nodes: List<DiagramNode>,
    getTint: (String) -> Color,
    selectedNode: DiagramNode?,
    onNodeClick: (DiagramNode) -> Unit
) {
    val clayColors = LocalClayColors.current
    val root = nodes.firstOrNull()
    val children = if (nodes.size > 1) nodes.drop(1) else emptyList()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (root != null) {
            ClayNodePill(
                node = root,
                tint = getTint(root.pastelTint),
                isSelected = selectedNode?.id == root.id,
                onClick = { onNodeClick(root) },
                isRoot = true
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            children.forEach { child ->
                ClayNodePill(
                    node = child,
                    tint = getTint(child.pastelTint),
                    isSelected = selectedNode?.id == child.id,
                    onClick = { onNodeClick(child) },
                    stepNumber = null
                )
            }
        }
    }
}

@Composable
private fun TimelineLayout(
    nodes: List<DiagramNode>,
    getTint: (String) -> Color,
    selectedNode: DiagramNode?,
    onNodeClick: (DiagramNode) -> Unit
) {
    val clayColors = LocalClayColors.current

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        nodes.forEachIndexed { index, node ->
            ClayNodePill(
                node = node,
                tint = getTint(node.pastelTint),
                isSelected = selectedNode?.id == node.id,
                onClick = { onNodeClick(node) },
                stepNumber = index + 1
            )
            if (index < nodes.size - 1) {
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .width(18.dp)
                        .background(clayColors.coral.copy(alpha = 0.6f), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun ClayNodePill(
    node: DiagramNode,
    tint: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    stepNumber: Int? = null,
    isRoot: Boolean = false
) {
    val clayColors = LocalClayColors.current

    Box(
        modifier = Modifier
            .clayClickable(onClick = onClick)
            .clip(CircleShape)
            .background(if (isSelected) tint else tint.copy(alpha = 0.75f))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) clayColors.coral else Color.White.copy(alpha = 0.85f),
                shape = CircleShape
            )
            .padding(horizontal = 14.dp, vertical = if (isRoot) 12.dp else 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (stepNumber != null) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$stepNumber",
                        fontFamily = FredokaFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = clayColors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = node.label,
                fontFamily = FredokaFontFamily,
                fontWeight = if (isSelected || isRoot) FontWeight.Bold else FontWeight.Medium,
                fontSize = if (isRoot) 14.sp else 12.sp,
                color = clayColors.textPrimary
            )
        }
    }
}

@Composable
private fun Modifier.clayCardBackground(color: Color): Modifier {
    val clayColors = LocalClayColors.current
    return this
        .clip(ClayCardShape)
        .background(color)
        .border(1.dp, clayColors.glossyBorder, ClayCardShape)
}
