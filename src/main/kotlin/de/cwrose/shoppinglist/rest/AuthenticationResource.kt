package de.cwrose.shoppinglist.rest

import de.cwrose.shoppinglist.JwtAuthenticationRequest
import de.cwrose.shoppinglist.RefreshTokenRepository
import de.cwrose.shoppinglist.auth.JwtUser
import de.cwrose.shoppinglist.auth.JwtUserDetailsService
import de.cwrose.shoppinglist.auth.JwtService
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
        val jwtService: JwtService) {

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
                jwtService.getRefreshTokenId(refreshToken).let {
                    refreshTokenRepository.findOne(it).let { token ->
                        when (token?.valid) {
                            true -> authenticate(response, token.user!!.username!!, refreshToken).let {
                                ResponseEntity.ok(it)
                            }
                            else -> throw BadCredentialsException("Invalid Refresh Token")
                        }
                    }
                }
            }

    @GetMapping("logout")
    fun logout(response: HttpServletResponse) = response.addCookie(jwtService.emptyRefreshCookie())

    fun authenticate(response: HttpServletResponse, username: String, token: String? = null) =
            jwtService.createOrUpdateRefreshCookie(username, token).let  {
                cookie -> response.addCookie(cookie)
            } .let {
                userDetailsService.loadUserByUsername(username).let { userDetails ->
                    jwtService.generateRegistrationResponse(userDetails as JwtUser)
                }
            }
}
