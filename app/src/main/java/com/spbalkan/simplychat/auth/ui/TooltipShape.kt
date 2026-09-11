package com.spbalkan.simplychat.auth.ui

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class TooltipShape(
    // values for top arrow tip drawing
    private val arrowWidth: Dp = 12.dp,
    private val arrowHeight: Dp = 8.dp,
    private val arrowOffset: Dp = 60.dp,
    private val cornerRadius: Dp = 16.dp
) : Shape {

    private val path = Path()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val arrowWidthPx = with(density) { arrowWidth.toPx() }
        val arrowHeightPx = with(density) { arrowHeight.toPx() }
        val arrowOffsetPx = with(density) { arrowOffset.toPx() }
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }

        path.reset()

        val width = size.width
        val height = size.height

        // Top-left start (after corner)
        path.moveTo(cornerRadiusPx, arrowHeightPx)

        // Arrow
        path.lineTo(arrowOffsetPx - (arrowWidthPx / 2f), arrowHeightPx)
        path.lineTo(arrowOffsetPx, 0f)
        path.lineTo(arrowOffsetPx + (arrowWidthPx / 2f), arrowHeightPx)

        // Top-right corner
        path.lineTo(width - cornerRadiusPx, arrowHeightPx)
        path.quadraticTo(width, arrowHeightPx, width, arrowHeightPx + cornerRadiusPx)

        // Bottom-right corner
        path.lineTo(width, height - cornerRadiusPx)
        path.quadraticTo(width, height, width - cornerRadiusPx, height)

        // Bottom-left corner
        path.lineTo(cornerRadiusPx, height)
        path.quadraticTo(0f, height, 0f, height - cornerRadiusPx)

        // Top-left corner
        path.lineTo(0f, arrowHeightPx + cornerRadiusPx)
        path.quadraticTo(0f, arrowHeightPx, cornerRadiusPx, arrowHeightPx)

        path.close()

        return Outline.Generic(path)
    }
}