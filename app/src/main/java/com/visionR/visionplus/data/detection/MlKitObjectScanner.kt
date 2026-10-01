package com.visionR.visionplus.data.detection

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.visionR.visionplus.domain.model.ScanResult
import com.visionR.visionplus.domain.model.ScannedObject
import kotlinx.coroutines.tasks.await

/**
 * Two steps: Object Detection finds where each object is, then Image Labeling
 * names only the pixels inside that box.
 */
class MlKitObjectScanner : ObjectScanner {

    private val detector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .build()
    )

    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(MIN_CONFIDENCE)
            .build()
    )

    override suspend fun scan(frame: Bitmap): ScanResult {
        val detected = detector.process(InputImage.fromBitmap(frame, 0)).await()
        val objects = detected.take(MAX_OBJECTS).map { obj ->
            val box = obj.boundingBox.clampTo(frame.width, frame.height)
            val best = labeler.process(InputImage.fromBitmap(frame.crop(box), 0))
                .await()
                .maxByOrNull { it.confidence }
            ScannedObject(box, best?.text, best?.confidence ?: 0f)
        }
        return ScanResult(frame.width, frame.height, objects)
    }

    override fun close() {
        detector.close()
        labeler.close()
    }

    private fun Rect.clampTo(width: Int, height: Int) = Rect(
        left.coerceIn(0, width - 1),
        top.coerceIn(0, height - 1),
        right.coerceIn(1, width),
        bottom.coerceIn(1, height)
    )

    private fun Bitmap.crop(box: Rect): Bitmap =
        Bitmap.createBitmap(this, box.left, box.top, box.width(), box.height())

    private companion object {
        const val MIN_CONFIDENCE = 0.5f
        const val MAX_OBJECTS = 3
    }
}
