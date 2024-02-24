package kr.weit.odya.support.config

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kr.weit.odya.config.handler.CustomAuthenticationEntryPoint
import kr.weit.odya.security.filter.FIREBASE_TOKEN_FILTER_PERMITTED_PATTERNS
import kr.weit.odya.support.filter.TestTokenFilter
import kr.weit.odya.support.log.TraceManager
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@TestConfiguration
class TestSecurityConfig {

    @Bean
    fun traceManager():TraceManager = mockk<TraceManager>().apply {
        every {
            doErrorLog(any())
        } just Runs
    }

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
            TestTokenFilter(),
            UsernamePasswordAuthenticationFilter::class.java,
        )
        .exceptionHandling {
            it.authenticationEntryPoint(CustomAuthenticationEntryPoint())
        }
        .build()
}
