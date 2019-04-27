package de.cwrose.shoppinglist.ct

import com.github.salomonbrys.kotson.typeToken
import com.google.gson.Gson
import de.cwrose.shoppinglist.Application
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit4.SpringRunner
import java.net.URI
import kotlin.test.assertEquals

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [Application::class])
class ShoppingListResourceTest : TestBase() {

    var location: URI? = null

    var token: String? = null

    var shoppingListId: String? = null

    @Before
    override fun setup() {
        super.setup()

        location = authenticate().let { authenticateResponse ->
            assertEquals(HttpStatus.OK, authenticateResponse.statusCode)
            extractToken(authenticateResponse.body).let { adminToken ->
                createUser(USER_1.toString(), adminToken.auth_token).let { createUserResponse ->
                    assertEquals(HttpStatus.CREATED, createUserResponse.statusCode)
                    createUserResponse.headers.location
                }
            }
        }
        token = authenticate(USER_1).let { authenticateResponse ->
            assertEquals(HttpStatus.OK, authenticateResponse.statusCode)
            extractToken(authenticateResponse.body).auth_token
        }
        shoppingListId = usersShoppingList(location, token).let { shoppingListEntryResponse ->
            assertEquals(HttpStatus.OK, shoppingListEntryResponse.statusCode)
            Gson().fromJson<List<UsersShoppingListVO>>(shoppingListEntryResponse.body, typeToken<List<UsersShoppingListVO>>()).let { userShoppingLists ->
                assertEquals(1, userShoppingLists.size)
                userShoppingLists.first().shopping_list_id
            }
        }
    }

    @After
    override fun destroy() {
        deleteUser(location, extractToken(authenticate().body).auth_token).let {
            super.destroy()
        }
    }

    @Test
    fun indexAddOne() {
        Json {
            "name" to "Cheese"
            "description" to "Tasty Cheese"
            "order" to 0
        }.let { entry1 ->
            addShoppingListEntry(location, shoppingListId, "[$entry1]", token).let { shoppingListEntriesResponse ->
                assertEquals(HttpStatus.OK, shoppingListEntriesResponse.statusCode)
                Gson().fromJson<List<ShoppingListEntryVO>>(shoppingListEntriesResponse.body, typeToken<List<ShoppingListEntryVO>>()).let { shoppingListEntries ->
                    assertEquals(1, shoppingListEntries.size)
                    assertEquals("Cheese", shoppingListEntries[0].name)
                    assertEquals("Tasty Cheese", shoppingListEntries[0].description)
                    assertEquals(0, shoppingListEntries[0].order)
                    assertEquals(false, shoppingListEntries[0].checked)
                    assertEquals(extractId(location), shoppingListEntries[0].user_id)
                }
            }
        }

        Json {
            "name" to "Milk"
            "description" to "Sweet Milk"
            "order" to 1
            "checked" to true
        }.let { entry2 ->
            addShoppingListEntry(location, shoppingListId,"[$entry2]", token).let { shoppingListEntriesResponse ->
                assertEquals(HttpStatus.OK, shoppingListEntriesResponse.statusCode)
                Gson().fromJson<List<ShoppingListEntryVO>>(shoppingListEntriesResponse.body, typeToken<List<ShoppingListEntryVO>>()).let { shoppingListEntries ->
                    assertEquals(2, shoppingListEntries.size)
                    assertEquals("Cheese", shoppingListEntries[0].name)
                    assertEquals("Tasty Cheese", shoppingListEntries[0].description)
                    assertEquals(0, shoppingListEntries[0].order)
                    assertEquals(false, shoppingListEntries[0].checked)

                    assertEquals(extractId(location), shoppingListEntries[0].user_id)
                    assertEquals("Milk", shoppingListEntries[1].name)
                    assertEquals("Sweet Milk", shoppingListEntries[1].description)
                    assertEquals(1, shoppingListEntries[1].order)
                    assertEquals(true, shoppingListEntries[1].checked)
                    assertEquals(extractId(location), shoppingListEntries[1].user_id)
                }
            }
        }

    }

    @Test
    fun search() {
        val first = Json {
            "name" to "Cheese"
            "description" to "Tasty Cheese"
            "order" to 0
        }
        val second = Json {
            "name" to "Milk"
            "description" to "Sweet Milk"
            "order" to 1
            "checked" to true
        }

        assertEquals(HttpStatus.OK, addShoppingListEntry(location, shoppingListId, "[$first, $second]", token).statusCode)

        searchShoppingList(location, shoppingListId, "Milk", token).let { searchResponse ->
            assertEquals(HttpStatus.OK, searchResponse.statusCode)
            Gson().fromJson<List<ShoppingListEntryVO>>(searchResponse.body, typeToken<List<ShoppingListEntryVO>>()).let { shoppingListEntries ->
                assertEquals(1, shoppingListEntries.size)
                assertEquals(extractId(location), shoppingListEntries[0].user_id)
                assertEquals("Milk", shoppingListEntries[0].name)
                assertEquals("Sweet Milk", shoppingListEntries[0].description)
                assertEquals(1, shoppingListEntries[0].order)
                assertEquals(true, shoppingListEntries[0].checked)
            }
        }

    }

    @Test
    fun indexRead() {
        getShoppingList(location, shoppingListId, token).let { userShoppingListsResponse ->
            assertEquals(HttpStatus.OK, userShoppingListsResponse.statusCode)
            assertEquals(0, Gson().fromJson<List<ShoppingListEntryVO>>(userShoppingListsResponse.body, typeToken<List<ShoppingListEntryVO>>()).size)
        }

        Json {
            "name" to "Cheese"
            "description" to "Tasty Cheese"
            "order" to 0
            "checked" to true
        }.let { entry1 ->
            addShoppingListEntry(location, shoppingListId, "[$entry1]", token).let { shoppingListEntriesResponse ->
                assertEquals(HttpStatus.OK, shoppingListEntriesResponse.statusCode)
                Gson().fromJson<List<ShoppingListEntryVO>>(shoppingListEntriesResponse.body, typeToken<List<ShoppingListEntryVO>>()).let { shoppingListEntries ->
                    assertEquals(1, shoppingListEntries.size)
                    assertEquals("Cheese", shoppingListEntries[0].name)
                    assertEquals("Tasty Cheese", shoppingListEntries[0].description)
                    assertEquals(0, shoppingListEntries[0].order)
                    assertEquals(true, shoppingListEntries[0].checked)
                    assertEquals(extractId(location), shoppingListEntries[0].user_id)
                }
            }
        }

        getShoppingList(location, shoppingListId, token).let { sle: ResponseEntity<String> ->
            assertEquals(HttpStatus.OK, sle.statusCode)
            val gson = Gson()
            gson.fromJson<List<ShoppingListEntryVO>>(sle.body, typeToken<List<ShoppingListEntryVO>>()).let {
                assertEquals(1, it.size)
                assertEquals("Cheese", it[0].name)
                assertEquals("Tasty Cheese", it[0].description)
                assertEquals(0, it[0].order)
                assertEquals(true, it[0].checked)
                assertEquals(extractId(location), it[0].user_id)
            }
        }
    }

    @Test
    fun entryRead() {
        Json {
            "name" to "Cheese"
            "description" to "Tasty Cheese"
            "order" to 0
            "checked" to true
        }.let { entry1 ->
            addShoppingListEntry(location, shoppingListId, "[$entry1]", token).let { addShoppingListResponse ->
                assertEquals(HttpStatus.OK, addShoppingListResponse.statusCode)
                Gson().fromJson<List<ShoppingListEntryVO>>(addShoppingListResponse.body, typeToken<List<ShoppingListEntryVO>>()).let { shoppingListEntries ->
                    assertEquals(1, shoppingListEntries.size)
                    getShoppingListEntry(location, shoppingListId, shoppingListEntries[0].id!!, token).let { getShoppingListEntryResponse ->
                        assertEquals("Cheese", getShoppingListEntryResponse.body?.name)
                        assertEquals("Tasty Cheese", getShoppingListEntryResponse.body?.description)
                        assertEquals(0, getShoppingListEntryResponse.body?.order)
                        assertEquals(true, getShoppingListEntryResponse.body?.checked)
                        assertEquals(extractId(location), getShoppingListEntryResponse.body?.user_id)
                    }

                }
            }
        }
    }

    @Test
    fun entryUpdate() {
        Json {
            "name" to "Cheese"
            "description" to "Tasty Cheese"
            "order" to 0
            "checked" to false
        }.let { entry1 ->
            addShoppingListEntry(location, shoppingListId, "[$entry1]", token).let { addShoppingListEntryResponse ->
                assertEquals(HttpStatus.OK, addShoppingListEntryResponse.statusCode)
                Gson().fromJson<List<ShoppingListEntryVO>>(addShoppingListEntryResponse.body, typeToken<List<ShoppingListEntryVO>>()).let { oldShoppingListEntries ->
                    assertEquals(1, oldShoppingListEntries.size)
                    Json {
                        "name" to "Milk"
                        "description" to "Sweet Milk"
                        "order" to 1
                        "checked" to true
                    }.let { valuesToUpdateTo ->
                        updateShoppingListEntry(location, shoppingListId, oldShoppingListEntries[0].id!!, valuesToUpdateTo.toString(), token).let { newShoppingListEntry ->
                            assertEquals(HttpStatus.OK, newShoppingListEntry.statusCode)
                            assertEquals(oldShoppingListEntries[0].id, newShoppingListEntry.body?.id)
                            assertEquals("Milk", newShoppingListEntry.body?.name)
                            assertEquals("Sweet Milk", newShoppingListEntry.body?.description)
                            assertEquals(1, newShoppingListEntry.body?.order)
                            assertEquals(true, newShoppingListEntry.body?.checked)
                            assertEquals(extractId(location), newShoppingListEntry.body?.user_id)

                        }
                    }
                }
            }
        }
    }

    @Test
    fun entryDelete() {
        Json {
            "name" to "Cheese"
            "description" to "Tasty Cheese"
            "order" to 0
        }.let { entry1 ->
            addShoppingListEntry(location, shoppingListId, "[$entry1]", token).let { addShoppingListEntryResponse ->
                assertEquals(HttpStatus.OK, addShoppingListEntryResponse.statusCode)
                val gson = Gson()
                gson.fromJson<List<ShoppingListEntryVO>>(addShoppingListEntryResponse.body, typeToken<List<ShoppingListEntryVO>>()).let { oldShoppingListEntries ->
                    assertEquals(1, oldShoppingListEntries.size)
                    deleteShoppingListEntry(location, shoppingListId, oldShoppingListEntries[0].id!!, token).let { deleteShoppingListEntryResponse ->
                        assertEquals(HttpStatus.OK, deleteShoppingListEntryResponse.statusCode)
                        getShoppingList(location, shoppingListId, token).let { shoppingListEntriesResponse ->
                            assertEquals(HttpStatus.OK, shoppingListEntriesResponse.statusCode)
                            assertEquals(0, Gson().fromJson<List<ShoppingListEntryVO>>(shoppingListEntriesResponse.body, typeToken<List<ShoppingListEntryVO>>()).size)
                        }
                    }
                }
            }
        }
    }
}

