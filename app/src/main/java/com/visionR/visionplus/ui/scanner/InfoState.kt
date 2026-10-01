package com.visionR.visionplus.ui.scanner

import com.visionR.visionplus.domain.model.ObjectInfo

sealed interface InfoState {
    val name: String

    data class Loading(override val name: String) : InfoState
    data class Loaded(override val name: String, val info: ObjectInfo) : InfoState
    data class NotFound(override val name: String) : InfoState
    data class Error(override val name: String) : InfoState
}
