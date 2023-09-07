package kr.weit.odya.domain.contentimage

import org.springframework.data.jpa.repository.JpaRepository

interface ContentImageRepository : JpaRepository<ContentImage, Long> {
    fun findAllByUserId(userId: Long): List<ContentImage>

    fun deleteAllByUserId(userId: Long)
}
