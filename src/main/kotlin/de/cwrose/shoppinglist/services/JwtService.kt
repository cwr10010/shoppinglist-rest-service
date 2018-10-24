package de.cwrose.shoppinglist.services

import de.cwrose.shoppinglist.*
import de.cwrose.shoppinglist.auth.JwtUser
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import javax.servlet.http.Cookie

@Service
class JwtService(val refreshTokenRepository: RefreshTokenRepository) {

    @Value("\${jwt.secret}")
    fun setSecret(secret: String) {
        this.secret = Base64.getEncoder().encode(secret.toByteArray())
    }
    lateinit var secret: ByteArray

    @Value("\${refresh.cookie.secure}")
    var refreshCookieSecure: Boolean = false

    @Value("\${refresh.cookie.path}")
    var refreshCookiePath: String = "/"

    fun generateAuthToken(user: JwtUser, issueDate: Date = Date()): String = Jwts.builder()
            .setSubject(user.username)
            .setIssuedAt(issueDate)
            .setExpiration(Date(issueDate.time + EXPIRATION * 1000L))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()

    fun generateRefreshToken(refreshId: String, issueDate: Date = Date()): String = Jwts.builder()
            .setClaims(hashMapOf("refresh_id" to refreshId as Any))
            .setSubject("RefreshToken")
            .setIssuedAt(issueDate)
            .setExpiration(Date(issueDate.time + REFRESH_EXPIRATION * 1000L))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()

    fun generateIDToken(user: JwtUser, issueDate: Date = Date()): String = Jwts.builder()
            .setClaims(hashMapOf("id" to user.id as Any))
            .setSubject("IdToken")
            .setIssuedAt(issueDate)
            .setExpiration(Date(issueDate.time + EXPIRATION * 1000L))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()

    fun generateRegistrationToken(username: String, issueDate: Date = Date()): String = Jwts.builder()
            .setClaims(hashMapOf("username" to username as Any))
            .setSubject("RegistrationToken")
            .setIssuedAt(issueDate)
            .setExpiration(Date(issueDate.time + TOKEN_EXPIRATION * 1000L))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()

    fun generateShareToken(sharedListId: String, issueDate: Date = Date()): String = Jwts.builder()
            .setClaims(hashMapOf(
                    "shared_list_id" to sharedListId as Any
            ))
            .setSubject("ShareToken")
            .setIssuedAt(issueDate)
            .setExpiration(Date(issueDate.time + TOKEN_EXPIRATION * 1000L))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()

    fun updateRefreshToken(token: String, issueDate: Date = Date()): String =
            getAllClaimsFromToken(token).apply {
                issuedAt = issueDate
            }.let(this::doUpdateToken)

    fun validateToken(token: String, userDetails: UserDetails): Boolean =
            getUsernameFromToken(token).let {
                userDetails.username == it
            }

    fun getUsernameFromToken(token: String): String = getAllClaimsFromToken(token).subject

    fun getRefreshTokenId(refreshToken: String): String = getAllClaimsFromToken(refreshToken)["refresh_id"] as String

    fun getRegistrationTokenUser(registrationToken: String): String = getAllClaimsFromToken(registrationToken)["username"] as String

    fun getSharedListId(sharedListToken: String): String = getAllClaimsFromToken(sharedListToken)["shared_list_id"] as String

    fun createRefreshToken(user: User): String = generateRefreshToken(
            refreshTokenRepository.save(
                    RefreshToken(user, Instant.now().plusSeconds(REFRESH_EXPIRATION.toLong())
                    )
            ).id!!)

    fun findValidToken(token: String): Optional<RefreshToken> =
            getRefreshTokenId(token).let {requestedTokenId ->
                refreshTokenRepository.findById(requestedTokenId).filter { refreshToken ->
                    refreshToken.valid!!
                }
            }

    fun createOrUpdateRefreshCookie(user: User, token: String? = null) =
            when (token) {
                null -> createRefreshToken(user)
                else -> {
                    getRefreshTokenId(token).let {
                        refreshTokenRepository.findById(it).ifPresent { refreshToken ->
                            refreshToken.apply {
                                expires = Instant.now().plusSeconds(REFRESH_EXPIRATION.toLong())
                            }.let { thisToken ->
                                refreshTokenRepository.save(thisToken)
                            }
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
                maxAge = REFRESH_EXPIRATION
            }

    fun emptyRefreshCookie() =
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
        private const val EXPIRATION = 1000L
        private const val REFRESH_EXPIRATION = 31 * 24 * 60 * 60
        const val TOKEN_EXPIRATION = 60 * 60 * 24
    }
}
