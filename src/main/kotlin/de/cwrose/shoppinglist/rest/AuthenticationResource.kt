package de.cwrose.shoppinglist.rest

import de.cwrose.shoppinglist.JwtAuthenticationRequest
import de.cwrose.shoppinglist.UserRepository
import de.cwrose.shoppinglist.auth.JwtUser
import de.cwrose.shoppinglist.auth.JwtUserDetailsService
import de.cwrose.shoppinglist.services.JwtService
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/auth")
class AuthenticationResource(
        val authenticationManager: AuthenticationManager,
        val userDetailsService: JwtUserDetailsService,
        val userRepository: UserRepository,
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
                jwtService.findValidToken(refreshToken).map {
                    authenticate(response, it.user!!.username!!, refreshToken).let {
                        ResponseEntity.ok(it)
                    }
                } .orElseThrow {
                    BadCredentialsException("Invalid Refresh Token")
                }
            }

    @GetMapping("logout")
    fun logout(response: HttpServletResponse) = response.addCookie(jwtService.emptyRefreshCookie())

    fun authenticate(response: HttpServletResponse, username: String, token: String? = null) =
            userRepository.findByUsername(username).map { user ->
                jwtService.createOrUpdateRefreshCookie(user, token).let  {
                    cookie -> response.addCookie(cookie)
                } .let {
                    userDetailsService.loadUserByUsername(username).let { userDetails ->
                        jwtService.generateRegistrationResponse(userDetails as JwtUser)
                    }
                }
            } .orElseThrow {
                UnknownTokenUserException("Unknown user to authenticate")
            }
}


class UnknownTokenUserException(message: String): RuntimeException(message)
