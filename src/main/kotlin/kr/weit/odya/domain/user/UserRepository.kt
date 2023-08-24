package kr.weit.odya.domain.user

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.repository.query.Param

fun UserRepository.existsByNickname(nickname: String): Boolean = existsByInformationNickname(nickname)

fun UserRepository.existsByEmail(email: String): Boolean = existsByInformationEmail(email)

fun UserRepository.existsByPhoneNumber(phoneNumber: String): Boolean = existsByInformationPhoneNumber(phoneNumber)

fun UserRepository.getByUsername(username: String): User =
    findByUsername(username) ?: throw NoSuchElementException("$username: 사용자가 존재하지 않습니다")

fun UserRepository.getByUserId(userId: Long): User =
    findByIdOrNull(userId) ?: throw NoSuchElementException("$userId: 사용자가 존재하지 않습니다")

fun UserRepository.getByUserIdWithProfile(userId: Long): User =
    findUserWithProfileById(userId) ?: throw NoSuchElementException("$userId: 사용자가 존재하지 않습니다")

fun UserRepository.getByUserIds(userIds: Collection<Long>): List<User> = findByIdIn(userIds)

interface UserRepository : JpaRepository<User, Long> {
    fun existsByUsername(username: String): Boolean

    fun existsByInformationNickname(nickname: String): Boolean

    fun findByUsername(username: String): User?

    fun existsByInformationPhoneNumber(phoneNumber: String): Boolean

    fun existsByInformationEmail(email: String): Boolean

    fun findByIdIn(userIds: Collection<Long>): List<User>

    @Query("select case when count(u) = :countUserIds then true else false end from User u where u.id in (:userIds)")
    fun existsAllByUserIds(
        @Param("userIds") userIds: Collection<Long>,
        @Param("countUserIds") countUserIds: Int,
    ): Boolean

    @EntityGraph(attributePaths = ["profile", "profile.profileColor"])
    fun findUserWithProfileById(userId: Long): User?
}
