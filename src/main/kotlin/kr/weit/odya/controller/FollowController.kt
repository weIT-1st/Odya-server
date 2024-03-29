package kr.weit.odya.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
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

    @DeleteMapping("follower/{followerId}")
    fun deleteFollower(
        @LoginUserId
        userId: Long,
        @PathVariable("followerId")
        @Positive(message = "팔로워의 USER ID는 양수여야 합니다.")
        followerId: Long,
    ): ResponseEntity<Void> {
        followService.deleteFollower(userId, followerId)
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
        @LoginUserId loginUserId: Long,
        @Positive(message = "USER ID는 양수여야 합니다.")
        @PathVariable("userId")
        userId: Long,
        @PageableDefault(page = 0, size = 10) pageable: Pageable,
        @RequestParam(name = "sortType", required = false, defaultValue = "LATEST") sortType: FollowSortType,
    ): ResponseEntity<SliceResponse<FollowUserResponse>> {
        return ResponseEntity.ok(followService.getSliceFollowings(loginUserId, userId, pageable, sortType))
    }

    @GetMapping("/{userId}/followers")
    fun getFollowers(
        @LoginUserId loginUserId: Long,
        @Positive(message = "USER ID는 양수여야 합니다.")
        @PathVariable("userId")
        userId: Long,
        @PageableDefault(page = 0, size = 10) pageable: Pageable,
        @RequestParam(name = "sortType", required = false, defaultValue = "LATEST") sortType: FollowSortType,
    ): ResponseEntity<SliceResponse<FollowUserResponse>> {
        return ResponseEntity.ok(followService.getSliceFollowers(loginUserId, userId, pageable, sortType))
    }

    @GetMapping("/followings/search")
    fun followingNicknameSearch(
        @LoginUserId userId: Long,
        @NotBlank(message = "검색할 닉네임은 필수입니다.")
        @RequestParam("nickname")
        nickname: String,
        @Positive(message = "사이즈는 양수여야 합니다.")
        @RequestParam(name = "size", required = false, defaultValue = "10")
        size: Int,
        @Positive(message = "마지막 Id는 양수여야 합니다.")
        @RequestParam(name = "lastId", required = false)
        lastId: Long?,
    ): ResponseEntity<SliceResponse<FollowUserResponse>> {
        return ResponseEntity.ok(followService.searchByFollowingNickname(userId, userId, nickname, size, lastId))
    }

    @GetMapping("/followers/search")
    fun followerNickNameSearch(
        @LoginUserId userId: Long,
        @NotBlank(message = "검색할 닉네임은 필수입니다.")
        @RequestParam("nickname")
        nickname: String,
        @Positive(message = "사이즈는 양수여야 합니다.")
        @RequestParam(name = "size", required = false, defaultValue = "10")
        size: Int,
        @Positive(message = "마지막 Id는 양수여야 합니다.")
        @RequestParam(name = "lastId", required = false)
        lastId: Long?,
    ): ResponseEntity<SliceResponse<FollowUserResponse>> {
        return ResponseEntity.ok(followService.searchByFollowerNickname(userId, userId, nickname, size, lastId))
    }

    @GetMapping("/{userId}/followings/search")
    fun otherFollowingNicknameSearch(
        @LoginUserId loginUserId: Long,
        @Positive(message = "USER ID는 양수여야 합니다.")
        @PathVariable("userId")
        userId: Long,
        @NotBlank(message = "검색할 닉네임은 필수입니다.")
        @RequestParam("nickname")
        nickname: String,
        @Positive(message = "사이즈는 양수여야 합니다.")
        @RequestParam(name = "size", required = false, defaultValue = "10")
        size: Int,
        @Positive(message = "마지막 Id는 양수여야 합니다.")
        @RequestParam(name = "lastId", required = false)
        lastId: Long?,
    ): ResponseEntity<SliceResponse<FollowUserResponse>> {
        return ResponseEntity.ok(followService.searchByFollowingNickname(loginUserId, userId, nickname, size, lastId))
    }

    @GetMapping("/{userId}/followers/search")
    fun otherFollowerNickNameSearch(
        @LoginUserId loginUserId: Long,
        @Positive(message = "USER ID는 양수여야 합니다.")
        @PathVariable("userId")
        userId: Long,
        @NotBlank(message = "검색할 닉네임은 필수입니다.")
        @RequestParam("nickname")
        nickname: String,
        @Positive(message = "사이즈는 양수여야 합니다.")
        @RequestParam(name = "size", required = false, defaultValue = "10")
        size: Int,
        @Positive(message = "마지막 Id는 양수여야 합니다.")
        @RequestParam(name = "lastId", required = false)
        lastId: Long?,
    ): ResponseEntity<SliceResponse<FollowUserResponse>> {
        return ResponseEntity.ok(followService.searchByFollowerNickname(loginUserId, userId, nickname, size, lastId))
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
