package de.cwrose.shoppinglist.auth

import io.jsonwebtoken.*
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.codec.Base64
import java.util.Date

private val SECRET = Base64.encode("ABC".toByteArray())
private val EXPIRATION = 1000L
val TOKEN_EXPIRATION = 60 * 60 * 24
val REFRESH_EXPIRATION = 31 * 24 * 60 * 60

internal fun generateAuthToken(user: JwtUser, issueDate: Date = Date()) = Jwts.builder()
        .setSubject(user.username)
        .setIssuedAt(issueDate)
        .setExpiration(Date(issueDate.time + EXPIRATION * 1000L))
        .signWith(SignatureAlgorithm.HS512, SECRET)
        .compact()

internal fun generateRefreshToken(refreshId: String, issueDate: Date = Date()) = Jwts.builder()
        .setClaims(hashMapOf("refresh_id" to refreshId as Any))
        .setSubject("RefreshToken")
        .setIssuedAt(issueDate)
        .setExpiration(Date(issueDate.time + REFRESH_EXPIRATION * 1000L))
        .signWith(SignatureAlgorithm.HS512, SECRET)
        .compact()

internal fun generateIDToken(user: JwtUser, issueDate: Date = Date()) = Jwts.builder()
        .setClaims(hashMapOf("id" to user.id as Any))
        .setSubject("IdToken")
        .setIssuedAt(issueDate)
        .setExpiration(Date(issueDate.time + EXPIRATION * 1000L))
        .signWith(SignatureAlgorithm.HS512, SECRET)
        .compact()

internal fun updateRefreshToken(token: String, issueDate: Date = Date()) =
        getAllClaimsFromToken(token).apply {
            issuedAt = issueDate
        }.let(::doUpdateToken)

internal fun validateToken(token: String, userDetails: UserDetails) =
        getUsernameFromToken(token).let {
            userDetails.username == it
        }

internal fun getUsernameFromToken(token: String) = getAllClaimsFromToken(token).subject

internal fun getRefreshTokenId(refreshToken: String) = getAllClaimsFromToken(refreshToken)["refresh_id"] as String

private fun getAllClaimsFromToken(token: String) = Jwts.parser()
        .setSigningKey(SECRET)
        .parseClaimsJws(token)
        .body

private fun doUpdateToken(claims: Claims) = Jwts.builder()
        .setClaims(claims)
        .signWith(SignatureAlgorithm.HS512, SECRET)
        .compact()
