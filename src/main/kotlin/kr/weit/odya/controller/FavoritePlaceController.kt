package kr.weit.odya.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.FavoritePlaceService
import kr.weit.odya.service.dto.FavoritePlaceRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/favorite-places")
class FavoritePlaceController(private val favoritePlaceService: FavoritePlaceService) {
    @PostMapping
    fun createFavoritePlace(
        @LoginUserId
        userId: Long,
        @Valid
        @RequestBody
        favoritePlace: FavoritePlaceRequest,
    ): ResponseEntity<Void> {
        favoritePlaceService.createFavoritePlace(userId, favoritePlace)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("/{id}")
    fun deleteFavoritePlace(
        @LoginUserId
        userId: Long,
        @NotNull(message = "관심 장소 ID는 필수 입력값입니다.")
        @Positive(message = "관심 장소 ID는 양수여야 합니다.")
        @PathVariable("id")
        favoritePlaceId: Long,
    ): ResponseEntity<Void> {
        favoritePlaceService.deleteFavoritePlace(userId, favoritePlaceId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @GetMapping("/{placeId}")
    fun getFavoritePlace(
        @LoginUserId
        userId: Long,
        @NotNull(message = "관심 장소 ID는 필수 입력값입니다.")
        @PathVariable("placeId")
        placeId: String,
    ): ResponseEntity<Boolean> {
        return ResponseEntity.ok(favoritePlaceService.getFavoritePlace(userId, placeId))
    }
}
