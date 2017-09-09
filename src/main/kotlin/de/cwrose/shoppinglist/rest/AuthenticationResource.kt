package de.cwrose.shoppinglist.rest

import com.fasterxml.jackson.annotation.JsonProperty
import de.cwrose.shoppinglist.RefreshToken
import de.cwrose.shoppinglist.RefreshTokenRepository
import de.cwrose.shoppinglist.UserRepository
import de.cwrose.shoppinglist.auth.JwtUser
import de.cwrose.shoppinglist.auth.JwtUserDetailsService
import de.cwrose.shoppinglist.auth.REFRESH_EXPIRATION
import de.cwrose.shoppinglist.auth.TOKEN_EXPIRATION
import de.cwrose.shoppinglist.auth.generateAuthToken
import de.cwrose.shoppinglist.auth.generateIDToken
import de.cwrose.shoppinglist.auth.generateRefreshToken
import de.cwrose.shoppinglist.auth.getRefreshTokenId
import de.cwrose.shoppinglist.auth.updateRefreshToken

import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/auth")
class AuthenticationResource(
        val authenticationManager: AuthenticationManager,
        val userDetailsService: JwtUserDetailsService,
        val refreshTokenRepository: RefreshTokenRepository,
        val userRepository: UserRepository) {

    @PostMapping
    fun createAuthToken(@RequestBody authenticationRequest: JwtAuthenticationRequest, response: HttpServletResponse) =
            UsernamePasswordAuthenticationToken(authenticationRequest.username, authenticationRequest.password).let {
                authenticationManager.authenticate(it).let {
                    SecurityContextHolder.getContext().authentication = it
                }.let {
                    authenticate(response, authenticationRequest.username).let {
                        ResponseEntity.ok(it)
                    }
                }
            }

    @GetMapping
    fun refreshAuthToken(response: HttpServletResponse, @CookieValue("RefreshCookie") refreshCookie: Cookie) =
            refreshCookie.value.let { refreshToken ->
                getRefreshTokenId(refreshToken).let {
                    refreshTokenRepository.findOne(it).let { token ->
                        when (token.valid) {
                            true -> authenticate(response, token.user!!.username!!, refreshToken).let {
                                ResponseEntity.ok(it)
                            }
                            else -> throw BadCredentialsException("Invalid Refresh Token")
                        }
                    }
                }
            }

    @GetMapping("logout")
    fun logout(response: HttpServletResponse) = response.addCookie(deleteRefreshCookie())

    fun authenticate(response: HttpServletResponse, username: String, token: String? = null) =
            createOrUpdateRefreshToken(response, username, token).let {
                userDetailsService.loadUserByUsername(username).let { userDetails ->
                    JwtAuthenticationResponse(
                            generateAuthToken(userDetails as JwtUser),
                            generateIDToken(userDetails),
                            TOKEN_EXPIRATION)
                }
            }

    fun createOrUpdateRefreshToken(response: HttpServletResponse, username: String, token: String? = null) =
            when (token) {
                null -> createRefreshToken(username)
                else -> {
                    getRefreshTokenId(token).let {
                        refreshTokenRepository.findOne(it).apply {
                            expires = DateTime.now().plusSeconds(REFRESH_EXPIRATION).toDate()
                        }.let {
                            refreshTokenRepository.save(it)
                        }
                    }
                    updateRefreshToken(token)
                }
            }.let(this::secureRefreshCookie).let(response::addCookie)

    private fun createRefreshToken(username: String) =
            userRepository.findByUsername(username).let { user ->
                refreshTokenRepository.save(RefreshToken(user, DateTime.now().plusSeconds(REFRESH_EXPIRATION).toDate()))
            }.let {
                generateRefreshToken(it.id!!)
            }

    @Value("\${refresh.cookie.secure}")
    var refreshCookieSecure: Boolean = false

    @Value("\${refresh.cookie.path}")
    var refreshCookiePath: String = "/"

    private fun secureRefreshCookie(value: String) =
            Cookie("RefreshCookie", value).apply {
                path = refreshCookiePath
                secure = refreshCookieSecure
                isHttpOnly = true
                maxAge = REFRESH_EXPIRATION
            }

    private fun deleteRefreshCookie() =
            Cookie("RefreshCookie", null).apply {
                path = refreshCookiePath
                secure = refreshCookieSecure
                isHttpOnly = true
                maxAge = 0
            }

}

data class JwtAuthenticationResponse(
        @JsonProperty("auth_token")
        val authToken: String? = null,

        @JsonProperty("id_token")
        val idToken: String? = null,

        val expires: Number? = null
)

data class JwtAuthenticationRequest(val username: String, val password: String)
