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
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val arrowWidthPx = with(density) { arrowWidth.toPx() }
        val arrowHeightPx = with(density) { arrowHeight.toPx() }
        val arrowOffsetPx = with(density) { arrowOffset.toPx() }
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }

        val path = Path().apply {
            // Start after the top-left corner
            moveTo(cornerRadiusPx, arrowHeightPx)

            // Top edge before the arrow
            lineTo(arrowOffsetPx - (arrowWidthPx / 2), arrowHeightPx)

            // Upward arrow peak
            lineTo(arrowOffsetPx, 0f)

            // Right side of arrow back to top edge
            lineTo(arrowOffsetPx + (arrowWidthPx / 2), arrowHeightPx)

            // Rest of top edge to top-right corner
            lineTo(size.width - cornerRadiusPx, arrowHeightPx)

            // Top-right corner
            arcTo(
                rect = Rect(
                    left = size.width - (2 * cornerRadiusPx),
                    top = arrowHeightPx,
                    right = size.width,
                    bottom = arrowHeightPx + (2 * cornerRadiusPx)
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Right edge down to bottom-right corner
            lineTo(size.width, size.height - cornerRadiusPx)

            // Bottom-right corner
            arcTo(
                rect = Rect(
                    left = size.width - (2 * cornerRadiusPx),
                    top = size.height - (2 * cornerRadiusPx),
                    right = size.width,
                    bottom = size.height
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Bottom edge to bottom-left corner
            lineTo(cornerRadiusPx, size.height)

            // Bottom-left corner
            arcTo(
                rect = Rect(
                    left = 0f,
                    top = size.height - (2 * cornerRadiusPx),
                    right = 2 * cornerRadiusPx,
                    bottom = size.height
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Left edge up to top-left corner
            lineTo(0f, arrowHeightPx + cornerRadiusPx)

            // Top-left corner
            arcTo(
                rect = Rect(
                    left = 0f,
                    top = arrowHeightPx,
                    right = 2 * cornerRadiusPx,
                    bottom = arrowHeightPx + (2 * cornerRadiusPx)
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            close()
        }

        return Outline.Generic(path)
    }
}