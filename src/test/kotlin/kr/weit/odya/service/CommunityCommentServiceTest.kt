package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.community.getByCommunityId
import kr.weit.odya.domain.communitycomment.CommunityComment
import kr.weit.odya.domain.communitycomment.CommunityCommentRepository
import kr.weit.odya.domain.communitycomment.getCommunityCommentBy
import kr.weit.odya.domain.communitycomment.getSliceCommunityCommentBy
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.support.TEST_COMMUNITY_COMMENT_ID
import kr.weit.odya.support.TEST_COMMUNITY_ID
import kr.weit.odya.support.TEST_DEFAULT_PROFILE_PNG
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_FILE_AUTHENTICATED_URL
import kr.weit.odya.support.TEST_OTHER_COMMUNITY_COMMENT_ID
import kr.weit.odya.support.TEST_OTHER_USER_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createCommunity
import kr.weit.odya.support.createCommunityComment
import kr.weit.odya.support.createCommunityCommentRequest
import kr.weit.odya.support.createMockCommunityComment
import kr.weit.odya.support.createUser
import org.springframework.context.ApplicationEventPublisher

class CommunityCommentServiceTest : DescribeSpec(
    {
        val communityRepository: CommunityRepository = mockk<CommunityRepository>()
        val communityCommentRepository: CommunityCommentRepository = mockk<CommunityCommentRepository>()
        val userRepository: UserRepository = mockk<UserRepository>()
        val fileService: FileService = mockk<FileService>()
        val followRepository: FollowRepository = mockk<FollowRepository>()
        val eventPublisher: ApplicationEventPublisher = mockk<ApplicationEventPublisher>()
        val communityCommentService = CommunityCommentService(
            communityRepository,
            communityCommentRepository,
            userRepository,
            fileService,
            followRepository,
            eventPublisher,
        )

        describe("createCommunityComment") {
            context("유효한 커뮤니티 ID와 사용자 ID가 주어지는 경우") {
                every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } returns createCommunity()
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                every { communityCommentRepository.save(any<CommunityComment>()) } returns createCommunityComment()
                it("생성된 커뮤니티 ID를 반환한다.") {
                    communityCommentService.createCommunityComment(
                        TEST_USER_ID,
                        TEST_COMMUNITY_ID,
                        createCommunityCommentRequest(),
                    ) shouldBe TEST_COMMUNITY_COMMENT_ID
                }
            }

            context("존재하지 않은 커뮤니티 ID가 주어지는 경우") {
                every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> {
                        communityCommentService.createCommunityComment(
                            TEST_USER_ID,
                            TEST_COMMUNITY_ID,
                            createCommunityCommentRequest(),
                        )
                    }
                }
            }

            context("존재하지 않은 사용자 ID가 주어지는 경우") {
                every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } returns createCommunity()
                every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> {
                        communityCommentService.createCommunityComment(
                            TEST_USER_ID,
                            TEST_COMMUNITY_ID,
                            createCommunityCommentRequest(),
                        )
                    }
                }
            }
        }

        describe("getCommunityComments") {
            context("lastId 없이 커뮤니티 ID와 사이즈가 주어지는 경우") {
                every {
                    communityCommentRepository.getSliceCommunityCommentBy(
                        TEST_COMMUNITY_ID,
                        TEST_DEFAULT_SIZE,
                        null,
                    )
                } returns listOf(
                    createMockCommunityComment(),
                    createMockCommunityComment(id = TEST_OTHER_COMMUNITY_COMMENT_ID),
                )
                every { fileService.getPreAuthenticatedObjectUrl(TEST_DEFAULT_PROFILE_PNG) } returns TEST_FILE_AUTHENTICATED_URL
                every { followRepository.findFollowingIdsByFollowerId(TEST_USER_ID) } returns listOf(TEST_OTHER_USER_ID)
                it("커뮤니티 댓글 목록을 반환한다.") {
                    communityCommentService.getCommunityComments(
                        TEST_USER_ID,
                        TEST_COMMUNITY_ID,
                        TEST_DEFAULT_SIZE,
                        null,
                    ).content.size shouldBe 2
                }
            }

            context("lastId가 주어지는 경우") {
                every {
                    communityCommentRepository.getSliceCommunityCommentBy(
                        TEST_COMMUNITY_ID,
                        TEST_DEFAULT_SIZE,
                        TEST_COMMUNITY_COMMENT_ID,
                    )
                } returns listOf(
                    createMockCommunityComment(id = TEST_OTHER_COMMUNITY_COMMENT_ID),
                )
                every { fileService.getPreAuthenticatedObjectUrl(TEST_DEFAULT_PROFILE_PNG) } returns TEST_FILE_AUTHENTICATED_URL
                every { followRepository.findFollowingIdsByFollowerId(TEST_USER_ID) } returns listOf(TEST_OTHER_USER_ID)
                it("커뮤니티 댓글 목록을 반환한다.") {
                    communityCommentService.getCommunityComments(
                        TEST_USER_ID,
                        TEST_COMMUNITY_ID,
                        TEST_DEFAULT_SIZE,
                        TEST_COMMUNITY_COMMENT_ID,
                    ).content.size shouldBe 1
                }
            }
        }

        describe("updateCommunityComment") {
            context("유효한 커뮤니티 댓글 ID, 커뮤니티 ID와 사용자 ID가 주어지는 경우") {
                every {
                    communityCommentRepository.getCommunityCommentBy(
                        TEST_COMMUNITY_COMMENT_ID,
                        TEST_COMMUNITY_ID,
                    )
                } returns createCommunityComment()
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        communityCommentService.updateCommunityComment(
                            TEST_USER_ID,
                            TEST_COMMUNITY_ID,
                            TEST_COMMUNITY_COMMENT_ID,
                            createCommunityCommentRequest(),
                        )
                    }
                }
            }

            context("유효하지 않은 커뮤니티 댓글 ID와 커뮤니티 ID가 주어지는 경우") {
                every {
                    communityCommentRepository.getCommunityCommentBy(
                        TEST_COMMUNITY_COMMENT_ID,
                        TEST_COMMUNITY_ID,
                    )
                } throws NoSuchElementException()
                it("[NoSuchElementException] 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> {
                        communityCommentService.updateCommunityComment(
                            TEST_USER_ID,
                            TEST_COMMUNITY_ID,
                            TEST_COMMUNITY_COMMENT_ID,
                            createCommunityCommentRequest(),
                        )
                    }
                }
            }

            context("댓글 작성자가 수정 요청을 하지 않는 경우") {
                every {
                    communityCommentRepository.getCommunityCommentBy(
                        TEST_COMMUNITY_COMMENT_ID,
                        TEST_COMMUNITY_ID,
                    )
                } returns createCommunityComment()
                it("[ForbiddenException] 예외가 발생한다") {
                    shouldThrow<ForbiddenException> {
                        communityCommentService.updateCommunityComment(
                            TEST_OTHER_USER_ID,
                            TEST_COMMUNITY_ID,
                            TEST_COMMUNITY_COMMENT_ID,
                            createCommunityCommentRequest(),
                        )
                    }
                }
            }
        }

        describe("deleteCommunityComment") {
            context("유효한 커뮤니티 댓글 ID, 커뮤니티 ID와 사용자 ID가 주어지는 경우") {
                every {
                    communityCommentRepository.getCommunityCommentBy(
                        TEST_COMMUNITY_COMMENT_ID,
                        TEST_COMMUNITY_ID,
                    )
                } returns createCommunityComment()
                every { communityCommentRepository.delete(any<CommunityComment>()) } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        communityCommentService.deleteCommunityComment(
                            TEST_USER_ID,
                            TEST_COMMUNITY_ID,
                            TEST_COMMUNITY_COMMENT_ID,
                        )
                    }
                }
            }

            context("유효하지 않은 커뮤니티 댓글 ID와 커뮤니티 ID가 주어지는 경우") {
                every {
                    communityCommentRepository.getCommunityCommentBy(
                        TEST_COMMUNITY_COMMENT_ID,
                        TEST_COMMUNITY_ID,
                    )
                } throws NoSuchElementException()
                it("[NoSuchElementException] 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> {
                        communityCommentService.deleteCommunityComment(
                            TEST_USER_ID,
                            TEST_COMMUNITY_ID,
                            TEST_COMMUNITY_COMMENT_ID,
                        )
                    }
                }
            }

            context("댓글 작성자가 수정 요청을 하지 않는 경우") {
                every {
                    communityCommentRepository.getCommunityCommentBy(
                        TEST_COMMUNITY_COMMENT_ID,
                        TEST_COMMUNITY_ID,
                    )
                } returns createCommunityComment()
                it("[ForbiddenException] 예외가 발생한다") {
                    shouldThrow<ForbiddenException> {
                        communityCommentService.deleteCommunityComment(
                            TEST_OTHER_USER_ID,
                            TEST_COMMUNITY_ID,
                            TEST_COMMUNITY_COMMENT_ID,
                        )
                    }
                }
            }
        }
    },
)
