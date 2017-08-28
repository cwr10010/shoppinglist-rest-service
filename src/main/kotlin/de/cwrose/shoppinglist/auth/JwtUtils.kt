package de.cwrose.shoppinglist.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.codec.Base64
import java.util.Date

private val SECRET = Base64.encode("ABC".toByteArray())
private val EXPIRATION = 100L

internal fun validateToken(token: String, userDetails: UserDetails): Boolean {
    val user = userDetails as JwtUser
    val username = getUsernameFromToken(token)
    return username == user.username
            && !isTokenExpired(token)
}

internal fun isTokenExpired(token: String) = getExpirationDateFromToken(token).before(Date())

internal fun getExpirationDateFromToken(token: String) = getAllClaimsFromToken(token).expiration

internal fun getUsernameFromToken(token: String) = getAllClaimsFromToken(token).subject

private fun getAllClaimsFromToken(token: String) = Jwts.parser()
        .setSigningKey(SECRET)
        .parseClaimsJws(token)
        .getBody()

internal fun generateToken(userDetails: UserDetails) = Jwts.builder()
        .setClaims(hashMapOf())
        .setSubject(userDetails.username)
        .setIssuedAt(Date())
        .setExpiration(Date(Date().time + EXPIRATION * 1000))
        .signWith(SignatureAlgorithm.HS512, SECRET)
        .compact()

internal fun refreshToken(token: String) = getAllClaimsFromToken(token).apply { issuedAt = Date() }.let(::doRefreshToken)

private fun doRefreshToken(claims: Claims) = Jwts.builder()
        .setClaims(claims)
        .signWith(SignatureAlgorithm.HS512, SECRET)
        .compact()
