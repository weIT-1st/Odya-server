package kr.weit.odya.domain.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

fun UserRepository.existsByNickname(nickname: String): Boolean = existsByInformationNickname(nickname)

fun UserRepository.getByUsername(username: String): User =
    findByUsername(username) ?: throw NoSuchElementException("$username: 사용자가 존재하지 않습니다")

fun UserRepository.getUserRoleByUsername(username: String): String =
    findUserRoleByUsername(username) ?: throw NoSuchElementException("$username: 사용자가 존재하지 않습니다")

interface UserRepository : JpaRepository<User, Long> {
    fun existsByUsername(username: String): Boolean

    fun existsByInformationNickname(nickname: String): Boolean

    fun findByUsername(username: String): User?

    @Query("select u.userRole from User u where u.username = :username")
    fun findUserRoleByUsername(@Param("username") username: String): String?
}
