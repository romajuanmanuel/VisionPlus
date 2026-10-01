package com.visionR.visionplus.ui.scanner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visionR.visionplus.domain.model.ScanResult
import com.visionR.visionplus.domain.model.ScannedObject
import kotlin.math.max

private val BoxColor = Color(0xFFFFD600)

/**
 * Draws a yellow box per detected object and reports taps on them. PreviewView centers
 * and crops the camera image to fill the screen (FILL_CENTER), so boxes get the same
 * scale and offset.
 */
@Composable
fun DetectionOverlay(
    result: ScanResult?,
    onObjectTap: (ScannedObject) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(result) {
                detectTapGestures { tap ->
                    result?.objects
                        ?.lastOrNull { it.screenRect(result, size.width.toFloat(), size.height.toFloat()).contains(tap) }
                        ?.let(onObjectTap)
                }
            }
    ) {
        if (result == null || result.frameWidth == 0 || result.frameHeight == 0) return@Canvas

        result.objects.forEach { obj ->
            val rect = obj.screenRect(result, size.width, size.height)
            drawRect(BoxColor, rect.topLeft, rect.size, style = Stroke(width = 4.dp.toPx()))

            val text = obj.name?.let { "$it ${(obj.confidence * 100).toInt()}%" } ?: "Objeto"
            drawText(
                textMeasurer = textMeasurer,
                text = text,
                topLeft = Offset(rect.left + 4.dp.toPx(), rect.top + 4.dp.toPx()),
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 16.sp,
                    background = BoxColor
                )
            )
        }
    }
}

private fun ScannedObject.screenRect(result: ScanResult, viewWidth: Float, viewHeight: Float): Rect {
    val scale = max(viewWidth / result.frameWidth, viewHeight / result.frameHeight)
    val offsetX = (viewWidth - result.frameWidth * scale) / 2f
    val offsetY = (viewHeight - result.frameHeight * scale) / 2f
    return Rect(
        left = box.left * scale + offsetX,
        top = box.top * scale + offsetY,
        right = box.right * scale + offsetX,
        bottom = box.bottom * scale + offsetY
    )
}
