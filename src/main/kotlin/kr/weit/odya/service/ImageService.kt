package kr.weit.odya.service

import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.contentimage.ContentImageRepository
import kr.weit.odya.domain.contentimage.getImageById
import kr.weit.odya.domain.contentimage.getImageByRectangle
import kr.weit.odya.domain.contentimage.getImageByUserId
import kr.weit.odya.domain.contentimage.getLifeShotByUserId
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.follow.getFollowingIds
import kr.weit.odya.service.dto.CoordinateImageRequest
import kr.weit.odya.service.dto.CoordinateImageResponse
import kr.weit.odya.service.dto.ImageResponse
import kr.weit.odya.service.dto.ImageUserType
import kr.weit.odya.service.dto.LifeShotRequest
import kr.weit.odya.service.dto.SliceResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ImageService(
    private val contentImageRepository: ContentImageRepository,
    private val fileService: FileService,
    private val followRepository: FollowRepository,
) {

    @Transactional(readOnly = true)
    fun getImages(userId: Long, size: Int, lastId: Long?): SliceResponse<ImageResponse> {
        val images = contentImageRepository.getImageByUserId(userId, size, lastId)
        return SliceResponse(
            size,
            images.map { ImageResponse.of(it, fileService.getPreAuthenticatedObjectUrl(it.name)) },
        )
    }

    @Transactional(readOnly = true)
    fun getLifeShots(userId: Long, size: Int, lastId: Long?): SliceResponse<ImageResponse> {
        val images = contentImageRepository.getLifeShotByUserId(userId, size, lastId)
        return SliceResponse(
            size,
            images.map { ImageResponse.of(it, fileService.getPreAuthenticatedObjectUrl(it.name)) },
        )
    }

    @Transactional
    fun setLifeShot(userId: Long, imageId: Long, lifeShotRequest: LifeShotRequest) {
        val image = contentImageRepository.getImageById(imageId)
        if (image.user.id != userId) {
            throw ForbiddenException("해당 이미지의 인생샷으로 등록할 권한이 없습니다.")
        }
        image.setLifeShotInfo(lifeShotRequest.placeName)
    }

    @Transactional
    fun cancelLifeShot(userId: Long, imageId: Long) {
        val image = contentImageRepository.getImageById(imageId)
        if (image.user.id != userId) {
            throw ForbiddenException("해당 인생샷을 취소할 권한이 없습니다.")
        }
        // 인생샷이 아닌 사진을 취소해도 에러를 발생시키지 않도록 한다
        image.unsetLifeShot()
    }

    @Transactional(readOnly = true)
    fun getImagesWithCoordinate(
        userId: Long,
        coordinateImageRequest: CoordinateImageRequest,
    ): List<CoordinateImageResponse> {
        val friendIdList = followRepository.getFollowingIds(userId)
        return contentImageRepository.getImageByRectangle(
            coordinateImageRequest.leftLongitude,
            coordinateImageRequest.bottomLatitude,
            coordinateImageRequest.rightLongitude,
            coordinateImageRequest.topLatitude,
            coordinateImageRequest.size,
        ).map {
            CoordinateImageResponse.of(
                it,
                getImageUserType(userId, it.user.id, friendIdList),
                fileService.getPreAuthenticatedObjectUrl(it.name),
            )
        }
    }

    private fun getImageUserType(userId: Long, imageOwnerId: Long, friendIdList: List<Long>): ImageUserType {
        return when {
            userId == imageOwnerId -> ImageUserType.USER
            friendIdList.contains(imageOwnerId) -> ImageUserType.FRIEND
            else -> ImageUserType.OTHER
        }
    }
}
