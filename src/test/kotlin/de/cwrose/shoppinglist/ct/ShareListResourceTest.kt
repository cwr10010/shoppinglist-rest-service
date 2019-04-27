package de.cwrose.shoppinglist.ct

import com.github.salomonbrys.kotson.typeToken
import com.google.gson.Gson
import de.cwrose.shoppinglist.Application
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import org.subethamail.wiser.Wiser
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [Application::class])
class ShareListResourceTest : TestBase() {

    var wiser: Wiser = Wiser()

    var locationUser1: URI? = null

    var tokenUser1: String? = null

    var shoppingListIdsUser1: List<String> = emptyList()

    var locationUser2: URI? = null

    var tokenUser2: String? = null

    var shoppingListIdsUser2: List<String> = emptyList()


    @Before
    override fun setup() {
        super.setup()
        wiser.setPort(2500)
        wiser.start()

        val (locationUser1, tokenUser1, shoppingListIdsUser1) = createUserWithShoppingList(USER_1)
        this.locationUser1 = locationUser1
        this.tokenUser1 = tokenUser1
        this.shoppingListIdsUser1 = shoppingListIdsUser1
        val (locationUser2, tokenUser2, shoppingListIdsUser2) = createUserWithShoppingList(USER_2)
        this.locationUser2 = locationUser2
        this.tokenUser2 = tokenUser2
        this.shoppingListIdsUser2 = shoppingListIdsUser2
    }

    data class CreateUserResult(val location: URI, val token: String, val shoppingLists: List<String>)
    fun createUserWithShoppingList(user: Json): CreateUserResult {
        val location = authenticate().let { authenticateResponse ->
            assertEquals(HttpStatus.OK, authenticateResponse.statusCode)
            extractToken(authenticateResponse.body).let { adminToken ->
                createUser(user.toString(), adminToken.auth_token).let { createUserResponse ->
                    assertEquals(HttpStatus.CREATED, createUserResponse.statusCode)
                    createUserResponse.headers.location
                }
            }
        }
        val token = authenticate(user).let { authenticateResponse ->
            assertEquals(HttpStatus.OK, authenticateResponse.statusCode)
            extractToken(authenticateResponse.body).auth_token
        }
        val shoppingListIds = usersShoppingList(location, token).let { userShoppingListResponse ->
            assertEquals(HttpStatus.OK, userShoppingListResponse.statusCode)
            Gson().fromJson<List<UsersShoppingListVO>>(userShoppingListResponse.body, typeToken<List<UsersShoppingListVO>>()).let { userShoppingLists ->
                assertEquals(1, userShoppingLists.size)
                userShoppingLists.filter { it.shopping_list_id != null } .map { it.shopping_list_id!! }
            }
        }
        return CreateUserResult(location!!, token, shoppingListIds)
    }

    @After
    override fun destroy() {
        wiser.stop()
    }

    @Test
    fun testShareList() {
        val shareRequest = Json {
            "user_id" to extractId(locationUser2)
            "shopping_list_id" to shoppingListIdsUser1.first()
        }

        assertEquals(HttpStatus.OK, shareShoppingList(shareRequest.toString(), tokenUser1).statusCode)

        val token = extractTokenFromMail(wiser.messages.first().mimeMessage)

        assertEquals(HttpStatus.OK, acceptShoppingList(tokenUser2!!, token).statusCode)

        val shoppingListIds = usersShoppingList(locationUser2, tokenUser2).let { usersShoppingListResponse ->
            assertEquals(HttpStatus.OK, usersShoppingListResponse.statusCode)
            Gson().fromJson<List<UsersShoppingListVO>>(usersShoppingListResponse.body, typeToken<List<UsersShoppingListVO>>()).let { usersShoppingLists ->
                assertEquals(2, usersShoppingLists.size)
                usersShoppingLists.filter { it.shopping_list_id != null } .map { it.shopping_list_id!! }
            }
        }

        assertTrue { shoppingListIds.containsAll(shoppingListIdsUser1 + shoppingListIdsUser2) }
    }

    @Test
    fun testUnknownShoppingList() {
        val shareRequest = Json {
            "user_id" to extractId(locationUser2)
            "shopping_list_id" to "Unknown"
        }

        shareShoppingList(shareRequest.toString(), tokenUser1).let { shareResponse ->
            assertEquals(HttpStatus.BAD_REQUEST, shareResponse.statusCode)
            assert(wiser.messages.isEmpty())
        }

    }

    @Test
    fun testUnknownReceiver() {
        val shareRequest = Json {
            "user_id" to "Unknown"
            "shopping_list_id" to shoppingListIdsUser1.first()
        }

        shareShoppingList(shareRequest.toString(), tokenUser1).let { shareResponse ->
            assertEquals(HttpStatus.BAD_REQUEST, shareResponse.statusCode)
            assert(wiser.messages.isEmpty())
        }

    }

    @Test
    fun testBadShareToken() {

        assertEquals(HttpStatus.NOT_FOUND, acceptShoppingList(tokenUser2!!, "").statusCode)

    }

    @Test
    fun testWrongReceiver() {
        val shareRequest = Json {
            "user_id" to extractId(locationUser2)
            "shopping_list_id" to shoppingListIdsUser1.first()
        }

        assertEquals(HttpStatus.OK, shareShoppingList(shareRequest.toString(), tokenUser1).statusCode)

        val token = extractTokenFromMail(wiser.messages.first().mimeMessage)

        assertEquals(HttpStatus.BAD_REQUEST, acceptShoppingList(tokenUser1!!, token).statusCode)

        val shoppingListIds = usersShoppingList(locationUser1, tokenUser1).let { userShoppingListsResponse ->
            assertEquals(HttpStatus.OK, userShoppingListsResponse.statusCode)
            Gson().fromJson<List<UsersShoppingListVO>>(userShoppingListsResponse.body, typeToken<List<UsersShoppingListVO>>()).let { userShoppingLists ->
                assertEquals(1, userShoppingLists.size)
                userShoppingLists.filter { it.shopping_list_id != null } .map { it.shopping_list_id!! }
            }
        }

        assertTrue { shoppingListIds == shoppingListIdsUser1 }
    }
}
