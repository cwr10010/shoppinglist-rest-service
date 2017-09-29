package de.cwrose.shoppinglist.auth

import de.cwrose.shoppinglist.JwtAuthenticationResponse
import de.cwrose.shoppinglist.RefreshToken
import de.cwrose.shoppinglist.RefreshTokenRepository
import de.cwrose.shoppinglist.UserRepository
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.codec.Base64
import org.springframework.stereotype.Service
import java.util.Date
import javax.servlet.http.Cookie

@Service
class JwtService(val refreshTokenRepository: RefreshTokenRepository,
                 val userRepository: UserRepository) {

    @Value("\${jwt.secret}")
    fun setSecret(secret: String) {
        this.secret = Base64.encode(secret.toByteArray())
    }
    lateinit var secret: ByteArray

    @Value("\${refresh.cookie.secure}")
    var refreshCookieSecure: Boolean = false

    @Value("\${refresh.cookie.path}")
    var refreshCookiePath: String = "/"

    internal fun generateAuthToken(user: JwtUser, issueDate: Date = Date()) = Jwts.builder()
            .setSubject(user.username)
            .setIssuedAt(issueDate)
            .setExpiration(Date(issueDate.time + EXPIRATION * 1000L))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()

    internal fun generateRefreshToken(refreshId: String, issueDate: Date = Date()) = Jwts.builder()
            .setClaims(hashMapOf("refresh_id" to refreshId as Any))
            .setSubject("RefreshToken")
            .setIssuedAt(issueDate)
            .setExpiration(Date(issueDate.time + REFRESH_EXPIRATION * 1000L))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()

    internal fun generateIDToken(user: JwtUser, issueDate: Date = Date()) = Jwts.builder()
            .setClaims(hashMapOf("id" to user.id as Any))
            .setSubject("IdToken")
            .setIssuedAt(issueDate)
            .setExpiration(Date(issueDate.time + EXPIRATION * 1000L))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()

    internal fun generateRegistrationToken(username: String, issueDate: Date = Date()) = Jwts.builder()
            .setClaims(hashMapOf("username" to username as Any))
            .setSubject("RegistrationToken")
            .setIssuedAt(issueDate)
            .setExpiration(Date(issueDate.time + Companion.TOKEN_EXPIRATION * 1000L))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()

    internal fun updateRefreshToken(token: String, issueDate: Date = Date()) =
            getAllClaimsFromToken(token).apply {
                issuedAt = issueDate
            }.let(this::doUpdateToken)

    internal fun validateToken(token: String, userDetails: UserDetails) =
            getUsernameFromToken(token).let {
                userDetails.username == it
            }

    internal fun getUsernameFromToken(token: String) = getAllClaimsFromToken(token).subject

    internal fun getRefreshTokenId(refreshToken: String) = getAllClaimsFromToken(refreshToken)["refresh_id"] as String

    internal fun getRegistrationTokenUser(registrationToken: String) = getAllClaimsFromToken(registrationToken)["username"] as String

    internal fun createRefreshToken(username: String) =
            userRepository.findByUsername(username).let { user ->
                refreshTokenRepository.save(RefreshToken(user, DateTime.now().plusSeconds(REFRESH_EXPIRATION).toDate()))
            }.let {
                generateRefreshToken(it.id!!)
            }

    fun createOrUpdateRefreshCookie(username: String, token: String? = null) =
            when (token) {
                null -> createRefreshToken(username)
                else -> {
                    getRefreshTokenId(token).let {
                        refreshTokenRepository.findOne(it).apply {
                            expires = DateTime.now().plusSeconds(JwtService.REFRESH_EXPIRATION).toDate()
                        }.let {
                            refreshTokenRepository.save(it)
                        }
                    }
                    updateRefreshToken(token)
                }
            } .let(this::secureRefreshCookie)

    private fun secureRefreshCookie(value: String) =
            Cookie("RefreshCookie", value).apply {
                path = refreshCookiePath
                secure = refreshCookieSecure
                isHttpOnly = true
                maxAge = JwtService.REFRESH_EXPIRATION
            }

    internal fun deleteRefreshCookie() =
            Cookie("RefreshCookie", null).apply {
                path = refreshCookiePath
                secure = refreshCookieSecure
                isHttpOnly = true
                maxAge = 0
            }

    private fun getAllClaimsFromToken(token: String) = Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .body

    private fun doUpdateToken(claims: Claims) = Jwts.builder()
            .setClaims(claims)
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()

    fun generateRegistrationResponse(userDetails: JwtUser) =
            JwtAuthenticationResponse(
                    generateAuthToken(userDetails),
                    generateIDToken(userDetails),
                    TOKEN_EXPIRATION)

    companion object {
        private val EXPIRATION = 1000L
        private val REFRESH_EXPIRATION = 31 * 24 * 60 * 60
        val TOKEN_EXPIRATION = 60 * 60 * 24
    }
}
