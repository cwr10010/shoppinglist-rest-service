package de.cwrose.shoppinglist.auth

import de.cwrose.shoppinglist.AuthorityName
import de.cwrose.shoppinglist.UserRepository
import io.jsonwebtoken.JwtException
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class JwtAuthenticationTokenFilter(private val userRepository: UserRepository, private val userDetailsService: UserDetailsService, private val jwtService: JwtService) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) =
            request.getHeader("Authorization").let { token ->
                when (token != null && token.startsWith("Bearer ")) {
                    true -> token.substring(7).let { authToken ->
                        try {
                            jwtService.getUsernameFromToken(authToken)
                        } catch (ex: JwtException) {
                            logger.warn("Token invalid")
                            null
                        }.let { username ->
                            when (username) {
                                null -> logger.warn("No username in authorization token")
                                else -> when (SecurityContextHolder.getContext()) {
                                    null -> logger.warn("Security context empty")
                                    else -> {
                                        updateAuthentication(request, username, authToken)
                                    }
                                }
                            }
                        }
                    }
                }
            }.let {
                filterChain.doFilter(request, response)
            }

    private fun updateAuthentication(request: HttpServletRequest, username: String, authToken: String) =
            userDetailsService.loadUserByUsername(username).let { userDetails ->
                checkIfUserMatchesUriId(request, userDetails)
                if (jwtService.validateToken(authToken, userDetails)) {
                    UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities).apply {
                        details = WebAuthenticationDetailsSource().buildDetails(request)
                    }.let {
                        SecurityContextHolder.getContext().authentication = it
                    }
                }
            }

    private fun checkIfUserMatchesUriId(request: HttpServletRequest, userDetails: UserDetails) =
            takeUserIdFromRequest(request).let { userIdFromRequestUri ->
                when {
                    userIdFromRequestUri.isEmpty() -> logger.debug("no user id in url, skipping check")
                    else -> if (AuthorityName.ROLE_ADMIN.name !in userDetails.authorities.map { it.authority }) {
                        userRepository.findOne(userIdFromRequestUri).let {
                            when (it) {
                                null -> throw BadCredentialsException("UNKNOWN: Access denied")
                                else -> if (!it.username.equals(userDetails.username)) {
                                    throw BadCredentialsException("NOT ALLOWED: Access denied")
                                }
                            }
                        }
                    }
                }
            }


    private fun takeUserIdFromRequest(request: HttpServletRequest) =
            request.requestURI.let {
                when {
                    it.startsWith("/api/users") -> {
                        it.substringAfter("users")
                            .substringBefore("shopping-list")
                            .replace("/", "")
                    }
                    else -> {
                        ""
                    }
                }
            }

    companion object : KLogging()
}

@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {

    @Throws(IOException::class)
    override fun commence(request: HttpServletRequest,
                          response: HttpServletResponse,
                          authException: AuthenticationException) =
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized")

}

@Configuration
class JwtMvcConfig : WebMvcConfigurerAdapter() {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "DELETE")
    }
}

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class JwtWebSecurityConfig(val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint, val userDetailsService: UserDetailsService, val userRepository: UserRepository, val jwtService: JwtService) : WebSecurityConfigurerAdapter() {

    @Autowired
    fun configureAuthentication(authenticationManagerBuilder: AuthenticationManagerBuilder, passwordEncoder: PasswordEncoder) {
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder)
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun authenticationTokenFilter(): JwtAuthenticationTokenFilter = JwtAuthenticationTokenFilter(userRepository, userDetailsService, jwtService)

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource =
            CorsConfiguration().apply {
                allowedOrigins = listOf("*")
                allowedMethods = listOf("GET", "POST", "DELETE")
                allowCredentials = true
                allowedHeaders = listOf("Authorization", "Cache-Control", "Content-Type")
            }.let { configuration ->
                UrlBasedCorsConfigurationSource().let {
                    it.registerCorsConfiguration("/**", configuration)
                    it
                }
            }

    override fun configure(http: HttpSecurity) =
            http.let {
                it.cors()
                it.csrf().disable()
                        .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
                        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                        .authorizeRequests().antMatchers("/auth", "/register").permitAll()
                        .anyRequest().authenticated()
                it.addFilterBefore(authenticationTokenFilter(), UsernamePasswordAuthenticationFilter::class.java)
                it.headers().cacheControl()
                Unit
            }
}
