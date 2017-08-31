package de.cwrose.shoppinglist

import de.cwrose.shoppinglist.auth.JwtUser
import de.cwrose.shoppinglist.auth.generateToken
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.StringUtils
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.test.assertEquals

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationResourceTest: TestBase() {

    @Test
    fun authenticate() {
        val authenticate = authenticate("")

        assertEquals(HttpStatus.OK, authenticate.statusCode)
        assert(!StringUtils.isEmpty(authenticate.body))
    }

    @Test
    fun refresh() {
        val authenticate = authenticate("")

        assertEquals(HttpStatus.OK, authenticate.statusCode)

        val refresh = refresh(extractToken(authenticate.body))
        assertEquals(HttpStatus.OK, refresh.statusCode)
        assert(!StringUtils.isEmpty(refresh.body))

    }

    @Test
    fun refreshNoToken() {

        val refresh = refresh("")
        assertEquals(HttpStatus.BAD_REQUEST, refresh.statusCode)
    }

    @Test
    fun refreshExpiredToken() {

        val token = generateToken(
                JwtUser("id", ADMIN.json["username"] as String, "password"),
                Date(LocalDateTime.now().minusDays(30).toInstant(ZoneOffset.UTC).toEpochMilli()))
        val refresh = refresh(token)
        assertEquals(HttpStatus.BAD_REQUEST, refresh.statusCode)
    }

    @Test
    fun checkUnauthorized() {

        val response = createUser(USER_1.toString(), "")
        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }
}