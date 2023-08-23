package co.nilin.opex.profile.app.controller

import co.nilin.opex.profile.app.service.LinkAccountManagement
import co.nilin.opex.profile.app.service.ProfileManagement
import co.nilin.opex.profile.core.data.limitation.*
import co.nilin.opex.profile.core.data.linkedbankAccount.LinkedAccountHistoryResponse
import co.nilin.opex.profile.core.data.linkedbankAccount.LinkedAccountResponse
import co.nilin.opex.profile.core.data.linkedbankAccount.LinkedBankAccountRequest
import co.nilin.opex.profile.core.data.linkedbankAccount.VerifyLinkedAccountRequest
import co.nilin.opex.profile.core.data.profile.Profile
import co.nilin.opex.profile.core.data.profile.ProfileHistory
import co.nilin.opex.profile.ports.postgres.imp.LimitationManagementImp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v2/admin/profile")

class ProfileAdminController(val profileManagement: ProfileManagement,
                             val linkAccountManagement: LinkAccountManagement,
                             val limitManagement: LimitationManagementImp) {

    @PostMapping("/{userId}")
    suspend fun createManually(@PathVariable("userId") userId: String, @RequestBody newProfile: Profile): Profile? {
        return profileManagement.create(userId, newProfile)
    }

    @PutMapping("/{userId}")
    suspend fun updateAsAdmin(@PathVariable("userId") userId: String, @RequestBody newProfile: Profile): Profile? {
        return profileManagement.updateAsAdmin(userId, newProfile)
    }

    @GetMapping("/history/{userId}")
    suspend fun getHistory(@PathVariable("userId") userId: String,
                           @RequestParam offset: Int?, @RequestParam size: Int?): List<ProfileHistory>? {
        return profileManagement.getHistory(userId, offset ?: 0, size ?: 1000)
    }

    @GetMapping("")
    suspend fun getProfiles(@RequestParam offset: Int?, @RequestParam size: Int?): List<Profile>? {
        return profileManagement.getAllProfiles(offset ?: 0, size ?: 1000)
    }


    @GetMapping("/{userId}")
    suspend fun getProfile(@PathVariable("userId") userId: String): Profile? {
        return profileManagement.getProfile(userId)
    }


    // =====================================linked accounts====================================

    @GetMapping("/linked-account/{userId}")
    suspend fun getLinkedAccount(@PathVariable userId: String): Flow<LinkedAccountResponse>? {
        return linkAccountManagement.getAccounts(userId)
    }

    @GetMapping("/linked-account/history/{accountId}")
    suspend fun getHistoryLinkedAccount(@PathVariable accountId: String): Flow<LinkedAccountHistoryResponse>? {

        return linkAccountManagement.getHistoryLinkedAccount(accountId)
    }

    @PostMapping("/linked-account/{userId}")
    suspend fun addLinkedAccount(@PathVariable userId: String,
                                 @RequestBody linkedBankAccountRequest: LinkedBankAccountRequest,
                                 @CurrentSecurityContext securityContext: SecurityContext): LinkedAccountResponse? {
        linkedBankAccountRequest.userId = userId
        linkedBankAccountRequest.description = "Inserted by admin: ${securityContext.authentication.name}"
        return linkAccountManagement.addNewAccount(linkedBankAccountRequest)?.awaitFirstOrNull()
    }

    @PutMapping("/linked-account/verify/{accountId}")
    suspend fun verifyLinkedAccount(@PathVariable accountId: String, @RequestBody verifyRequest: VerifyLinkedAccountRequest,
                                    @CurrentSecurityContext securityContext: SecurityContext): LinkedAccountResponse? {
        verifyRequest.accountId = accountId
        verifyRequest.verifier = securityContext.authentication.name
        return linkAccountManagement.verifyAccount(verifyRequest)?.awaitFirstOrNull()

    }

    //==============================================limitation services=================================================


    @PostMapping("/limitation")
    suspend fun updateLimitation(@RequestBody permissionRequest: UpdateLimitationRequest) {
        limitManagement.updateLimitation(permissionRequest)
    }

    @GetMapping("/limitation")
    suspend fun getLimitation(@RequestParam("userId") userId: String?,
                              @RequestParam("action") action: ActionType?,
                              @RequestParam("reason") reason: LimitationReason?,
                              @RequestParam("groupBy") groupBy: String?,
                              @RequestParam("size") size: Int?,
                              @RequestParam("offset") offset: Int?): LimitationResponse? {

        var res = limitManagement.getLimitation(userId, action, reason, offset ?: 0, size ?: 1000)

        return when (groupBy) {
            "user" -> LimitationResponse(res?.groupBy { r -> r.userId })
            "action" -> LimitationResponse(res?.groupBy { r -> r.actionType?.name })
            "reason" -> LimitationResponse(res?.groupBy { r -> (r.reason ?: LimitationReason.Other).name })
            else -> {
                LimitationResponse(totalData = res)
            }
        }

    }

    @GetMapping("/limitation/history")
    suspend fun getLimitationHistory(@RequestParam("userId") userId: String?,
                                     @RequestParam("action") action: ActionType?,
                                     @RequestParam("reason") reason: LimitationReason?,
                                     @RequestParam("groupBy") groupBy: String?,
                                     @RequestParam("size") size: Int?,
                                     @RequestParam("offset") offset: Int?): LimitationHistoryResponse? {

        var res = limitManagement.getLimitationHistory(userId, action, reason, offset ?: 0, size ?: 1000)
        return when (groupBy) {
            "user" -> LimitationHistoryResponse(res?.groupBy { r -> r.userId })
            "action" -> LimitationHistoryResponse(res?.groupBy { r -> r.actionType?.name })
            "reason" -> LimitationHistoryResponse(res?.groupBy { r -> (r.reason ?: LimitationReason.Other).name })
            else -> {
                LimitationHistoryResponse(totalData = res)
            }
        }
    }

}