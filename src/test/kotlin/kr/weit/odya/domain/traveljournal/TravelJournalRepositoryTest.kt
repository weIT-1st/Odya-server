package kr.weit.odya.domain.traveljournal

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.contentimage.ContentImageRepository
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.createContentImage
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createTravelCompanionById
import kr.weit.odya.support.createTravelJournal
import kr.weit.odya.support.createTravelJournalContent
import kr.weit.odya.support.createTravelJournalContentImage
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class TravelJournalRepositoryTest(
    private val userRepository: UserRepository,
    private val contentImageRepository: ContentImageRepository,
    private val travelJournalRepository: TravelJournalRepository,
) : ExpectSpec(
    {
        lateinit var user: User
        beforeEach {
            user = userRepository.save(createUser())
            val otherUser: User = userRepository.save(createOtherUser())
            val contentImage = contentImageRepository.save(createContentImage(user = user))
            travelJournalRepository.save(
                createTravelJournal(
                    user = user,
                    travelCompanions = listOf(createTravelCompanionById(user = otherUser)),
                    travelJournalContents = listOf(
                        createTravelJournalContent(
                            travelJournalContentImages = listOf(
                                createTravelJournalContentImage(contentImage = contentImage),
                            ),
                        ),
                    ),
                ),
            )
        }

        context("여행 일지 조회") {
            expect("여행 일지 ID와 일치하는 여행 일지를 조회한다.") {
                val id = travelJournalRepository.findAll()[0].id
                val result = travelJournalRepository.getByTravelJournalId(id)
                result.id shouldBe id
            }
        }

        context("여행일지 사용자 ID로 조회") {
            expect("유저 ID와 일치하는 여행기록을 조회한다.") {
                val result = travelJournalRepository.getByUserId(user.id)
                result.size shouldBe 1
            }
        }

        context("여행 일지 삭제") {
            expect("여행 일지 ID와 일치하는 여행 일지를 삭제한다.") {
                val id = travelJournalRepository.findAll()[0].id
                travelJournalRepository.deleteById(id)
                travelJournalRepository.existsById(id) shouldBe false
            }

            expect("USER ID와 일치하는 여행 일지 모두 삭제한다.") {
                travelJournalRepository.deleteAllByUserId(user.id)
                travelJournalRepository.count() shouldBe 0
            }
        }
    },
)
