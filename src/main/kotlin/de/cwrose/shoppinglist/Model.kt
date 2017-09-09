package de.cwrose.shoppinglist

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Date
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass
import javax.persistence.OneToMany
import javax.persistence.PrePersist
import javax.persistence.PreUpdate
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity
@Table(name = "SHOPPING_LIST")
data class ShoppingListItem(

        var name: String? = null,

        var description: String? = null,

        @Column(name = "item_order")
        var order: Int? = null,

        @Column(name = "item_read")
        @Type(type = "yes_no")
        var read: Boolean = false,

        @JsonProperty("user_id")
        @Column(name = "user_id")
        var userId: String? = null
) : EntityBase()

@Entity
@Table(name = "USER")
data class User(

        var username: String? = null,

        @Transient
        var password: String? = null,

        @JsonIgnore
        @Column(name = "password")
        var passwordHash: String? = null,

        @JsonProperty("shopping_list")
        @OneToMany(fetch = FetchType.EAGER)
        @JoinColumn(name = "user_id")
        var shoppingList: Set<ShoppingListItem> = emptySet()
) : EntityBase()

@Entity
@Table(name = "REFRESH_TOKEN")
data class RefreshToken(

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "user_id")
        var user: User? = null,

        @Temporal(TemporalType.TIMESTAMP)
        var expires: Date? = null,

        @Type(type = "yes_no")
        var valid: Boolean? = true
) : EntityBase()

@MappedSuperclass
abstract class EntityBase(

        @JsonProperty("id")
        @Id
        var id: String? = null,

        @Temporal(TemporalType.TIMESTAMP)
        var created: Date? = null,

        @Temporal(TemporalType.TIMESTAMP)
        var modified: Date? = null
) {
    @PrePersist
    fun prePersist() {
        id = UUID.randomUUID().toString()
        created = Date()
    }

    @PreUpdate
    fun preUpdate() {
        modified = Date()
    }
}

@Repository
interface ShoppingListsRepository : JpaRepository<ShoppingListItem, String>

@Repository
interface UserRepository : JpaRepository<User, String> {

    fun findByUsername(username: String): User?
}

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, String> {

    fun findAllByUser(user: User): List<RefreshToken>
}