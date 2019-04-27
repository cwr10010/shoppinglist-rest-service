package de.cwrose.shoppinglist.ct

import de.cwrose.shoppinglist.Application
import de.cwrose.shoppinglist.auth.JwtUser
import de.cwrose.shoppinglist.services.JwtService
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.StringUtils
import java.net.HttpCookie
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.assertEquals

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [Application::class])
class AuthenticationResourceTest : TestBase() {

    @Autowired
    private lateinit var jwtService: JwtService

    @Test
    fun testAuthenticateOk() {
        validAuthResponse(authenticate())
    }

    @Test
    fun testRefreshOk() {
        authenticate().let {
            assertEquals(HttpStatus.OK, it.statusCode)

            validAuthResponse(refresh(extractRefreshTokenFromCookie(it)))
        }
    }

    @Test
    fun testAuthTokenInvalid() {
        assertEquals(HttpStatus.FORBIDDEN, createUser(USER_1.toString(), "Bearer abcdefg").statusCode)
    }

    @Test
    fun testAuthTokenEmpty() {
        assertEquals(HttpStatus.FORBIDDEN, createUser(USER_1.toString(), "").statusCode)
    }

    private fun validAuthResponse(response: ResponseEntity<String>) {
        assertEquals(HttpStatus.OK, response.statusCode)
        extractToken(response.body).let { token ->
            assert(!StringUtils.isEmpty(token.auth_token))
            assert(!StringUtils.isEmpty(token.id_token))
        }
        assert(extractRefreshTokenFromCookie(response).startsWith("RefreshCookie="))
    }

    private fun validLogoutResponse(response: ResponseEntity<String>) {
        assertEquals(HttpStatus.OK, response.statusCode)
        assert(extractRefreshTokenFromCookie(response).startsWith("RefreshCookie=;"))
    }

    @Test
    fun testInvalidRefreshTokenBadRequest() {
        val refreshCookieToken = authenticate().let {
            assertEquals(HttpStatus.OK, it.statusCode)
            extractRefreshTokenFromCookie(it)
        }

        invalidateAllRefreshToken()

        assertEquals(HttpStatus.BAD_REQUEST, refresh(refreshCookieToken).statusCode)
    }

    @Test
    fun testCheckMalformedRefreshTokenBadRequest() {
        assertEquals(HttpStatus.OK, authenticate().statusCode)

        assertEquals(HttpStatus.BAD_REQUEST, refresh("0123456789ABCDEF").statusCode)
    }

    @Test
    fun testRefreshNoRefreshTokenBadRequest() {
        assertEquals(HttpStatus.BAD_REQUEST, refresh("").statusCode)
    }

    @Test
    fun testRefreshTokenExpiredForbidden() {
        jwtService.generateAuthToken(
                JwtUser("id", ADMIN.json["username"] as String, "password", true, emptyList()),
                Date.from(Instant.now().minus(30, ChronoUnit.DAYS))).let { authToken ->

            assertEquals(HttpStatus.FORBIDDEN, refresh(HttpCookie("RefreshCookie", authToken).toString()).statusCode)
        }
    }

    @Test
    fun testLogout() {
        authenticate().let { response ->
            validAuthResponse(response).let {
                val token = extractToken(response.body)
                validLogoutResponse(logout(token.auth_token))
            }
        }
    }
}