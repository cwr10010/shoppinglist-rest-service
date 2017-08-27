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

        val location = createUser(USER_1.toString())

        Json {
            "name" To "Cheese"
            "description" To "Tasty Cheese"
            "order" To 0
        } .let {
            entry1 -> addShoppingListEntry(location, "[${entry1.toString()}]").let {
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
            entry2 -> addShoppingListEntry(location, "[${entry2.toString()}]").let {
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
    }

    @Test
    fun indexRead() {

        val location = createUser(USER_1.toString())

        getShoppingList(location).let {
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
            entry1 -> addShoppingListEntry(location, "[${entry1.toString()}]").let {
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

        getShoppingList(location).let {
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

    @Test
    fun entryRead() {

        val location = createUser(USER_1.toString())

        Json {
            "name" To "Cheese"
            "description" To "Tasty Cheese"
            "order" To 0
            "read" To true
        } .let {
            entry1 -> addShoppingListEntry(location, "[${entry1.toString()}]").let {
                sleResponse: ResponseEntity<String> ->
                assertEquals(HttpStatus.OK, sleResponse.statusCode)
                val gson = Gson()
                gson.fromJson<List<ShoppingListEntryVO>>(sleResponse.body, typeToken<List<ShoppingListEntryVO>>()).let {
                    assertEquals(1, it.size)
                    getShoppingListEntry(location, it[0].id!!).let {
                        assertEquals("Cheese", it.body.name)
                        assertEquals("Tasty Cheese", it.body.description)
                        assertEquals(0, it.body.order)
                        assertEquals(true, it.body.read)
                    }

                }
            }
        }
    }

    @Test
    fun entryUpdate() {

        val location = createUser(USER_1.toString())

        Json {
            "name" To "Cheese"
            "description" To "Tasty Cheese"
            "order" To 0
            "read" To false
        } .let {
            entry1 -> addShoppingListEntry(location, "[${entry1.toString()}]").let {
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
                        entry2 -> updateShoppingListEntry(location, it[0].id!!, entry2.toString()).let {
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
    }

    @Test
    fun entryDelete() {
        val location = createUser(USER_1.toString())

        Json {
            "name" To "Cheese"
            "description" To "Tasty Cheese"
            "order" To 0
        } .let { entry1 ->
            addShoppingListEntry(location, "[${entry1.toString()}]").let { sleResponse: ResponseEntity<String> ->
                assertEquals(HttpStatus.OK, sleResponse.statusCode)
                val gson = Gson()
                gson.fromJson<List<ShoppingListEntryVO>>(sleResponse.body, typeToken<List<ShoppingListEntryVO>>()).let {
                    assertEquals(1, it.size)
                    deleteShoppingListEntry(location, it[0].id!!).let {
                        getShoppingList(location).let {
                            sle: ResponseEntity<String> ->
                            assertEquals(HttpStatus.OK, sle.statusCode)
                            val gson2 = Gson()
                            gson2.fromJson<List<ShoppingListEntryVO>>(sle.body, typeToken<List<ShoppingListEntryVO>>()).let {
                                assertEquals(0, it.size)
                            }
                        }
                    }
                }
            }
        }
    }
}
