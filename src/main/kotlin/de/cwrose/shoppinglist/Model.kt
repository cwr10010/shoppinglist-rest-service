package de.cwrose.shoppinglist

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.*

@Entity @Table(name = "SHOPPING_LIST")
data class ShoppingListEntry(

        @Id var id: String = UUID.randomUUID().toString(),

        var name: String? = null,

        var description: String? = null,

        @Column(name = "entry_order")
        var order: Int? = null,

        var read: Boolean = false

)

@Entity @Table(name = "USER")
data class User (

        @JsonProperty("user_id")
        @Id var id: String = UUID.randomUUID().toString(),

        var name: String? = null,

        var password: String? = null,

        @JsonProperty("shopping_list")
        @OneToMany
        var shoppingList: Set<ShoppingListEntry> = emptySet()
)

@Repository
interface ShoppingListsRepository: JpaRepository<ShoppingListEntry, String>

@Repository
interface UserRepository: JpaRepository<User, String>
