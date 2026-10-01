package com.visionR.visionplus.ui.scanner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visionR.visionplus.domain.model.ScanResult
import kotlin.math.max

private val BoxColor = Color(0xFFFFD600)

/**
 * Draws a yellow box per detected object. PreviewView centers and crops the camera
 * image to fill the screen (FILL_CENTER), so boxes get the same scale and offset.
 */
@Composable
fun DetectionOverlay(result: ScanResult?, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {
        if (result == null || result.frameWidth == 0 || result.frameHeight == 0) return@Canvas

        val scale = max(size.width / result.frameWidth, size.height / result.frameHeight)
        val offsetX = (size.width - result.frameWidth * scale) / 2f
        val offsetY = (size.height - result.frameHeight * scale) / 2f

        result.objects.forEach { obj ->
            val topLeft = Offset(obj.box.left * scale + offsetX, obj.box.top * scale + offsetY)
            val boxSize = Size(obj.box.width() * scale, obj.box.height() * scale)
            drawRect(BoxColor, topLeft, boxSize, style = Stroke(width = 4.dp.toPx()))

            val text = obj.name?.let { "$it ${(obj.confidence * 100).toInt()}%" } ?: "Objeto"
            drawText(
                textMeasurer = textMeasurer,
                text = text,
                topLeft = Offset(topLeft.x + 4.dp.toPx(), topLeft.y + 4.dp.toPx()),
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 16.sp,
                    background = BoxColor
                )
            )
        }
    }
}
