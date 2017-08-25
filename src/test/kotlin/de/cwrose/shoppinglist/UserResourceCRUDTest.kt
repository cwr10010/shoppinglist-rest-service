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
    fun testCR() {
        val location = createUser(USER_1.toString())

        readUser(location).let {
            assertEquals(HttpStatus.OK, it.statusCode)
            assertEquals("""{"user_id":"${extractId(location)}","name":"Max","password":"p4ssw0rd","shopping_list":[]}""", it.body)
        }
    }

    @Test
    fun testCU() {
        val location = createUser(USER_1.toString())

        Json {
            "user_id" To "${extractId(location)}"
            "name" To "Mini"
            "password" To "p4ssw0rd2"
            "shopping_list" To emptyList<Json>()
        }.let {
            user -> updateUser(location, user.toString()).let {
                assertEquals(HttpStatus.OK, it.statusCode)
                assertEquals("""{"user_id":"${extractId(location)}","name":"Mini","password":"p4ssw0rd2","shopping_list":[]}""", it.body)
            }
        }
    }

    @Test
    fun testCD() {
        val location = createUser(USER_1.toString())

        readUser(location).let {
            assertEquals(HttpStatus.OK, it.statusCode)
        }

        deleteUser(location)

        readUser(location).let {
            assertEquals(HttpStatus.NOT_FOUND, it.statusCode)
        }
    }
}
