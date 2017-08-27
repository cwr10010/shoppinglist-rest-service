package de.cwrose.shoppinglist

import net.minidev.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URI

open class TestBase {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    fun extractId(location: URI): String {
        return location.toASCIIString().split("/").last()
    }

    fun standardHeaders(): HttpHeaders {
        return HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
    }

    fun createUser(jsonStr: String): URI {
        return HttpEntity<String>(jsonStr, standardHeaders()).let {
            restTemplate.postForLocation("/users", it, String::class.java)
        }
    }

    fun readUser(location: URI): ResponseEntity<UserVO> {
        return restTemplate.getForEntity(location, UserVO::class.java)
    }

    fun updateUser(location: URI, jsonStr: String): ResponseEntity<UserVO> {
        return HttpEntity<String>(jsonStr, standardHeaders()).let {
            restTemplate.postForEntity(location, it, UserVO::class.java)
        }
    }

    fun deleteUser(location: URI) {
        restTemplate.delete(location)
    }

    fun getShoppingList(location: URI) :ResponseEntity<String> {
        return restTemplate.getForEntity(location.toASCIIString() + "/shopping-list", String::class.java)
    }

    fun addShoppingListEntry(location: URI, jsonStr: String): ResponseEntity<String> {
        return HttpEntity<String>(jsonStr, standardHeaders()).let {
            restTemplate.postForEntity(location.toASCIIString() + "/shopping-list", it, String::class.java)
        }
    }

    fun getShoppingListEntry(location: URI, id: String): ResponseEntity<ShoppingListEntryVO> {
        return restTemplate.getForEntity(location.toASCIIString() + "/shopping-list/" + id, ShoppingListEntryVO::class.java)
    }

    fun updateShoppingListEntry(location: URI, id: String, jsonStr: String): ResponseEntity<ShoppingListEntryVO> {
        return HttpEntity<String>(jsonStr, standardHeaders()).let {
            restTemplate.postForEntity(location.toASCIIString() + "/shopping-list/" + id, it, ShoppingListEntryVO::class.java)
        }
    }

    fun deleteShoppingListEntry(location: URI, id: String) {
        restTemplate.delete(location.toASCIIString() + "/shopping-list/" + id)
    }

}

val USER_1 = Json {
    "name" To "Max"
    "password" To "p4ssw0rd"
}

val USER_2 = Json {
    "name" To "Mini"
    "password" To "p4ssw0rd2"
}

class Json() {

    val json = JSONObject()

    constructor(init: Json.() -> Unit) : this() {
        this.init()
    }

    infix fun <T> String.To(value: T) {
        json.put(this, value)
    }

    override fun toString(): String = json.toString()
}

data class ShoppingListEntryVO(var id: String? = null, var name: String? = null, var description: String? = null, var order: Int? = null)
data class UserVO(var name: String? = null, var password: String? = null, var shopping_list: List<ShoppingListEntryVO>? = emptyList())

