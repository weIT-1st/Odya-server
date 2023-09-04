package kr.weit.odya.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kr.weit.odya.domain.follow.FollowSortType
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.FollowService
import kr.weit.odya.service.dto.FollowCountsResponse
import kr.weit.odya.service.dto.FollowRequest
import kr.weit.odya.service.dto.FollowUserResponse
import kr.weit.odya.service.dto.SliceResponse
import kr.weit.odya.service.dto.VisitedFollowingResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/follows")
class FollowController(
    private val followService: FollowService,
) {
    @PostMapping
    fun createFollow(
        @LoginUserId userId: Long,
        @RequestBody @Valid
        followRequest: FollowRequest,
    ): ResponseEntity<Void> {
        followService.createFollow(userId, followRequest)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping
    fun deleteFollow(
        @LoginUserId userId: Long,
        @RequestBody @Valid
        followRequest: FollowRequest,
    ): ResponseEntity<Void> {
        followService.deleteFollow(userId, followRequest)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{userId}/counts")
    fun getFollowCounts(
        @Positive(message = "USER ID는 양수여야 합니다.")
        @PathVariable("userId")
        userId: Long,
    ): ResponseEntity<FollowCountsResponse> {
        return ResponseEntity.ok(followService.getFollowCounts(userId))
    }

    @GetMapping("/{userId}/followings")
    fun getFollowings(
        @Positive(message = "USER ID는 양수여야 합니다.")
        @PathVariable("userId")
        userId: Long,
        @PageableDefault(page = 0, size = 10) pageable: Pageable,
        @RequestParam(name = "sortType", required = false, defaultValue = "LATEST") sortType: FollowSortType,
    ): ResponseEntity<SliceResponse<FollowUserResponse>> {
        return ResponseEntity.ok(followService.getSliceFollowings(userId, pageable, sortType))
    }

    @GetMapping("/{userId}/followers")
    fun getFollowers(
        @Positive(message = "USER ID는 양수여야 합니다.")
        @PathVariable("userId")
        userId: Long,
        @PageableDefault(page = 0, size = 10) pageable: Pageable,
        @RequestParam(name = "sortType", required = false, defaultValue = "LATEST") sortType: FollowSortType,
    ): ResponseEntity<SliceResponse<FollowUserResponse>> {
        return ResponseEntity.ok(followService.getSliceFollowers(userId, pageable, sortType))
    }

    @GetMapping("/followings/search")
    fun search(
        @LoginUserId userId: Long,
        @NotNull(message = "검색할 닉네임은 필수입니다.")
        @RequestParam("nickname")
        nickname: String,
        @Positive(message = "사이즈는 양수여야 합니다.")
        @RequestParam(name = "size", required = false, defaultValue = "10")
        size: Int,
        @Positive(message = "마지막 Id는 양수여야 합니다.")
        @RequestParam(name = "lastId", required = false)
        lastId: Long?,
    ): ResponseEntity<SliceResponse<FollowUserResponse>> {
        return ResponseEntity.ok(followService.searchByNickname(userId, nickname, size, lastId))
    }

    @GetMapping("/may-know") // 페이스 북에서는 알수도 있는 친구를 you may know라고 표현하길래 따라해봤습니다
    fun getMayKnowFollowings(
        @LoginUserId userId: Long,
        @Positive(message = "사이즈는 양수여야 합니다.")
        @RequestParam(name = "size", required = false, defaultValue = "10")
        size: Int,
        @Positive(message = "마지막 Id는 양수여야 합니다.")
        @RequestParam(name = "lastId", required = false)
        lastId: Long?,
    ): ResponseEntity<SliceResponse<FollowUserResponse>> {
        return ResponseEntity.ok(followService.getMayKnowFollowings(userId, size, lastId))
    }

    @GetMapping("/{placeID}")
    fun getVisitedFollowings(
        @LoginUserId userId: Long,
        @NotBlank(message = "장소 ID는 필수 입력 값입니다.")
        @PathVariable("placeID")
        placeID: String,
    ): ResponseEntity<VisitedFollowingResponse> {
        return ResponseEntity.ok(followService.getVisitedFollowings(placeID, userId))
    }
}
