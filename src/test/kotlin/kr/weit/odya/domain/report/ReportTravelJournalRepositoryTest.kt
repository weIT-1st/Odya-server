package kr.weit.odya.domain.report

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.createContentImage
import kr.weit.odya.support.createCustomUser
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createReportTravelJournal
import kr.weit.odya.support.createTravelCompanionById
import kr.weit.odya.support.createTravelJournal
import kr.weit.odya.support.createTravelJournalContent
import kr.weit.odya.support.createTravelJournalContentImage
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class ReportTravelJournalRepositoryTest(
    private val reportTravelJournalRepository: ReportTravelJournalRepository,
    private val travelJournalRepository: TravelJournalRepository,
    private val userRepository: UserRepository,
) : ExpectSpec(
    {
        lateinit var user1: User
        lateinit var user2: User
        lateinit var user3: User
        lateinit var travelJournal: TravelJournal
        beforeEach {
            user1 = userRepository.save(createUser())
            user2 = userRepository.save(createOtherUser())
            user3 = userRepository.save(createCustomUser("test_user3", "test_user3"))
            travelJournal = travelJournalRepository.save(
                createTravelJournal(
                    user = user1,
                    travelCompanions = listOf(createTravelCompanionById(user = user2)),
                    travelJournalContents = listOf(
                        createTravelJournalContent(
                            travelJournalContentImages = listOf(
                                createTravelJournalContentImage(contentImage = createContentImage(user = user1)),
                            ),
                        ),
                    ),
                ),
            )
            reportTravelJournalRepository.save(createReportTravelJournal(travelJournal, user1))
            reportTravelJournalRepository.save(createReportTravelJournal(travelJournal, user2))
        }

        context("여행 일지 신고 수 조회") {
            expect("TRAVEL_JOURNAL_ID와 일치하는 여행 일지의 신고 수를 조회한다") {
                val result = reportTravelJournalRepository.countAllByTravelJournalId(travelJournal.id)
                result shouldBe 2
            }
        }

        context("여행 일지 신고 여부 확인(존재)") {
            expect("TRAVEL_JOURNAL_ID와 USER_ID가 일치하는 여행 일지의 신고 여부 확인(존재)") {
                val result = reportTravelJournalRepository.existsByJournalIdAndUserId(travelJournal.id, user1.id)
                result shouldBe true
            }

            expect("TRAVEL_JOURNAL_ID와 USER_ID가 일치하는 여행 일지의 신고 여부 확인(존재하지 않음)") {
                val result = reportTravelJournalRepository.existsByJournalIdAndUserId(travelJournal.id, user3.id)
                result shouldBe false
            }
        }

        context("여행 일지 신고 삭제") {
            expect("여행 일지 ID와 일치하는 여행 일지를 삭제한다.") {
                reportTravelJournalRepository.deleteAllByTravelJournalId(travelJournal.id)
                reportTravelJournalRepository.existsById(travelJournal.id) shouldBe false
            }

            expect("USER_ID와 일치하는 신고한 내역과 해당 유저의 여행일지 신고 내역 모두 삭제한다") {
                reportTravelJournalRepository.deleteAllByUserId(user1.id)
                reportTravelJournalRepository.count() shouldBe 0
            }
        }
    },
)
