package de.cwrose.shoppinglist.ct

import de.cwrose.shoppinglist.Application
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import java.net.URI
import kotlin.test.assertEquals


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [Application::class])
class UserResourceCRUDTest : TestBase() {

    var location: URI? = null

    var token: String? = null

    @Before
    override fun setup() {
        super.setup()

        token = authenticate().let {
            assertEquals(HttpStatus.OK, it.statusCode)
            extractToken(it.body).auth_token
        }
        location = createUser(USER_1.toString(), token).let {
            assertEquals(HttpStatus.CREATED, it.statusCode)
            it.headers.location
        }
    }

    @Test
    fun testCreateUser() {
        // user is created on setup()
        assertEquals(HttpStatus.OK, readUser(location, token).statusCode)
    }

    @Test
    fun testReadUser() {
        readUser(location, token).let {
            assertEquals(HttpStatus.OK, it.statusCode)
            assertEquals("Max", it.body?.username)
            assertEquals(null, it.body?.password)
        }
    }

    @Test
    fun testUpdateUser() {
        Json {
            "username" to "Mini"
            "password" to "p4ssw0rd2"
            "shopping_list" to emptyList<Json>()
        }.let { user ->
            updateUser(location, user.toString(), token).let {
                assertEquals(HttpStatus.OK, it.statusCode)
                assertEquals("Mini", it.body?.username)
            }
        }
    }

    @Test
    fun testIgnoreCreateUserIfExists() {
        assertEquals(HttpStatus.OK, readUser(location, token).statusCode)

        assertEquals(HttpStatus.OK, createUser(USER_1.toString(), token).statusCode)
    }

    @Test
    fun testDeleteUser() {
        deleteUser(location, token)

        assertEquals(HttpStatus.NOT_FOUND, readUser(location, token).statusCode)
    }

    @After
    override fun destroy() {
        deleteUser(location, token)

        assertEquals(HttpStatus.NOT_FOUND, readUser(location, token).statusCode)
        super.destroy()
    }
}
