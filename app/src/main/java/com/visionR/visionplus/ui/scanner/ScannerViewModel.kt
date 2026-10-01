package com.visionR.visionplus.ui.scanner

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionR.visionplus.data.detection.MlKitObjectScanner
import com.visionR.visionplus.data.detection.ObjectScanner
import com.visionR.visionplus.domain.model.ScanResult
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScannerViewModel : ViewModel() {

    private val scanner: ObjectScanner = MlKitObjectScanner()

    private val _result = MutableStateFlow<ScanResult?>(null)
    val result: StateFlow<ScanResult?> = _result.asStateFlow()

    val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private val busy = AtomicBoolean(false)
    private var lastAnalysisTime = 0L

    /** Takes at most one frame every [ANALYSIS_INTERVAL_MS] and never overlaps two scans. */
    val analyzer = ImageAnalysis.Analyzer { frame ->
        val now = SystemClock.elapsedRealtime()
        if (now - lastAnalysisTime < ANALYSIS_INTERVAL_MS || !busy.compareAndSet(false, true)) {
            frame.close()
            return@Analyzer
        }
        lastAnalysisTime = now
        val bitmap = frame.toUprightBitmap()
        viewModelScope.launch(Dispatchers.Default) {
            try {
                _result.value = scanner.scan(bitmap)
            } catch (e: Exception) {
                Log.w(TAG, "Scan failed", e)
            } finally {
                busy.set(false)
            }
        }
    }

    private fun ImageProxy.toUprightBitmap(): Bitmap = use {
        val raw = toBitmap()
        val rotation = imageInfo.rotationDegrees
        if (rotation == 0) raw
        else Bitmap.createBitmap(
            raw, 0, 0, raw.width, raw.height,
            Matrix().apply { postRotate(rotation.toFloat()) },
            true
        )
    }

    override fun onCleared() {
        analysisExecutor.shutdown()
        scanner.close()
    }

    private companion object {
        const val TAG = "ScannerViewModel"
        const val ANALYSIS_INTERVAL_MS = 300L
    }
}
