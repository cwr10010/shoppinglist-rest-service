package de.cwrose.shoppinglist.rest

import de.cwrose.shoppinglist.auth.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/auth")
class AuthenticationResource @Autowired constructor(val authenticationManager: AuthenticationManager, val userDetailsService: JwtUserDetailsService) {

    @PostMapping
    fun createAuthToken(@RequestBody authenticationRequest: JwtAuthenticationRequest): ResponseEntity<JwtAuthenticationResponse> {

        authenticationManager.authenticate(UsernamePasswordAuthenticationToken(authenticationRequest.username, authenticationRequest.password)).let {
            authentication ->  SecurityContextHolder.getContext().authentication = authentication
        }

        return userDetailsService.loadUserByUsername(authenticationRequest.username).let {
            userDetails ->  generateToken(userDetails)
        }.let {
            token -> ResponseEntity.ok(JwtAuthenticationResponse(token))
        }
    }

    @GetMapping
    fun refreshAuthToken(request: HttpServletRequest): ResponseEntity<JwtAuthenticationResponse> {
        val token = request.getHeader("Authentication")

        return when {
            !isTokenExpired(token) -> refreshToken(token).let {
                ResponseEntity.ok(JwtAuthenticationResponse(token))
            }
            else -> ResponseEntity.badRequest().body(null)
        }
    }
}

data class JwtAuthenticationResponse(val token: String)

data class JwtAuthenticationRequest(val username: String, val password: String)
