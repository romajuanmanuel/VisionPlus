package com.visionR.visionplus.domain.model

import android.graphics.Rect

/** An object found in a frame. [box] is in the coordinates of the upright (rotated) frame. */
data class ScannedObject(
    val box: Rect,
    val name: String?,
    val confidence: Float
)

/** Objects found in one frame, plus the upright frame size needed to map [ScannedObject.box] to the screen. */
data class ScanResult(
    val frameWidth: Int,
    val frameHeight: Int,
    val objects: List<ScannedObject>
)
