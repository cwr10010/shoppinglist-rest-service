package de.cwrose.shoppinglist.ct

import de.cwrose.shoppinglist.Application
import de.cwrose.shoppinglist.auth.JwtUser
import de.cwrose.shoppinglist.auth.generateAuthToken
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.StringUtils
import java.net.HttpCookie
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date
import kotlin.test.assertEquals

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = arrayOf(Application::class))
class AuthenticationResourceTest : TestBase() {

    @Test
    fun testAuthenticateOk() {
        authenticate().let {
            validAuthResponse(it)
        }
    }

    @Test
    fun testRefreshOk() {
        authenticate().let {
            assertEquals(HttpStatus.OK, it.statusCode)

            extractRefreshTokenFromCookie(it).let { refreshCookieToken ->
                refresh(refreshCookieToken).let { refresh ->
                    validAuthResponse(refresh)
                }
            }
        }
    }

    @Test
    fun testAuthTokenInvalid() {
        authenticate().let {
            createUser(USER_1.toString(), "Bearer abcdefg").let {
                assertEquals(HttpStatus.FORBIDDEN, it.statusCode)
            }
        }
    }

    @Test
    fun testAuthTokenEmpty() {
        authenticate().let {
            createUser(USER_1.toString(), "").let {
                assertEquals(HttpStatus.FORBIDDEN, it.statusCode)
            }
        }
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

        refresh(refreshCookieToken).let {
            assertEquals(HttpStatus.BAD_REQUEST, it.statusCode)
        }
    }

    @Test
    fun testCheckMalformedRefreshTokenBadRequest() {
        authenticate().let {
            assertEquals(HttpStatus.OK, it.statusCode)
        }

        refresh("0123456789ABCDEF").let {
            assertEquals(HttpStatus.BAD_REQUEST, it.statusCode)
        }
    }

    @Test
    fun testRefreshNoRefreshTokenBadRequest() {
        refresh("").let {
            assertEquals(HttpStatus.BAD_REQUEST, it.statusCode)
        }
    }

    @Test
    fun testRefreshTokenExpiredForbidden() {
        generateAuthToken(
                JwtUser("id", ADMIN.json["username"] as String, "password"),
                Date(LocalDateTime.now().minusDays(30).toInstant(ZoneOffset.UTC).toEpochMilli())).let {

            HttpCookie("RefreshCookie", it).let { cookie ->
                refresh(cookie.toString()).let { refreshResult ->
                    assertEquals(HttpStatus.FORBIDDEN, refreshResult.statusCode)
                }
            }
        }
    }

    @Test
    fun testCheckUnauthorizedAccesIsForbidden() {
        createUser(USER_1.toString(), "").let {
            assertEquals(HttpStatus.FORBIDDEN, it.statusCode)
        }
    }

    @Test
    fun testLogout() {
        authenticate().let { response ->
            validAuthResponse(response).let {
                var token = extractToken(response.body)
                logout(token.auth_token).let {
                    validLogoutResponse(it)
                }
            }
        }

    }
}