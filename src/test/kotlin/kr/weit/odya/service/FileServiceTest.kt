package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kr.weit.odya.service.generator.FileNameGenerator
import kr.weit.odya.support.DELETE_NOT_EXIST_PROFILE_ERROR_MESSAGE
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.TEST_FILE_AUTHENTICATED_URL
import kr.weit.odya.support.TEST_FILE_NAME
import kr.weit.odya.support.TEST_IMAGE_FILE_WEBP
import kr.weit.odya.support.TEST_INVALID_IMAGE_FILE_ORIGINAL_NAME
import kr.weit.odya.support.createMockProfile

class FileServiceTest : DescribeSpec(
    {
        val fileNameGenerator = mockk<FileNameGenerator>()
        val objectStorageService = mockk<ObjectStorageService>()
        val fileService = FileService(fileNameGenerator, objectStorageService)

        describe("saveFile") {
            context("유효한 파일이 주어지는 경우") {
                val mockFile = createMockProfile()
                every { fileNameGenerator.generate() } returns TEST_FILE_NAME
                every { objectStorageService.save(any(), TEST_IMAGE_FILE_WEBP) } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny { fileService.saveFile(mockFile) }
                }
            }

            context("올바르지 않은 형식의 ORIGINAL FILE NAME이 주어지는 경우") {
                val mockFile = createMockProfile(originalFileName = TEST_INVALID_IMAGE_FILE_ORIGINAL_NAME)
                every { fileNameGenerator.generate() } throws IllegalArgumentException("프로필 사진은 ${ALLOW_FILE_FORMAT_LIST.joinToString()} 형식만 가능합니다")
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> { fileService.saveFile(mockFile) }
                }
            }

            context("ORIGINAL FILE NAME이 주어지지 않는 경우") {
                val mockFile = createMockProfile(originalFileName = null)
                every { fileNameGenerator.generate() } throws IllegalArgumentException("원본 파일 이름이 존재하지 않습니다")
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> { fileService.saveFile(mockFile) }
                }
            }

            context("프로필 업로드에 실패하는 경우") {
                val mockFile = createMockProfile()
                every { fileNameGenerator.generate() } returns TEST_FILE_NAME
                every { objectStorageService.save(any(), TEST_IMAGE_FILE_WEBP) } throws ObjectStorageException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("[ObjectStorageException] 반환한다") {
                    shouldThrow<ObjectStorageException> { fileService.saveFile(mockFile) }
                }
            }
        }

        describe("getPreAuthenticatedObjectUrl") {
            context("유효한 파일 이름이 주어지는 경우") {
                every { objectStorageService.getPreAuthenticatedObjectUrl(TEST_IMAGE_FILE_WEBP) } returns TEST_FILE_AUTHENTICATED_URL
                it("정상적으로 종료한다") {
                    shouldNotThrowAny { fileService.getPreAuthenticatedObjectUrl(TEST_IMAGE_FILE_WEBP) }
                }
            }

            context("프로필 조회에 실패하는 경우") {
                every { objectStorageService.getPreAuthenticatedObjectUrl(TEST_IMAGE_FILE_WEBP) } throws ObjectStorageException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("[ObjectStorageException] 반환한다") {
                    shouldThrow<ObjectStorageException> {
                        fileService.getPreAuthenticatedObjectUrl(
                            TEST_IMAGE_FILE_WEBP,
                        )
                    }
                }
            }
        }

        describe("deleteFile") {
            context("유효한 파일 이름이 주어지는 경우") {
                every { objectStorageService.delete(TEST_IMAGE_FILE_WEBP) } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny { fileService.deleteFile(TEST_IMAGE_FILE_WEBP) }
                }
            }

            context("OBJECT STORAGE에 프로필이 없어 파일 삭제에 실패하는 경우") {
                every { objectStorageService.delete(TEST_IMAGE_FILE_WEBP) } throws IllegalArgumentException(
                    DELETE_NOT_EXIST_PROFILE_ERROR_MESSAGE,
                )
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> { fileService.deleteFile(TEST_IMAGE_FILE_WEBP) }
                }
            }

            context("프로필 삭제에 실패하는 경우") {
                every { objectStorageService.delete(TEST_IMAGE_FILE_WEBP) } throws ObjectStorageException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("[ObjectStorageException] 반환한다") {
                    shouldThrow<ObjectStorageException> { fileService.deleteFile(TEST_IMAGE_FILE_WEBP) }
                }
            }
        }
    },
)
