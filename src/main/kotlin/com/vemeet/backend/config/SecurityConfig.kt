package com.vemeet.backend.config

import com.vemeet.backend.security.CustomAccessDeniedHandler
import com.vemeet.backend.security.CustomAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val customAccessDeniedHandler: CustomAccessDeniedHandler,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val corsConfigurationSource: CorsConfigurationSource
    ) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource)}
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(
                        "/v1/auth/login",
                        "/v1/auth/confirm",
                        "/v1/auth/register",
                        "/v1/auth/refresh",
                        "/v1/auth/password-reset/**",
                        "/v1/auth/verification-email/resend"
                    ).permitAll()
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/hello/**").permitAll()
                    .requestMatchers("/chat/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
            .exceptionHandling {
                it.accessDeniedHandler(customAccessDeniedHandler)
                it.authenticationEntryPoint(customAuthenticationEntryPoint)
            }
        return http.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("cognito:groups")
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_")

        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter { jwt: Jwt ->
            val grantedAuthorities = jwtGrantedAuthoritiesConverter.convert(jwt)
            val cognitoId = jwt.claims["sub"] as String?
            if (cognitoId != null) {
                grantedAuthorities?.add(GrantedAuthority { "COGNITO_ID_$cognitoId" })
            }
            grantedAuthorities
        }

        return jwtAuthenticationConverter
    }
}