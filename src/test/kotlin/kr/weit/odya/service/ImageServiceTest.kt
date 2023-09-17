package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.contentimage.ContentImageRepository
import kr.weit.odya.domain.contentimage.getImageById
import kr.weit.odya.domain.contentimage.getImageByRectangle
import kr.weit.odya.domain.contentimage.getImageByUserId
import kr.weit.odya.domain.contentimage.getLifeShotByUserId
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.follow.getFollowingIds
import kr.weit.odya.support.TEST_BOTTOM_LATITUDE
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_FILE_AUTHENTICATED_URL
import kr.weit.odya.support.TEST_IMAGE_ID
import kr.weit.odya.support.TEST_LEFT_LONGITUDE
import kr.weit.odya.support.TEST_OTHER_USER_ID
import kr.weit.odya.support.TEST_RIGHT_LONGITUDE
import kr.weit.odya.support.TEST_SIZE
import kr.weit.odya.support.TEST_TOP_LATITUDE
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createContentImage
import kr.weit.odya.support.createCoordinateImageRequest
import kr.weit.odya.support.createLifeShotRequest
import kr.weit.odya.support.createPlaceDetails
import kr.weit.odya.support.createUser

class ImageServiceTest : DescribeSpec(
    {
        val contentImageRepository = mockk<ContentImageRepository>()
        val fileService = mockk<FileService>()
        val followRepository = mockk<FollowRepository>()
        val imageService = ImageService(contentImageRepository, fileService, followRepository)
        every { fileService.getPreAuthenticatedObjectUrl(any()) } returns TEST_FILE_AUTHENTICATED_URL

        describe("getImages") {
            context("유효한 유저 id와 size, lastId가 주어지는 경우") {
                every { contentImageRepository.getImageByUserId(TEST_USER_ID, TEST_SIZE, null) } returns listOf(
                    createContentImage(),
                )
                it("유저가 올린 사진이 반환된다.") {
                    imageService.getImages(TEST_USER_ID, TEST_SIZE, null).content.size shouldBe 1
                }
            }
        }

        describe("getLifeShots") {
            context("유효한 유저 id와 size, lastId가 주어지는 경우") {
                every { contentImageRepository.getLifeShotByUserId(TEST_USER_ID, TEST_SIZE, null) } returns listOf(
                    createContentImage(isLifeShot = true),
                )
                it("유저가 설정한 인생샷이 반환된다.") {
                    imageService.getLifeShots(TEST_USER_ID, TEST_SIZE, null).content.size shouldBe 1
                }
            }
        }

        describe("setLifeShot") {
            context("사진 올린 유저와 인생샷 설정 요청 유저가 다른 경우") {
                every { contentImageRepository.getImageById(any()) } returns createContentImage(user = createUser(id = TEST_OTHER_USER_ID))
                it("[ForbiddenException] 예외가 발생한다.") {
                    shouldThrow<ForbiddenException> {
                        imageService.setLifeShot(
                            TEST_USER_ID,
                            TEST_IMAGE_ID,
                            createLifeShotRequest(),
                        )
                    }
                }
            }

            context("유효한 유저 id와 사진 id, 인생샷 설정 요청이 주어지는 경우") {
                every { contentImageRepository.getImageById(any()) } returns createContentImage()
                it("정상적으로 종료한다.") {
                    shouldNotThrowAny {
                        imageService.setLifeShot(
                            TEST_USER_ID,
                            TEST_IMAGE_ID,
                            createLifeShotRequest(),
                        )
                    }
                }
            }

            context("유효한 유저 id와 사진 id, 장소id가 null로 주어지는 경우") {
                every { contentImageRepository.getImageById(any()) } returns createContentImage()
                it("정상적으로 종료한다.") {
                    shouldNotThrowAny {
                        imageService.setLifeShot(
                            TEST_USER_ID,
                            TEST_IMAGE_ID,
                            createLifeShotRequest(placeName = null),
                        )
                    }
                }
            }
        }

        describe("cancelLifeShot") {
            context("사진 올린 유저와 인생샷 설정 요청 유저가 다른 경우") {
                every { contentImageRepository.getImageById(any()) } returns createContentImage(user = createUser(id = TEST_OTHER_USER_ID))
                it("[ForbiddenException] 예외가 발생한다.") {
                    shouldThrow<ForbiddenException> {
                        imageService.cancelLifeShot(
                            TEST_USER_ID,
                            TEST_IMAGE_ID,
                        )
                    }
                }
            }

            context("유효한 유저 id와 사진 id가 주어지는 경우") {
                every { contentImageRepository.getImageById(any()) } returns createContentImage()
                it("정상적으로 종료한다.") {
                    shouldNotThrowAny {
                        imageService.cancelLifeShot(
                            TEST_USER_ID,
                            TEST_IMAGE_ID,
                        )
                    }
                }
            }
        }

        describe("getImagesWithCoordinate") {
            context("유효한 유저 id와 좌표가 주어지는 경우") {
                every { followRepository.getFollowingIds(TEST_USER_ID) } returns listOf(TEST_OTHER_USER_ID)
                every {
                    contentImageRepository.getImageByRectangle(
                        TEST_LEFT_LONGITUDE,
                        TEST_BOTTOM_LATITUDE,
                        TEST_RIGHT_LONGITUDE,
                        TEST_TOP_LATITUDE,
                        TEST_DEFAULT_SIZE,
                    )
                } returns listOf(createContentImage(placeDetails = createPlaceDetails()))
                every { fileService.getPreAuthenticatedObjectUrl(any()) } returns TEST_FILE_AUTHENTICATED_URL
                it("정상적으로 종료한다.") {
                    shouldNotThrowAny {
                        imageService.getImagesWithCoordinate(
                            TEST_USER_ID,
                            createCoordinateImageRequest(),
                        )
                    }
                }
            }
        }
    },
)
