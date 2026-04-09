package com.petal.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CyclePatternFlag(
    val id: String,
    val title: String,
    val detail: String,
    val severity: FlagSeverity
)

@Serializable
enum class FlagSeverity(val display: String) {
    Info("info"),
    Watch("watch"),
    Care("care");
}
