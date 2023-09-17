package kr.weit.odya.domain.contentImage

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.contentimage.ContentImageRepository
import kr.weit.odya.domain.contentimage.getImageByRectangle
import kr.weit.odya.domain.contentimage.getImageByUserId
import kr.weit.odya.domain.contentimage.getLifeShotByUserId
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_BOTTOM_LATITUDE
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_LEFT_LONGITUDE
import kr.weit.odya.support.TEST_RIGHT_LONGITUDE
import kr.weit.odya.support.TEST_TOP_LATITUDE
import kr.weit.odya.support.createContentImage
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createPlaceDetails
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class ContentImageRepositoryTest(private val userRepository: UserRepository, private val contentImageRepository: ContentImageRepository) : ExpectSpec(
    {
        lateinit var contentImage1: ContentImage
        lateinit var contentImage2: ContentImage
        lateinit var user: User
        beforeEach {
            user = userRepository.save(createUser())
            contentImage1 = contentImageRepository.save(createContentImage(user = user, placeDetails = createPlaceDetails(lat = 10.0, lng = 10.0), isLifeShot = true))
            contentImage2 = contentImageRepository.save(createContentImage(user = user, placeDetails = createPlaceDetails(lat = 1.0, lng = 1.0), isLifeShot = true, placeName = null))
            contentImageRepository.save(
                createContentImage(user = user, placeDetails = createPlaceDetails(lat = -1.0, lng = -1.0), isLifeShot = true),
            )
            contentImageRepository.save(createContentImage(user = user))
            contentImageRepository.save(createContentImage(user = userRepository.save(createOtherUser()), isLifeShot = true))
        }

        context("contentImage 삭제") {
            expect("user와 일치하는 contentImage 모두 삭제한다") {
                contentImageRepository.deleteAllByUserId(user.id)
                contentImageRepository.count() shouldBe 1
            }
        }

        context("contentImage 전체 조회") {
            expect("userId와 일치하는 contentImage 모두 조회한다") {
                contentImageRepository.findAllByUserId(user.id).size shouldBe 4
            }
        }

        context("contentImage slice 조회") {
            expect("userId와 일치하는 contentImage를 조회한다") {
                contentImageRepository.getImageByUserId(user.id, TEST_DEFAULT_SIZE, null).size shouldBe 4
            }

            expect("userId와 일치하는 contentImage를 size만큼 조회한다") {
                contentImageRepository.getImageByUserId(user.id, 1, null).size shouldBe 2
            }

            expect("userId와 일치하는 contentImage를 lastId 이전으로 조회한다") {
                contentImageRepository.getImageByUserId(user.id, TEST_DEFAULT_SIZE, contentImage2.id).size shouldBe 1
            }
        }

        context("인생샷 slice 조회") {
            expect("userId와 일치하는 인생샷을 조회한다") {
                contentImageRepository.getLifeShotByUserId(user.id, TEST_DEFAULT_SIZE, null).size shouldBe 3
            }

            expect("userId와 일치하는 인생샷을 size만큼 조회한다") {
                contentImageRepository.getLifeShotByUserId(user.id, 1, null).size shouldBe 2
            }

            expect("userId와 일치하는 인생샷을 lastId 이전으로 조회한다") {
                contentImageRepository.getLifeShotByUserId(user.id, TEST_DEFAULT_SIZE, contentImage2.id).size shouldBe 1
            }
        }

        context("좌표로 contentImage 조회") {
            expect("좌표에 해당하는 contentImage를 조회한다") {
                val images = contentImageRepository.getImageByRectangle(
                    TEST_LEFT_LONGITUDE,
                    TEST_BOTTOM_LATITUDE,
                    TEST_RIGHT_LONGITUDE,
                    TEST_TOP_LATITUDE,
                    TEST_DEFAULT_SIZE,
                )
                images.size shouldBe 1
                images[0].id shouldBe contentImage2.id
            }
        }
    },
)
