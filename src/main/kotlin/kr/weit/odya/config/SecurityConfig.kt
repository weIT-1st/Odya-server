package kr.weit.odya.config

import kr.weit.odya.config.handler.CustomAuthenticationEntryPoint
import kr.weit.odya.security.FirebaseTokenHelper
import kr.weit.odya.security.UserDetailsService
import kr.weit.odya.security.filter.FIREBASE_TOKEN_FILTER_PERMITTED_PATTERNS
import kr.weit.odya.security.filter.FirebaseTokenFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val userDetailsService: UserDetailsService,
    private val firebaseTokenHelper: FirebaseTokenHelper,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
) {
    @Bean
    fun springSecurity(http: HttpSecurity): SecurityFilterChain = http
        .csrf { it.disable() }
        .formLogin { it.disable() }
        .httpBasic { it.disable() }
        .logout { it.disable() }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests {
            it
                .requestMatchers(*FIREBASE_TOKEN_FILTER_PERMITTED_PATTERNS.toTypedArray()).permitAll()
                .anyRequest().authenticated()
        }
        .addFilterBefore(
            FirebaseTokenFilter(userDetailsService, firebaseTokenHelper),
            UsernamePasswordAuthenticationFilter::class.java,
        )
        .exceptionHandling {
            it.authenticationEntryPoint(customAuthenticationEntryPoint)
        }
        .build()
}
