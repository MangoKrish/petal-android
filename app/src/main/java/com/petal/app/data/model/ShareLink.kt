package com.petal.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SharePermissions(
    val latestPeriod: Boolean = true,
    val cycleLength: Boolean = true,
    val symptoms: Boolean = false,
    val predictions: Boolean = true
)

@Serializable
data class SharedLink(
    val id: String,
    val token: String,
    val label: String,
    val showCycleLength: Boolean = true,
    val showNextPeriod: Boolean = true,
    val showSymptoms: Boolean = false,
    val showPhase: Boolean = true,
    val active: Boolean = true,
    val createdAt: String
)

@Serializable
data class PartnerConnection(
    val id: String,
    val partnerName: String,
    val partnerEmail: String,
    val note: String = "",
    val sharingEnabled: Boolean = true,
    val status: PartnerStatus = PartnerStatus.Invited,
    val permissions: SharePermissions = SharePermissions(),
    val isCaregiver: Boolean = false,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
enum class PartnerStatus(val display: String) {
    Invited("Invited"),
    Active("Active"),
    Paused("Paused");
}
