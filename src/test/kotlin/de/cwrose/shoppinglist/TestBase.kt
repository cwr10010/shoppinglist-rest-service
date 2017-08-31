package de.cwrose.shoppinglist

import com.google.gson.Gson
import net.minidev.json.JSONObject
import org.junit.After
import org.junit.Before
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.net.URI
import javax.annotation.PostConstruct

open class TestBase {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder


    @Autowired
    lateinit var transactionManager: PlatformTransactionManager

    lateinit var transactionTemplate: TransactionTemplate

    @PostConstruct
    fun setupClass() {
        transactionTemplate = TransactionTemplate(transactionManager)
    }

    @Before
    open fun setup() {
        userRepository.save(User(username=ADMIN.json["username"] as String, passwordHash=passwordEncoder.encode(ADMIN.json["password"] as String)))
    }

    @After
    open fun destroy() {
        transactionTemplate.execute {
            userRepository.deleteByUsername(username=ADMIN.json["username"] as String)
        }
    }

    fun authenticate(token: String, user: Json = ADMIN): ResponseEntity<String> {
        return HttpEntity<String>(user.toString(), standardHeaders(token)).let {
            restTemplate.postForEntity("/auth", it, String::class.java)
        }
    }

    fun refresh(token: String?): ResponseEntity<String> {
        return HttpEntity<String>(standardHeaders(token)).let {
            restTemplate.exchange("/auth", HttpMethod.GET, it, String::class.java)
        }
    }

    fun extractId(location: URI?): String? {
        return location?.toASCIIString()?.split("/")?.last()
    }

    fun extractToken(token: String?): String {
        val gson = Gson()
        return gson.fromJson(token, TokenVO::class.java).token
    }

    data class TokenVO(val token: String)

    fun standardHeaders(token: String?): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            add("Authorization", "Bearer $token")
        }
    }

    fun createUser(jsonStr: String, token: String?): ResponseEntity<String> {
        return HttpEntity<String>(jsonStr, standardHeaders(token)).let {
            restTemplate.postForEntity("/users", it, String::class.java)
        }
    }

    fun readUser(location: URI?, token: String?): ResponseEntity<UserVO> {
        return HttpEntity<Void>(standardHeaders(token)).let {
            restTemplate.exchange(location, HttpMethod.GET, it, UserVO::class.java)
        }
    }

    fun updateUser(location: URI?, jsonStr: String, token: String?): ResponseEntity<UserVO> {
        return HttpEntity<String>(jsonStr, standardHeaders(token)).let {
            restTemplate.postForEntity(location, it, UserVO::class.java)
        }
    }

    fun deleteUser(location: URI?, token: String?): ResponseEntity<Void> {
        return HttpEntity<Void>(standardHeaders(token)).let {
            restTemplate.exchange(location, HttpMethod.DELETE, it, Void::class.java)
        }
    }

    fun getShoppingList(location: URI?, token: String?) :ResponseEntity<String> {
        return HttpEntity<String>(standardHeaders(token)).let {
            restTemplate.exchange(location?.toASCIIString() + "/shopping-list", HttpMethod.GET, it, String::class.java)
        }
    }

    fun addShoppingListEntry(location: URI?, jsonStr: String, token: String?): ResponseEntity<String> {
        return HttpEntity<String>(jsonStr, standardHeaders(token)).let {
            restTemplate.postForEntity(location?.toASCIIString() + "/shopping-list", it, String::class.java)
        }
    }

    fun getShoppingListEntry(location: URI?, id: String, token: String?): ResponseEntity<ShoppingListEntryVO> {
        return HttpEntity<ShoppingListEntryVO>(standardHeaders(token)).let {
            restTemplate.exchange(location?.toASCIIString() + "/shopping-list/" + id, HttpMethod.GET, it, ShoppingListEntryVO::class.java)
        }
    }

    fun updateShoppingListEntry(location: URI?, id: String, jsonStr: String, token: String?): ResponseEntity<ShoppingListEntryVO> {
        return HttpEntity<String>(jsonStr, standardHeaders(token)).let {
            restTemplate.postForEntity(location?.toASCIIString() + "/shopping-list/" + id, it, ShoppingListEntryVO::class.java)
        }
    }

    fun deleteShoppingListEntry(location: URI?, id: String, token: String?): ResponseEntity<Void> {
        return HttpEntity<String>(standardHeaders(token)).let {
            restTemplate.exchange(location?.toASCIIString() + "/shopping-list/" + id, HttpMethod.DELETE, it, Void::class.java)
        }
    }
}

val USER_1 = Json {
    "username" To "Max"
    "password" To "p4ssw0rd"
}

val USER_2 = Json {
    "username" To "Mini"
    "password" To "p4ssw0rd2"
}

val ADMIN = Json {
    "username" To "Admin"
    "password" To "passwd"
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

data class ShoppingListEntryVO(var id: String? = null, var name: String? = null, var description: String? = null, var order: Int? = null, var read: Boolean? = null)
data class UserVO(var username: String? = null, var password: String? = null, var shopping_list: List<ShoppingListEntryVO>? = emptyList())
