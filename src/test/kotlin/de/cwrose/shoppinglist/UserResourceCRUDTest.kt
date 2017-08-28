package de.cwrose.shoppinglist

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserResourceCRUDTest : TestBase() {


    @Test
    fun test1CRD() {
        val authenticate = authenticate("")
        val token = extractToken(authenticate.body)
        val response = createUser(USER_1.toString(), token.token)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        val location = response.headers.location

        readUser(location, token.token).let {
            assertEquals(HttpStatus.OK, it.statusCode)
            assertEquals(HttpStatus.OK, it.statusCode)
            assertEquals("Max", it.body.username)
            assertEquals("p4ssw0rd", it.body.password)
        }

        deleteUser(location, token.token)

        readUser(location, token.token).let {
            assertEquals(HttpStatus.NOT_FOUND, it.statusCode)
        }
    }

    @Test
    fun test2CUD() {
        val authenticate = authenticate("")
        val token = extractToken(authenticate.body)
        val response = createUser(USER_1.toString(), token.token)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        val location = response.headers.location

        Json {
            "user_id" To extractId(location)
            "username" To "Mini"
            "password" To "p4ssw0rd2"
            "shopping_list" To emptyList<Json>()
        }.let {
            user -> updateUser(location, user.toString(), token.token).let {
                assertEquals(HttpStatus.OK, it.statusCode)
                assertEquals("Mini", it.body.username)
                assertEquals("p4ssw0rd2", it.body.password)
            }
        }

        deleteUser(location, token.token)

        readUser(location, token.token).let {
            assertEquals(HttpStatus.NOT_FOUND, it.statusCode)
        }
    }

    @Test
    fun test3CCD() {
        val authenticate = authenticate("")
        val token = extractToken(authenticate.body)
        val response = createUser(USER_1.toString(), token.token)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        val location = response.headers.location

        readUser(location, token.token).let {
            assertEquals(HttpStatus.OK, it.statusCode)
        }

        createUser(USER_1.toString(), token.token).let {
            assertEquals(HttpStatus.OK, it.statusCode)
        }

        deleteUser(location, token.token)

        readUser(location, token.token).let {
            assertEquals(HttpStatus.NOT_FOUND, it.statusCode)
        }
    }
}
