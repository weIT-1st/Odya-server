package kr.weit.odya.controller

import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.TermsService
import kr.weit.odya.service.dto.TermsUpdateRequest
import kr.weit.odya.service.dto.TermsUpdateResponse
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/change/terms")
class ChangeTermsController(private val termsService: TermsService) {
    @GetMapping
    fun getOptionalTermsTitleList(@LoginUserId userId: Long): ResponseEntity<TermsUpdateResponse> {
        return ResponseEntity.ok(termsService.getOptionalTermsListAndOptionalAgreedTerms(userId))
    }

    @PatchMapping
    fun updateTermsAgreement(
        @RequestBody
        termsUpdateRequest: TermsUpdateRequest,
        @LoginUserId
        userId: Long,
    ): ResponseEntity<Void> {
        termsService.updateAgreedTerms(termsUpdateRequest, userId)
        return ResponseEntity.noContent().build()
    }
}
