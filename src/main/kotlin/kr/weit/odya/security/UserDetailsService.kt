package kr.weit.odya.security

import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getUserRoleByUsername
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component

@Component
class UserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val userRole = userRepository.getUserRoleByUsername(username)

        return User(username, "", listOf(SimpleGrantedAuthority(userRole)))
    }
}
