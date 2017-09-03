package de.cwrose.shoppinglist.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
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
import java.io.Serializable
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class JwtAuthenticationTokenFilter(val userDetailsService: UserDetailsService): OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {

        val token = request.getHeader("Authorization")

        var username: String? = null
        var authToken: String? = null

        if (token != null && token.startsWith("Bearer ") && token.length > 10) {
            authToken = token.substring(7)
            username = getUsernameFromToken(authToken)
        }

        if (username != null && SecurityContextHolder.getContext().authentication == null) {
            userDetailsService.loadUserByUsername(username).let { userDetails ->
                if (validateToken(authToken!!, userDetails)) {
                    UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities).apply {
                        details = WebAuthenticationDetailsSource().buildDetails(request)
                    }.let {
                        SecurityContextHolder.getContext().authentication = it
                    }
                }
            }
        }

        filterChain.doFilter(request, response)
    }
}

@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint, Serializable {

    @Throws(IOException::class)
    override fun commence(request: HttpServletRequest,
                 response: HttpServletResponse,
                 authException: AuthenticationException) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized")
    }

    companion object {

        private const val serialVersionUID = -1L
    }
}

@Configuration
class JwtMvcConfig: WebMvcConfigurerAdapter() {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "DELETE");
    }
}

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class JwtWebSecurityConfig (val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint, val userDetailsService: UserDetailsService) : WebSecurityConfigurerAdapter() {

    @Autowired
    fun configureAuthentication(authenticationManagerBuilder: AuthenticationManagerBuilder) {
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder())
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun authenticationTokenFilter(): JwtAuthenticationTokenFilter = JwtAuthenticationTokenFilter(userDetailsService)

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.setAllowedOrigins(listOf("*"))
        configuration.setAllowedMethods(listOf("GET", "POST", "DELETE"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(listOf("Authorization", "Cache-Control", "Content-Type"));
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    override fun configure(http: HttpSecurity) {
        http.cors();

        http.csrf().disable()
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests().antMatchers("/auth").permitAll()
                .anyRequest().authenticated()

        http.addFilterBefore(authenticationTokenFilter(), UsernamePasswordAuthenticationFilter::class.java)
        http.headers().cacheControl()
    }
}

