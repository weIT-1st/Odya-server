package kr.weit.odya.domain.traveljournal

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
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
    private val travelJournalRepository: TravelJournalRepository,
) : ExpectSpec(
    {
        lateinit var user: User
        lateinit var otherUser: User
        lateinit var travelJournal: TravelJournal
        beforeEach {
            user = userRepository.save(createUser())
            otherUser = userRepository.save(createOtherUser())
            travelJournal = travelJournalRepository.save(
                createTravelJournal(
                    user = user,
                    travelCompanions = listOf(createTravelCompanionById(user = otherUser)),
                    travelJournalContents = listOf(
                        createTravelJournalContent(
                            travelJournalContentImages = listOf(
                                createTravelJournalContentImage(contentImage = createContentImage(user = user)),
                                createTravelJournalContentImage(contentImage = createContentImage(name = "test1.webp", originName = "test1.webp", user = user)),
                            ),
                        ),
                    ),
                ),
            )
        }

        context("여행 일지 조회") {
            expect("여행 일지 ID와 일치하는 여행 일지를 조회한다.") {
                val result = travelJournalRepository.getByTravelJournalId(travelJournal.id)
                result.id shouldBe travelJournal.id
            }
        }

        context("여행일지 사용자 Id로 조회") {
            expect("유저 ID와 일치하는 여행기록을 조회한다.") {
                val result = travelJournalRepository.getByUserId(user.id)
                result.size shouldBe 1
            }
        }

        context("ContentImage name 조회") {
            expect("여행 일지 ID와 일치하는 ContentImage name를 조회한다.") {
                val result = travelJournalRepository.getByContentImageNames(travelJournal.id)
                result shouldBe listOf("generated_file.webp", "test1.webp")
            }
        }

        context("여행 일지 삭제") {
            expect("여행 일지 ID와 일치하는 여행 일지를 삭제한다.") {
                travelJournalRepository.deleteById(travelJournal.id)
                travelJournalRepository.existsById(travelJournal.id) shouldBe false
            }

            expect("USER ID와 일치하는 여행 일지 모두 삭제한다.") {
                travelJournalRepository.deleteAllByUserId(user.id)
                travelJournalRepository.count() shouldBe 0
            }
        }
    },
)
