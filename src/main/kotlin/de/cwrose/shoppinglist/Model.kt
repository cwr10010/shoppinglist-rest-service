package de.cwrose.shoppinglist

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Date
import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass
import javax.persistence.OneToMany
import javax.persistence.PrePersist
import javax.persistence.PreUpdate
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType


data class JwtAuthenticationResponse(
        @JsonProperty("auth_token")
        val authToken: String? = null,

        @JsonProperty("id_token")
        val idToken: String? = null,

        val expires: Number? = null
)

data class JwtAuthenticationRequest(val username: String, val password: String)

data class UserShoppingList(
        @JsonProperty("shopping_list_id")
        var shoppinglistId: String?,
        @JsonProperty("shopping_list_name")
        var shoppinglistName: String?,
        @JsonProperty("owners_id")
        var ownersId: String?,
        @JsonProperty("owners_name")
        var ownersName: String?
)

@Entity
@Table(name = "SHOPPING_LIST_ITEM")
data class ShoppingListItem(

        var name: String? = null,

        var description: String? = null,

        @Column(name = "item_order")
        var order: Int? = null,

        @Column(name = "item_checked")
        @Type(type = "yes_no")
        var checked: Boolean = false,

        @JsonProperty("user_id")
        @Column(name = "user_id")
        var userId: String? = null,

        @JsonIgnore
        @ManyToOne(optional = false)
        var shoppingList: ShoppingList? = null
) : EntityBase()

@Entity
@Table(name = "SHOPPING_LIST")
class ShoppingList(

        var name: String? = null,

        @JsonProperty("owners_user_id")
        @Column(name = "owners_user_id")
        var ownersUserId: String? = null,

        @JsonProperty("accessable_for")
        @OneToMany(fetch = FetchType.LAZY)
        @JoinTable(
                name = "ACCESSABLE_FOR_USER_IDS",
                joinColumns = arrayOf(JoinColumn(name = "SHOPPINGLIST_ID", referencedColumnName = "ID")),
                inverseJoinColumns = arrayOf(JoinColumn(name = "USER_ID", referencedColumnName = "ID")))
        var accessableForUserIds: Set<User> = emptySet(),

        @JsonProperty("shopping_list_items")
        @OneToMany(fetch = FetchType.EAGER, mappedBy = "shoppingList", cascade = arrayOf(CascadeType.ALL))
        var shoppingListItems: Set<ShoppingListItem> = emptySet()
) : EntityBase()

enum class AuthorityName {
    ROLE_USER, ROLE_ADMIN
}

@Entity
@Table(name = "AUTHORITY")
data class Authority(

        @Column(name = "NAME", length = 50)
        @Enumerated(EnumType.STRING)
        var name: AuthorityName? = null

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

        @JsonProperty("email_address")
        var emailAddress: String? = null,

        @Type(type = "yes_no")
        var active: Boolean = true,

        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(
                name = "USER_AUTHORITY",
                joinColumns = arrayOf(JoinColumn(name = "USER_ID", referencedColumnName = "ID")),
                inverseJoinColumns = arrayOf(JoinColumn(name = "AUTHORITY_ID", referencedColumnName = "ID")))
        var authorities: Set<Authority> = emptySet()

) : EntityBase()

@Entity
@Table(name = "REGISTRATION_DATA")
data class RegistrationData(

        var username: String? = null,

        @Transient
        var password: String? = null,

        @JsonIgnore
        @Column(name = "password")
        var passwordHash: String? = null,

        @JsonProperty("email_address")
        var emailAddress: String? = null,

        @JsonIgnore
        @Column(name = "token")
        var registrationToken: String? = null

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
interface ShoppingListItemsRepository : JpaRepository<ShoppingListItem, String>

@Repository
interface ShoppingListsRepository : JpaRepository<ShoppingList, String> {

    fun findByOwnersUserId(ownersUserId: String): ShoppingList

    @Query("SELECT sl.* FROM user u, accessable_for_user_ids au, shopping_list sl WHERE u.id=:userId AND au.user_id=u.id AND sl.id=au.shoppinglist_id", nativeQuery = true)
    fun findShoppingListsAuthorizedForUser(@Param("userId") userId: String): List<ShoppingList>
}

@Repository
interface UserRepository : JpaRepository<User, String> {

    fun findByUsername(username: String): User?
}

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, String> {

    fun findAllByUser(user: User): List<RefreshToken>
}

@Repository
interface RegistrationDataRepository : JpaRepository<RegistrationData, String> {

    fun findByUsername(username: String) : RegistrationData?
}

@Repository
interface AuthorityRepository : JpaRepository<Authority, String> {

    fun findByName(name: AuthorityName): Authority?
}