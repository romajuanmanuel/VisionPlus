# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

VisionPlus: Android app (Kotlin, Jetpack Compose) that recognizes objects through the camera and shows information about them, eventually with ARCore augmented reality. Single module `:app`, package `com.visionR.visionplus`. The user is new to this field and wants small, verifiable steps, so confirm each stage works before starting the next. Replies to the user are in Spanish.

## Commands

`JAVA_HOME` is not set in the shell. Use Android Studio's bundled JDK:

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug
```

- Same prefix for other tasks: `testDebugUnitTest`, `connectedDebugAndroidTest` (needs a device).
- Single unit test: `./gradlew :app:testDebugUnitTest --tests "com.visionR.visionplus.ExampleUnitTest"`.
- A full build takes 3-4 minutes. Configuration cache is on.
- Test device: Xiaomi Redmi Note 13 Pro 5G (physical, needed for camera and ARCore).

## Build setup

- AGP 9.4.1 has built-in Kotlin: do not add the `kotlin-android` plugin. Only `kotlin.plugin.compose` is applied.
- All versions live in `gradle/libs.versions.toml`; check Google Maven for real latest versions rather than guessing (CameraX is on stable 1.6.2, not the 1.7 alpha).
- compileSdk/targetSdk 37, minSdk 24 (enough for CameraX, ML Kit, ARCore). Release has `optimization.enable = false`; revisit R8 rules for ML Kit/ARCore before shipping.
- Hilt/KSP compatibility with AGP 9 is unverified; DI is undecided.

## Architecture (planned)

Layering is MVVM with unidirectional data flow, packages by layer inside one module: `data/` (detector, info repository), `domain/` (models, use cases), `camera/`, `ar/`, `ui/<feature>/`. Currently only `ui/scanner/ScannerScreen.kt` (camera permission + CameraX `PreviewView` in `AndroidView`) and `ui/theme/` exist.

Key decisions that span several files:
- The object detector must sit behind an interface taking an image, so the implementation (ML Kit now, MediaPipe maybe later) and the frame source can be swapped.
- CameraX and ARCore cannot share the camera. Stages 1-4 use CameraX; the AR stage switches to ARCore as the only camera source, feeding `frame.acquireCameraImage()` to the same detector and anchoring labels with hit-tests.
- ML Kit Object Detection only returns boxes plus 5 coarse categories. Plan for named objects: Object Detection finds/boxes, Image Labeling names the crop. Do labels first (stage 2a), boxes after (2b).
- Analysis must run off the main thread with `STRATEGY_KEEP_ONLY_LATEST`; throttle detection on AR frames.

Staged plan: 0-1 dependencies + camera preview (done, awaiting device check), 2 ML Kit analysis, 3 2D overlay with rotation/scale coordinate transform, 4 local info (JSON in assets), 5-7 ARCore, 8 optional MediaPipe, 9 polish/release.
