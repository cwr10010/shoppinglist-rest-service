package de.cwrose.shoppinglist.auth

import io.jsonwebtoken.*
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.codec.Base64
import java.util.Date

private val SECRET = Base64.encode("ABC".toByteArray())
private val EXPIRATION = 1000L

internal fun validateToken(token: String, userDetails: UserDetails) = getUsernameFromToken(token).let {
        userDetails.username == it && !isTokenExpired(token)
    }

internal fun isTokenExpired(token: String) = try {
        getAllClaimsFromToken(token).expiration.before(Date())
    } catch (ex: ExpiredJwtException) {
        true
    }

internal fun getUsernameFromToken(token: String) = try {
        getAllClaimsFromToken(token).subject
    }  catch (ex: JwtException) {
        null
    }

private fun getAllClaimsFromToken(token: String) = Jwts.parser()
        .setSigningKey(SECRET)
        .parseClaimsJws(token)
        .getBody()

internal fun generateToken(user: JwtUser, issueDate: Date = Date()) = Jwts.builder()
        .setClaims(hashMapOf("id" to user.id as Any))
        .setSubject(user.username)
        .setIssuedAt(issueDate)
        .setExpiration(Date(issueDate.time + EXPIRATION * 1000))
        .signWith(SignatureAlgorithm.HS512, SECRET)
        .compact()

internal fun refreshToken(token: String) = getAllClaimsFromToken(token).apply { issuedAt = Date() }.let(::doRefreshToken)

private fun doRefreshToken(claims: Claims) = Jwts.builder()
        .setClaims(claims)
        .signWith(SignatureAlgorithm.HS512, SECRET)
        .compact()
