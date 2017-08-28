package de.cwrose.shoppinglist

import com.github.salomonbrys.kotson.typeToken
import com.google.gson.Gson
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingListResourceTest: TestBase() {

    @Test
    fun indexAddOne() {

        val authenticate = authenticate("")
        val token = extractToken(authenticate.body)
        val response = createUser(USER_1.toString(), token.token)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        val location = response.headers.location

        Json {
            "name" To "Cheese"
            "description" To "Tasty Cheese"
            "order" To 0
        } .let {
            entry1 -> addShoppingListEntry(location, "[${entry1.toString()}]", token.token).let {
                sle: ResponseEntity<String> ->
                assertEquals(HttpStatus.OK, sle.statusCode)
                val gson = Gson()
                gson.fromJson<List<ShoppingListEntryVO>>(sle.body, typeToken<List<ShoppingListEntryVO>>()).let {
                    assertEquals(1, it.size)
                    assertEquals("Cheese", it[0].name)
                    assertEquals("Tasty Cheese", it[0].description)
                    assertEquals(0, it[0].order)
                    assertEquals(false, it[0].read)
                }
            }
        }

        Json {
            "name" To "Milk"
            "description" To "Sweet Milk"
            "order" To 1
            "read" To true
        } .let {
            entry2 -> addShoppingListEntry(location, "[${entry2.toString()}]", token.token).let {
                sle: ResponseEntity<String> ->
                assertEquals(HttpStatus.OK, sle.statusCode)
                val gson = Gson()
                gson.fromJson<List<ShoppingListEntryVO>>(sle.body, typeToken<List<ShoppingListEntryVO>>()).let {
                    assertEquals(2, it.size)
                    assertEquals("Cheese", it[0].name)
                    assertEquals("Tasty Cheese", it[0].description)
                    assertEquals(0, it[0].order)
                    assertEquals(false, it[0].read)
                    assertEquals("Milk", it[1].name)
                    assertEquals("Sweet Milk", it[1].description)
                    assertEquals(1, it[1].order)
                    assertEquals(true, it[1].read)
                }
            }
        }

        deleteUser(location, token.token)
    }

    @Test
    fun indexRead() {

        val authenticate = authenticate("")
        val token = extractToken(authenticate.body)
        val response = createUser(USER_1.toString(), token.token)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        val location = response.headers.location

        getShoppingList(location, token.token).let {
            sle: ResponseEntity<String> ->
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
        } .let {
            entry1 -> addShoppingListEntry(location, "[${entry1.toString()}]", token.token).let {
                sle: ResponseEntity<String> ->
                assertEquals(HttpStatus.OK, sle.statusCode)
                val gson = Gson()
                gson.fromJson<List<ShoppingListEntryVO>>(sle.body, typeToken<List<ShoppingListEntryVO>>()).let {
                    assertEquals(1, it.size)
                    assertEquals("Cheese", it[0].name)
                    assertEquals("Tasty Cheese", it[0].description)
                    assertEquals(0, it[0].order)
                    assertEquals(true, it[0].read)
                }
            }
        }

        getShoppingList(location, token.token).let {
            sle: ResponseEntity<String> ->
            assertEquals(HttpStatus.OK, sle.statusCode)
            val gson = Gson()
            gson.fromJson<List<ShoppingListEntryVO>>(sle.body, typeToken<List<ShoppingListEntryVO>>()).let {
                assertEquals(1, it.size)
                assertEquals("Cheese", it[0].name)
                assertEquals("Tasty Cheese", it[0].description)
                assertEquals(0, it[0].order)
                assertEquals(true, it[0].read)
            }
        }

        deleteUser(location, token.token)
    }

    @Test
    fun entryRead() {

        val authenticate = authenticate("")
        val token = extractToken(authenticate.body)
        val response = createUser(USER_1.toString(), token.token)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        val location = response.headers.location

        Json {
            "name" To "Cheese"
            "description" To "Tasty Cheese"
            "order" To 0
            "read" To true
        } .let {
            entry1 -> addShoppingListEntry(location, "[${entry1.toString()}]", token.token).let {
                sleResponse: ResponseEntity<String> ->
                assertEquals(HttpStatus.OK, sleResponse.statusCode)
                val gson = Gson()
                gson.fromJson<List<ShoppingListEntryVO>>(sleResponse.body, typeToken<List<ShoppingListEntryVO>>()).let {
                    assertEquals(1, it.size)
                    getShoppingListEntry(location, it[0].id!!, token.token).let {
                        assertEquals("Cheese", it.body.name)
                        assertEquals("Tasty Cheese", it.body.description)
                        assertEquals(0, it.body.order)
                        assertEquals(true, it.body.read)
                    }

                }
            }
        }

        deleteUser(location, token.token)
    }

    @Test
    fun entryUpdate() {

        val authenticate = authenticate("")
        val token = extractToken(authenticate.body)
        val response = createUser(USER_1.toString(), token.token)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        val location = response.headers.location

        Json {
            "name" To "Cheese"
            "description" To "Tasty Cheese"
            "order" To 0
            "read" To false
        } .let {
            entry1 -> addShoppingListEntry(location, "[${entry1.toString()}]", token.token).let {
                sleResponse: ResponseEntity<String> ->
                assertEquals(HttpStatus.OK, sleResponse.statusCode)
                val gson = Gson()
                gson.fromJson<List<ShoppingListEntryVO>>(sleResponse.body, typeToken<List<ShoppingListEntryVO>>()).let {
                    assertEquals(1, it.size)
                    Json {
                        "name" To "Milk"
                        "description" To "Sweet Milk"
                        "order" To 1
                        "read" To true
                    } .let {
                        entry2 -> updateShoppingListEntry(location, it[0].id!!, entry2.toString(), token.token).let {
                            sle: ResponseEntity<ShoppingListEntryVO> ->
                            assertEquals(HttpStatus.OK, sle.statusCode)
                            assertEquals(it[0].id, sle.body.id)
                            assertEquals("Milk", sle.body.name)
                            assertEquals("Sweet Milk", sle.body.description)
                            assertEquals(1, sle.body.order)
                            assertEquals(true, sle.body.read)
                        }
                    }
                }
            }
        }

        deleteUser(location, token.token)
    }

    @Test
    fun entryDelete() {
        val authenticate = authenticate("")
        val token = extractToken(authenticate.body)
        val response = createUser(USER_1.toString(), token.token)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        val location = response.headers.location

        Json {
            "name" To "Cheese"
            "description" To "Tasty Cheese"
            "order" To 0
        } .let { entry1 ->
            addShoppingListEntry(location, "[${entry1.toString()}]", token.token).let {
                addResponse: ResponseEntity<String> ->
                assertEquals(HttpStatus.OK, addResponse.statusCode)
                val gson = Gson()
                gson.fromJson<List<ShoppingListEntryVO>>(addResponse.body, typeToken<List<ShoppingListEntryVO>>()).let {
                    sleList ->
                    assertEquals(1, sleList.size)
                    deleteShoppingListEntry(location, sleList[0].id!!, token.token).let {
                        deleteResult -> assertEquals(HttpStatus.OK, deleteResult.statusCode)
                        getShoppingList(location, token.token).let {
                            getResult: ResponseEntity<String> ->
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

        deleteUser(location, token.token)
    }
}
