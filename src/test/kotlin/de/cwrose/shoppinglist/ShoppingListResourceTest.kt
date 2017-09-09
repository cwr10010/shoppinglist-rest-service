package de.cwrose.shoppinglist

import com.github.salomonbrys.kotson.typeToken
import com.google.gson.Gson
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit4.SpringRunner
import java.net.URI
import kotlin.test.assertEquals

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = arrayOf(Application::class))
class ShoppingListResourceTest : TestBase() {

    var location: URI? = null

    var token: String? = null

    @Before
    override fun setup() {
        super.setup()

        location = authenticate().let {
            assertEquals(HttpStatus.OK, it.statusCode)
            extractToken(it.body).let {
                createUser(USER_1.toString(), it.auth_token).let {
                    assertEquals(HttpStatus.CREATED, it.statusCode)
                    it.headers.location
                }
            }
        }
        token = authenticate(USER_1).let {
            assertEquals(HttpStatus.OK, it.statusCode)
            extractToken(it.body).auth_token
        }
    }

    @After
    override fun destroy() {
        deleteUser(location, token)
        super.destroy()
    }

    @Test
    fun indexAddOne() {
        Json {
            "name" To "Cheese"
            "description" To "Tasty Cheese"
            "order" To 0
        }.let { entry1 ->
            addShoppingListEntry(location, "[$entry1]", token).let { sle: ResponseEntity<String> ->
                assertEquals(HttpStatus.OK, sle.statusCode)
                val gson = Gson()
                gson.fromJson<List<ShoppingListEntryVO>>(sle.body, typeToken<List<ShoppingListEntryVO>>()).let {
                    assertEquals(1, it.size)
                    assertEquals("Cheese", it[0].name)
                    assertEquals("Tasty Cheese", it[0].description)
                    assertEquals(0, it[0].order)
                    assertEquals(false, it[0].read)
                    assertEquals(extractId(location), it[0].user_id)
                }
            }
        }

        Json {
            "name" To "Milk"
            "description" To "Sweet Milk"
            "order" To 1
            "read" To true
        }.let { entry2 ->
            addShoppingListEntry(location, "[$entry2]", token).let { sle: ResponseEntity<String> ->
                assertEquals(HttpStatus.OK, sle.statusCode)
                val gson = Gson()
                gson.fromJson<List<ShoppingListEntryVO>>(sle.body, typeToken<List<ShoppingListEntryVO>>()).let {
                    assertEquals(2, it.size)
                    assertEquals("Cheese", it[0].name)
                    assertEquals("Tasty Cheese", it[0].description)
                    assertEquals(0, it[0].order)
                    assertEquals(false, it[0].read)

                    assertEquals(extractId(location), it[0].user_id)
                    assertEquals("Milk", it[1].name)
                    assertEquals("Sweet Milk", it[1].description)
                    assertEquals(1, it[1].order)
                    assertEquals(true, it[1].read)
                    assertEquals(extractId(location), it[1].user_id)
                }
            }
        }

    }

    @Test
    fun search() {
        val first = Json {
            "name" To "Cheese"
            "description" To "Tasty Cheese"
            "order" To 0
        }
        val second = Json {
            "name" To "Milk"
            "description" To "Sweet Milk"
            "order" To 1
            "read" To true
        }

        addShoppingListEntry(location, "[$first, $second]", token).let { sle: ResponseEntity<String> ->
            assertEquals(HttpStatus.OK, sle.statusCode)
        }

        searchShoppingList(location, "Milk", token).let { sle: ResponseEntity<String> ->
            assertEquals(HttpStatus.OK, sle.statusCode)
            val gson = Gson()
            gson.fromJson<List<ShoppingListEntryVO>>(sle.body, typeToken<List<ShoppingListEntryVO>>()).let {
                assertEquals(1, it.size)
                assertEquals(extractId(location), it[0].user_id)
                assertEquals("Milk", it[0].name)
                assertEquals("Sweet Milk", it[0].description)
                assertEquals(1, it[0].order)
                assertEquals(true, it[0].read)
            }
        }

    }

    @Test
    fun indexRead() {
        getShoppingList(location, token).let { sle: ResponseEntity<String> ->
            assertEquals(HttpStatus.OK, sle.statusCode)
            val gson = Gson()
            gson.fromJson<List<ShoppingListEntryVO>>(sle.body, typeToken<List<ShoppingListEntryVO>>()).let {
                assertEquals(0, it.size)
            }
        }

        Json {
            "name" To "Cheese"
            "description" To "Tasty Cheese"
            "order" To 0
            "read" To true
        }.let { entry1 ->
            addShoppingListEntry(location, "[$entry1]", token).let { sle: ResponseEntity<String> ->
                assertEquals(HttpStatus.OK, sle.statusCode)
                val gson = Gson()
                gson.fromJson<List<ShoppingListEntryVO>>(sle.body, typeToken<List<ShoppingListEntryVO>>()).let {
                    assertEquals(1, it.size)
                    assertEquals("Cheese", it[0].name)
                    assertEquals("Tasty Cheese", it[0].description)
                    assertEquals(0, it[0].order)
                    assertEquals(true, it[0].read)
                    assertEquals(extractId(location), it[0].user_id)
                }
            }
        }

        getShoppingList(location, token).let { sle: ResponseEntity<String> ->
            assertEquals(HttpStatus.OK, sle.statusCode)
            val gson = Gson()
            gson.fromJson<List<ShoppingListEntryVO>>(sle.body, typeToken<List<ShoppingListEntryVO>>()).let {
                assertEquals(1, it.size)
                assertEquals("Cheese", it[0].name)
                assertEquals("Tasty Cheese", it[0].description)
                assertEquals(0, it[0].order)
                assertEquals(true, it[0].read)
                assertEquals(extractId(location), it[0].user_id)
            }
        }
    }

    @Test
    fun entryRead() {
        Json {
            "name" To "Cheese"
            "description" To "Tasty Cheese"
            "order" To 0
            "read" To true
        }.let { entry1 ->
            addShoppingListEntry(location, "[$entry1]", token).let { addResponse: ResponseEntity<String> ->
                assertEquals(HttpStatus.OK, addResponse.statusCode)
                val gson = Gson()
                gson.fromJson<List<ShoppingListEntryVO>>(addResponse.body, typeToken<List<ShoppingListEntryVO>>()).let { gsonResult ->
                    assertEquals(1, gsonResult.size)
                    getShoppingListEntry(location, gsonResult[0].id!!, token).let { getResponse ->
                        assertEquals("Cheese", getResponse.body.name)
                        assertEquals("Tasty Cheese", getResponse.body.description)
                        assertEquals(0, getResponse.body.order)
                        assertEquals(true, getResponse.body.read)
                        assertEquals(extractId(location), getResponse.body.user_id)
                    }

                }
            }
        }
    }

    @Test
    fun entryUpdate() {
        Json {
            "name" To "Cheese"
            "description" To "Tasty Cheese"
            "order" To 0
            "read" To false
        }.let { entry1 ->
            addShoppingListEntry(location, "[$entry1]", token).let { sleResponse: ResponseEntity<String> ->
                assertEquals(HttpStatus.OK, sleResponse.statusCode)
                val gson = Gson()
                gson.fromJson<List<ShoppingListEntryVO>>(sleResponse.body, typeToken<List<ShoppingListEntryVO>>()).let {
                    assertEquals(1, it.size)
                    Json {
                        "name" To "Milk"
                        "description" To "Sweet Milk"
                        "order" To 1
                        "read" To true
                    }.let { entry2 ->
                        updateShoppingListEntry(location, it[0].id!!, entry2.toString(), token).let { sle: ResponseEntity<ShoppingListEntryVO> ->
                            assertEquals(HttpStatus.OK, sle.statusCode)
                            assertEquals(it[0].id, sle.body.id)
                            assertEquals("Milk", sle.body.name)
                            assertEquals("Sweet Milk", sle.body.description)
                            assertEquals(1, sle.body.order)
                            assertEquals(true, sle.body.read)
                            assertEquals(extractId(location), sle.body.user_id)

                        }
                    }
                }
            }
        }
    }

    @Test
    fun entryDelete() {
        Json {
            "name" To "Cheese"
            "description" To "Tasty Cheese"
            "order" To 0
        }.let { entry1 ->
            addShoppingListEntry(location, "[$entry1]", token).let { addResponse: ResponseEntity<String> ->
                assertEquals(HttpStatus.OK, addResponse.statusCode)
                val gson = Gson()
                gson.fromJson<List<ShoppingListEntryVO>>(addResponse.body, typeToken<List<ShoppingListEntryVO>>()).let { sleList ->
                    assertEquals(1, sleList.size)
                    deleteShoppingListEntry(location, sleList[0].id!!, token).let { deleteResult ->
                        assertEquals(HttpStatus.OK, deleteResult.statusCode)
                        getShoppingList(location, token).let { getResult: ResponseEntity<String> ->
                            assertEquals(HttpStatus.OK, getResult.statusCode)
                            val gson2 = Gson()
                            gson2.fromJson<List<ShoppingListEntryVO>>(getResult.body, typeToken<List<ShoppingListEntryVO>>()).let {
                                assertEquals(0, it.size)
                            }
                        }
                    }
                }
            }
        }
    }
}
