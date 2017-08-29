package de.cwrose.shoppinglist

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.annotations.Type
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.*

@Entity @Table(name = "SHOPPING_LIST")
data class ShoppingListItem(

        var name: String? = null,

        var description: String? = null,

        @Column(name = "item_order")
        var order: Int? = null,

        @Column(name = "item_read")
        @Type(type="yes_no")
        var read: Boolean = false
): EntityBase()

@Entity @Table(name = "USER")
data class User (

        var username: String? = null,

        var password: String? = null,

        @JsonProperty("shopping_list")
        @OneToMany(fetch = FetchType.EAGER)
        @JoinColumn(name="user_id")
        var shoppingList: Set<ShoppingListItem> = emptySet()
): EntityBase()

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class EntityBase (

        @JsonProperty("user_id")
        @Id
        var id: String = UUID.randomUUID().toString(),

        @JsonIgnore
        @CreatedDate
        @Temporal(TemporalType.DATE)
        var created: Date = Date(),

        @JsonIgnore
        @LastModifiedDate
        @Temporal(TemporalType.DATE)
        var modified: Date = Date()
)

@Repository
interface ShoppingListsRepository: JpaRepository<ShoppingListItem, String>

@Repository
interface UserRepository: JpaRepository<User, String> {

    fun findByUsername(username: String): User?

    fun deleteByUsername(username: String)
}
