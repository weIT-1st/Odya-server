package kr.weit.odya.domain.follow

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.placeReview.PlaceReviewRepository
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_DEFAULT_PAGEABLE
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_DEFAULT_SORT_TYPE
import kr.weit.odya.support.TEST_FCM_TOKEN
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.createCommunity
import kr.weit.odya.support.createCommunityContentImage
import kr.weit.odya.support.createContentImage
import kr.weit.odya.support.createCustomUser
import kr.weit.odya.support.createFollow
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createPlaceReview
import kr.weit.odya.support.createTravelCompanionById
import kr.weit.odya.support.createTravelJournal
import kr.weit.odya.support.createTravelJournalContent
import kr.weit.odya.support.createTravelJournalContentImage
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class FollowRepositoryTest(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
    private val placeReviewRepository: PlaceReviewRepository,
    private val travelJournalRepository: TravelJournalRepository,
    private val communityRepository: CommunityRepository,
) : ExpectSpec(
    {
        lateinit var follower: User
        lateinit var following: User
        lateinit var following2: User
        lateinit var notFollowing: User
        beforeEach {
            follower = userRepository.save(createUser().apply { changeFcmToken(TEST_FCM_TOKEN) })
            following = userRepository.save(createOtherUser())
            following2 = userRepository.save(createCustomUser("test_user_3", "test_user_3"))
            notFollowing = userRepository.save(createCustomUser("test_user_4", "test_user_4"))
            followRepository.save(createFollow(follower, following))
            followRepository.save(createFollow(following, follower))
            followRepository.save(createFollow(follower, following2))
            followRepository.save(createFollow(following, notFollowing))
            followRepository.save(createFollow(notFollowing, following))
            followRepository.save(createFollow(following2, notFollowing))
        }

        context("팔로우 목록 조회") {
            expect("FOLLOWER가 팔로잉하는 사용자 목록을 조회한다") {
                val result = followRepository.getFollowingListBySearchCond(
                    follower.id,
                    TEST_DEFAULT_PAGEABLE,
                    TEST_DEFAULT_SORT_TYPE,
                )
                result.size shouldBe 2
            }

            expect("FOLLOWING을 팔로워하는 사용자 목록을 조회한다") {
                val result = followRepository.getFollowerListBySearchCond(
                    following.id,
                    TEST_DEFAULT_PAGEABLE,
                    TEST_DEFAULT_SORT_TYPE,
                )
                result.size shouldBe 2
            }
        }

        context("팔로우 여부 확인") {
            expect("FOLLOWER ID와 FOLLOWING ID가 일치하는 팔로우 여부를 확인한다") {
                val result = followRepository.existsByFollowerIdAndFollowingId(follower.id, following.id)
                result shouldBe true
            }
        }

        context("팔로우 수 확인") {
            expect("FOLLOWER가 팔로잉하는 사용자의 수를 조회한다") {
                val result = followRepository.countByFollowerId(follower.id)
                result shouldBe 2
            }

            expect("FOLLOWING을 팔로워하는 사용자의 수를 조회한다") {
                val result = followRepository.countByFollowingId(following.id)
                result shouldBe 2
            }
        }

        context("팔로우 삭제") {
            expect("FOLLOWER ID와 FOLLOWING ID가 일치하는 팔로우를 삭제한다") {
                followRepository.deleteByFollowerIdAndFollowingId(follower.id, following.id)
                followRepository.existsByFollowerIdAndFollowingId(follower.id, following.id) shouldBe false
            }
        }

        context("FOLLOWER가 팔로잉하는 사용자 목록을 전체 삭제한다") {
            expect("userId와 일치하는 팔로잉 목록을 전체 삭제한다") {
                followRepository.deleteByFollowerId(follower.id)
                followRepository.countByFollowerId(follower.id) shouldBe 0
            }
        }

        context("FOLLOWING이 팔로워하는 사용자 목록을 전체 삭제한다") {
            expect("userId와 일치하는 팔로우 목록을 전체 삭제한다") {
                followRepository.deleteByFollowingId(following.id)
                followRepository.countByFollowingId(following.id) shouldBe 0
            }
        }

        context("팔로잉 유저 id list 검색") {
            expect("팔로잉 id list에 해당하는 팔로잉 유저를 조회한다") {
                val userIds = listOf(following.id, following.id + 1)
                val result = followRepository.getByFollowerIdAndFollowingIdIn(
                    follower.id,
                    userIds,
                    TEST_DEFAULT_SIZE,
                    null,
                )
                result.size shouldBe 2
            }

            expect("팔로잉 id list에 해당하는 팔로잉 유저를 size만큼 조회한다") {
                val userIds = listOf(following.id, following.id + 1)
                val result =
                    followRepository.getByFollowerIdAndFollowingIdIn(follower.id, userIds, 1, null)
                result.size shouldBe 1
            }

            expect("팔로잉 id list에 해당하는 lastId보다 큰 팔로잉 유저를 조회한다") {
                val userIds = listOf(following.id - 1, following.id)
                var result = followRepository.getByFollowerIdAndFollowingIdIn(
                    follower.id,
                    userIds,
                    TEST_DEFAULT_SIZE,
                    following.id - 1,
                )
                result.size shouldBe 1
                result = followRepository.getByFollowerIdAndFollowingIdIn(
                    follower.id,
                    userIds,
                    TEST_DEFAULT_SIZE,
                    following.id,
                )
                result.size shouldBe 0
            }
        }

        context("내가 팔로우한 유저 id list 검색") {
            expect("팔로워 id list에 해당하는 팔로워 유저를 조회한다.") {
                val userIds = listOf(follower.id, notFollowing.id)
                val result = followRepository.getByFollowingIdAndFollowerIdIn(
                    following.id,
                    userIds,
                    TEST_DEFAULT_SIZE,
                    null,
                )
                result.size shouldBe 2
            }

            expect("팔로워 id list에 해당하는 팔로워 유저를 size만큼 조회한다") {
                val userIds = listOf(follower.id, notFollowing.id)
                val result =
                    followRepository.getByFollowingIdAndFollowerIdIn(
                        following.id,
                        userIds,
                        1,
                        null,
                    )
                result.size shouldBe 1
            }

            expect("팔로워 id list에 해당하는 lastId보다 큰 팔로워 유저를 조회한다") {
                val userIds = listOf(follower.id, notFollowing.id)
                var result = followRepository.getByFollowingIdAndFollowerIdIn(
                    following.id,
                    userIds,
                    TEST_DEFAULT_SIZE,
                    follower.id,
                )
                result.size shouldBe 1
                result = followRepository.getByFollowerIdAndFollowingIdIn(
                    following.id,
                    userIds,
                    TEST_DEFAULT_SIZE,
                    notFollowing.id,
                )
                result.size shouldBe 0
            }
        }

        context("알수도 있는 유저 검색") {
            expect("알수도 있는 유저를 조회한다") {
                val result = followRepository.getMayKnowFollowings(
                    follower.id,
                    TEST_DEFAULT_SIZE,
                    null,
                )
                result.size shouldBe 1
            }

            expect("lastId보다 작은 알수도 있는 유저를 조회한다") {
                var result = followRepository.getMayKnowFollowings(
                    follower.id,
                    TEST_DEFAULT_SIZE,
                    notFollowing.id + 1,
                )
                result.size shouldBe 1

                result = followRepository.getMayKnowFollowings(
                    follower.id,
                    TEST_DEFAULT_SIZE,
                    notFollowing.id,
                )
                result.size shouldBe 0
            }
        }

        context("팔로워들의 FCM 토큰 검색") {
            expect("팔로워들의 FCM 토큰을 조회한다") {
                val result = followRepository.getFollowerFcmTokens(following.id)
                result shouldBe listOf(TEST_FCM_TOKEN)
            }
        }

        context("방문한 친구 id 검색") {
            expect("같은 장소에 리뷰를 작성한 친구를 조회한다") {
                placeReviewRepository.save(
                    createPlaceReview(
                        following,
                    ),
                )

                val result = followRepository.getVisitedFollowingIds(
                    TEST_PLACE_ID,
                    follower.id,
                )
                result shouldBe listOf(following.id)
            }

            expect("같은 장소에 여행일지를 작성한 친구를 조회한다") {
                travelJournalRepository.save(
                    createTravelJournal(
                        user = following,
                        travelCompanions = listOf(createTravelCompanionById(user = notFollowing)),
                        travelJournalContents = listOf(
                            createTravelJournalContent(
                                travelJournalContentImages = listOf(
                                    createTravelJournalContentImage(contentImage = createContentImage(user = following)),
                                ),
                            ),
                        ),
                    ),
                )

                val result = followRepository.getVisitedFollowingIds(
                    TEST_PLACE_ID,
                    follower.id,
                )
                result shouldBe listOf(following.id)
            }

            expect("같은 장소에 커뮤니티를 작성한 친구를 조회한다") {
                communityRepository.save(
                    createCommunity(
                        travelJournal = null,
                        user = following,
                        communityContentImages = listOf(
                            createCommunityContentImage(contentImage = createContentImage(user = following)),
                        ),
                    ),
                )

                val result = followRepository.getVisitedFollowingIds(
                    TEST_PLACE_ID,
                    follower.id,
                )
                result shouldBe listOf(following.id)
            }

            expect("해당 장소에 들린 유저가 없다면 아무것도 반환되지 않는다") {
                val result = followRepository.getVisitedFollowingIds(
                    TEST_PLACE_ID,
                    follower.id,
                )
                result shouldBe emptyList()
            }
        }

        context("getFollowingIds") {
            expect("팔로잉 id list를 조회한다") {
                val result = followRepository.getFollowingIds(follower.id)
                result shouldBe listOf(following.id, following2.id)
            }
        }
    },
)
