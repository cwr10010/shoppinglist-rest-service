package de.cwrose.shoppinglist

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingListResourceTest: TestBase() {

    @Test
    fun index() {

        val location = createUser(USER_1.toString())

        Json {
            "name" To "Cheese"
            "description" To "Tasty Cheese"
            "order" To 0
        } .let {
            entry1 -> addShoppingListEntry(location, "[${entry1.toString()}]").let {
                assertEquals(HttpStatus.OK, it.statusCode)
                assert(it.body.contains(""""name":"Cheese""""))
            }
        }

        Json {
            "name" To "Milk"
            "description" To "Sweet Milk"
            "order" To 1
        } .let {
            entry2 -> addShoppingListEntry(location, "[${entry2.toString()}]").let {
                assertEquals(HttpStatus.OK, it.statusCode)
                assert(it.body.contains(""""name":"Cheese"""") && it.body.contains(""""name":"Milk""""))
            }
        }

    }

}