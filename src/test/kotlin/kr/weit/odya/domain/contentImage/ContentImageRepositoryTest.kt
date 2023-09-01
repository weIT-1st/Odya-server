package kr.weit.odya.domain.contentImage

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.contentimage.ContentImageRepository
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.createContentImage
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class ContentImageRepositoryTest(private val userRepository: UserRepository, private val contentImageRepository: ContentImageRepository) : ExpectSpec(
    {
        lateinit var contentImage1: ContentImage
        lateinit var contentImage2: ContentImage
        lateinit var user: User
        beforeTest {
            user = userRepository.save(createUser())
            contentImage1 = contentImageRepository.save(createContentImage(user = user))
            contentImage2 = contentImageRepository.save(createContentImage(user = user))
        }

        context("contentImage 삭제") {
            expect("user와 일치하는 contentImage 모두 삭제한다") {
                contentImageRepository.deleteAllByUserId(user.id)
                contentImageRepository.count() shouldBe 0
            }
        }
    },
)
