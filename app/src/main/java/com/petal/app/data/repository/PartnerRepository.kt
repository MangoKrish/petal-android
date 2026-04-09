package com.petal.app.data.repository

import com.petal.app.data.model.PartnerConnection
import com.petal.app.data.model.PartnerStatus
import com.petal.app.data.model.SharePermissions
import com.petal.app.data.model.SharedLink
import com.petal.app.data.remote.PetalApiService
import com.petal.app.data.remote.dto.CreateShareLinkRequest
import com.petal.app.data.remote.dto.InvitePartnerRequest
import com.petal.app.data.remote.dto.UpdatePartnerRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PartnerRepository @Inject constructor(
    private val apiService: PetalApiService
) {
    suspend fun getPartnerConnections(): Result<List<PartnerConnection>> = try {
        val response = apiService.getPartnerConnections()
        if (response.isSuccessful) {
            val connections = response.body()?.map { dto ->
                PartnerConnection(
                    id = dto.id,
                    partnerName = dto.partnerName,
                    partnerEmail = dto.partnerEmail,
                    note = dto.note,
                    sharingEnabled = dto.sharingEnabled,
                    status = PartnerStatus.entries.firstOrNull { it.display == dto.status }
                        ?: PartnerStatus.Invited,
                    permissions = SharePermissions(
                        latestPeriod = dto.permissions.showNextPeriod,
                        cycleLength = dto.permissions.showCycleLength,
                        symptoms = dto.permissions.showSymptoms,
                        predictions = dto.permissions.showPhase
                    ),
                    isCaregiver = dto.isCaregiver,
                    createdAt = dto.createdAt,
                    updatedAt = dto.updatedAt
                )
            } ?: emptyList()
            Result.success(connections)
        } else {
            Result.failure(Exception("Failed to load partner connections."))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun invitePartner(
        name: String,
        email: String,
        note: String = "",
        isCaregiver: Boolean = false,
        permissions: SharePermissions = SharePermissions()
    ): Result<PartnerConnection> = try {
        val response = apiService.invitePartner(
            InvitePartnerRequest(
                partnerName = name,
                partnerEmail = email,
                note = note,
                isCaregiver = isCaregiver,
                showCycleLength = permissions.cycleLength,
                showNextPeriod = permissions.latestPeriod,
                showSymptoms = permissions.symptoms,
                showPhase = permissions.predictions
            )
        )
        if (response.isSuccessful) {
            val dto = response.body()!!
            Result.success(
                PartnerConnection(
                    id = dto.id,
                    partnerName = dto.partnerName,
                    partnerEmail = dto.partnerEmail,
                    note = dto.note,
                    sharingEnabled = dto.sharingEnabled,
                    status = PartnerStatus.entries.firstOrNull { it.display == dto.status }
                        ?: PartnerStatus.Invited,
                    isCaregiver = dto.isCaregiver,
                    permissions = SharePermissions(
                        latestPeriod = dto.permissions.showNextPeriod,
                        cycleLength = dto.permissions.showCycleLength,
                        symptoms = dto.permissions.showSymptoms,
                        predictions = dto.permissions.showPhase
                    ),
                    createdAt = dto.createdAt,
                    updatedAt = dto.updatedAt
                )
            )
        } else {
            Result.failure(Exception("Failed to send partner invitation."))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updatePartnerConnection(
        connectionId: String,
        sharingEnabled: Boolean? = null,
        permissions: SharePermissions? = null
    ): Result<Unit> = try {
        val response = apiService.updatePartnerConnection(
            connectionId,
            UpdatePartnerRequest(
                sharingEnabled = sharingEnabled,
                showCycleLength = permissions?.cycleLength,
                showNextPeriod = permissions?.latestPeriod,
                showSymptoms = permissions?.symptoms,
                showPhase = permissions?.predictions
            )
        )
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Failed to update connection."))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun removePartner(connectionId: String): Result<Unit> = try {
        val response = apiService.removePartner(connectionId)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Failed to remove partner."))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getShareLinks(): Result<List<SharedLink>> = try {
        val response = apiService.getShareLinks()
        if (response.isSuccessful) {
            val links = response.body()?.map { dto ->
                SharedLink(
                    id = dto.id,
                    token = dto.token,
                    label = dto.label,
                    showCycleLength = dto.showCycleLength,
                    showNextPeriod = dto.showNextPeriod,
                    showSymptoms = dto.showSymptoms,
                    showPhase = dto.showPhase,
                    active = dto.active,
                    createdAt = dto.createdAt
                )
            } ?: emptyList()
            Result.success(links)
        } else {
            Result.failure(Exception("Failed to load share links."))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createShareLink(
        label: String,
        showCycleLength: Boolean = true,
        showNextPeriod: Boolean = true,
        showSymptoms: Boolean = false,
        showPhase: Boolean = true
    ): Result<SharedLink> = try {
        val response = apiService.createShareLink(
            CreateShareLinkRequest(label, showCycleLength, showNextPeriod, showSymptoms, showPhase)
        )
        if (response.isSuccessful) {
            val dto = response.body()!!
            Result.success(
                SharedLink(
                    id = dto.id,
                    token = dto.token,
                    label = dto.label,
                    showCycleLength = dto.showCycleLength,
                    showNextPeriod = dto.showNextPeriod,
                    showSymptoms = dto.showSymptoms,
                    showPhase = dto.showPhase,
                    active = dto.active,
                    createdAt = dto.createdAt
                )
            )
        } else {
            Result.failure(Exception("Failed to create share link."))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun revokeShareLink(linkId: String): Result<Unit> = try {
        val response = apiService.revokeShareLink(linkId)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Failed to revoke share link."))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
