package de.cwrose.shoppinglist

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.StringUtils
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

        val refresh = refresh(extractToken(authenticate.body).token)
        assertEquals(HttpStatus.OK, refresh.statusCode)
        assert(!StringUtils.isEmpty(refresh.body))

    }

}