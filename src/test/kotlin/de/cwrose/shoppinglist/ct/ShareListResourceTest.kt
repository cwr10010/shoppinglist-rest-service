package de.cwrose.shoppinglist.ct

import com.github.salomonbrys.kotson.typeToken
import com.google.gson.Gson
import de.cwrose.shoppinglist.Application
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit4.SpringRunner
import org.subethamail.wiser.Wiser
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = arrayOf(Application::class))
class ShareListResourceTest : TestBase() {

    var wiser: Wiser = Wiser()

    var location_1: URI? = null

    var token_1: String? = null

    var shoppingListIds_1: List<String> = emptyList()

    var location_2: URI? = null

    var token_2: String? = null

    var shoppingListIds_2: List<String> = emptyList()


    @Before
    override fun setup() {
        super.setup()
        wiser.setPort(2500)
        wiser.start()

        location_1 = authenticate().let {
            assertEquals(HttpStatus.OK, it.statusCode)
            extractToken(it.body).let {
                createUser(USER_1.toString(), it.auth_token).let {
                    assertEquals(HttpStatus.CREATED, it.statusCode)
                    it.headers.location
                }
            }
        }
        token_1 = authenticate(USER_1).let {
            assertEquals(HttpStatus.OK, it.statusCode)
            extractToken(it.body).auth_token
        }
        shoppingListIds_1 = usersShoppingList(location_1, token_1).let { sle: ResponseEntity<String> ->
            assertEquals(HttpStatus.OK, sle.statusCode)
            val gson = Gson()
            gson.fromJson<List<UsersShoppingListVO>>(sle.body, typeToken<List<UsersShoppingListVO>>()).let {
                assertEquals(1, it.size)
                it.filter { it.shopping_list_id != null } .map { it.shopping_list_id!! }
            }
        }

        location_2 = authenticate().let {
            assertEquals(HttpStatus.OK, it.statusCode)
            extractToken(it.body).let {
                createUser(USER_2.toString(), it.auth_token).let {
                    assertEquals(HttpStatus.CREATED, it.statusCode)
                    it.headers.location
                }
            }
        }
        token_2 = authenticate(USER_2).let {
            assertEquals(HttpStatus.OK, it.statusCode)
            extractToken(it.body).auth_token
        }
        shoppingListIds_2 = usersShoppingList(location_2, token_2).let { sle: ResponseEntity<String> ->
            assertEquals(HttpStatus.OK, sle.statusCode)
            val gson = Gson()
            gson.fromJson<List<UsersShoppingListVO>>(sle.body, typeToken<List<UsersShoppingListVO>>()).let {
                assertEquals(1, it.size)
                it.filter { it.shopping_list_id != null } .map { it.shopping_list_id!! }
            }
        }
    }

    @After
    override fun destroy() {
        wiser.stop()
    }

    @Test
    fun testShareList() {
        val shareRequest = Json {
            "user_id" To extractId(location_2)
            "shopping_list_id" To shoppingListIds_1.first()
        }

        shareShoppingList(shareRequest.toString(), token_1).let { shareResponse ->
            assertEquals(HttpStatus.OK, shareResponse.statusCode)
        }

        val token = extractTokenFromMail(wiser.messages.first().mimeMessage)

        acceptShoppingList(token_2!!, token).let {
            assertEquals(HttpStatus.OK, it.statusCode)
        }

        val shoppingListIds_3 = usersShoppingList(location_2, token_2).let { sle: ResponseEntity<String> ->
            assertEquals(HttpStatus.OK, sle.statusCode)
            val gson = Gson()
            gson.fromJson<List<UsersShoppingListVO>>(sle.body, typeToken<List<UsersShoppingListVO>>()).let {
                assertEquals(2, it.size)
                it.filter { it.shopping_list_id != null } .map { it.shopping_list_id!! }
            }
        }

        assertTrue { shoppingListIds_3.containsAll(shoppingListIds_1 + shoppingListIds_2) }
    }

    @Test
    fun testUnknownShoppingList() {
        val shareRequest = Json {
            "user_id" To extractId(location_2)
            "shopping_list_id" To "Unknown"
        }

        shareShoppingList(shareRequest.toString(), token_1).let { shareResponse ->
            assertEquals(HttpStatus.BAD_REQUEST, shareResponse.statusCode)
            assert(wiser.messages.isEmpty())
        }

    }

    @Test
    fun testUnknownReceiver() {
        val shareRequest = Json {
            "user_id" To "Unknown"
            "shopping_list_id" To shoppingListIds_1.first()
        }

        shareShoppingList(shareRequest.toString(), token_1).let { shareResponse ->
            assertEquals(HttpStatus.BAD_REQUEST, shareResponse.statusCode)
            assert(wiser.messages.isEmpty())
        }

    }

    @Test
    fun testBadShareToken() {

        acceptShoppingList(token_2!!, "").let {
            assertEquals(HttpStatus.NOT_FOUND, it.statusCode)
        }

    }

    @Test
    fun testWrongReceiver() {
        val shareRequest = Json {
            "user_id" To extractId(location_2)
            "shopping_list_id" To shoppingListIds_1.first()
        }

        shareShoppingList(shareRequest.toString(), token_1).let { shareResponse ->
            assertEquals(HttpStatus.OK, shareResponse.statusCode)
        }

        val token = extractTokenFromMail(wiser.messages.first().mimeMessage)

        acceptShoppingList(token_1!!, token).let {
            assertEquals(HttpStatus.BAD_REQUEST, it.statusCode)
        }

        val shoppingListIds_3 = usersShoppingList(location_1, token_1).let { sle: ResponseEntity<String> ->
            assertEquals(HttpStatus.OK, sle.statusCode)
            val gson = Gson()
            gson.fromJson<List<UsersShoppingListVO>>(sle.body, typeToken<List<UsersShoppingListVO>>()).let {
                assertEquals(1, it.size)
                it.filter { it.shopping_list_id != null } .map { it.shopping_list_id!! }
            }
        }

        assertTrue { shoppingListIds_3 == shoppingListIds_1 }
    }
}
