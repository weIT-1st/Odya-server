package kr.weit.odya.controller

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kr.weit.odya.service.TermsService
import kr.weit.odya.service.dto.TermsContentResponse
import kr.weit.odya.service.dto.TermsTitleListResponse
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/terms")
class TermsController(private val termsService: TermsService) {
    @GetMapping
    fun getTermsTitleList(): ResponseEntity<List<TermsTitleListResponse>> {
        return ResponseEntity.ok(termsService.getTermsList())
    }

    @GetMapping("/{id}")
    fun getTermsContent(
        @PathVariable("id")
        @NotNull(message = "약관 ID는 필수 입력값입니다.")
        @Positive(message = "약관 ID는 양수여야 합니다.")
        termsId: Long,
    ): ResponseEntity<TermsContentResponse> {
        return ResponseEntity.ok(termsService.getTermsContent(termsId))
    }
}
