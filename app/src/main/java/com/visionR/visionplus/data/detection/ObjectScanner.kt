package com.visionR.visionplus.data.detection

import android.graphics.Bitmap
import com.visionR.visionplus.domain.model.ScanResult

/** Finds objects in an upright frame and names each one. */
interface ObjectScanner {
    suspend fun scan(frame: Bitmap): ScanResult
    fun close()
}
