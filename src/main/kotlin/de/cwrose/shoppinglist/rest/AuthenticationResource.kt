package de.cwrose.shoppinglist.rest

import de.cwrose.shoppinglist.auth.JwtUserDetailsService
import de.cwrose.shoppinglist.auth.generateToken
import de.cwrose.shoppinglist.auth.isTokenExpired
import de.cwrose.shoppinglist.auth.refreshToken
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/auth")
class AuthenticationResource (val authenticationManager: AuthenticationManager, val userDetailsService: JwtUserDetailsService) {

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
        val authorizationHeader = request.getHeader("Authorization")

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ") && authorizationHeader.length > 10) {
            var token = authorizationHeader.substring(7)
            return when {
                !isTokenExpired(token) -> refreshToken(token).let {
                    ResponseEntity.ok(JwtAuthenticationResponse(token))
                }
                else -> ResponseEntity.badRequest().body(null)
            }
        }
        return ResponseEntity.badRequest().body(null)
    }
}

data class JwtAuthenticationResponse(val token: String)

data class JwtAuthenticationRequest(val username: String, val password: String)
