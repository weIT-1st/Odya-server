package kr.weit.odya.controller

import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.TermsService
import kr.weit.odya.service.dto.ModifyAgreedTermsRequest
import kr.weit.odya.service.dto.TermsUpdateResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/terms")
class TermsController(private val termsService: TermsService) {
    @GetMapping
    fun getOptionalTermsAndUserAgreedTermsList(@LoginUserId userId: Long): ResponseEntity<TermsUpdateResponse> {
        return ResponseEntity.ok(termsService.getOptionalTermsListAndOptionalAgreedTerms(userId))
    }

    @PatchMapping
    fun modifyAgreedTerms(
        @RequestBody
        modifyAgreedTermsRequest: ModifyAgreedTermsRequest,
        @LoginUserId
        userId: Long,
    ): ResponseEntity<Void> {
        termsService.modifyAgreedTerms(modifyAgreedTermsRequest, userId)
        return ResponseEntity.noContent().build()
    }
}
