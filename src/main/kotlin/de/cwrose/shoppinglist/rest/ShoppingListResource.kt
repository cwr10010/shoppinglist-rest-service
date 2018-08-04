package de.cwrose.shoppinglist.rest

import de.cwrose.shoppinglist.ShoppingListItem
import de.cwrose.shoppinglist.ShoppingListItemsRepository
import de.cwrose.shoppinglist.ShoppingListsRepository
import de.cwrose.shoppinglist.UserRepository
import de.cwrose.shoppinglist.UserShoppingList
import mu.KLogging
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException

@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@RestController
@RequestMapping("/users/{user_id}/shopping-list")
class ShoppingListResource(
        val userRepository: UserRepository,
        val shoppingLists: ShoppingListsRepository,
        val shoppingListItems: ShoppingListItemsRepository) {

    @GetMapping
    fun list(@PathVariable("user_id") user_id: String) =
            shoppingLists.findShoppingListsAuthorizedForUser(user_id).let { authorizedShoppingLists ->
                userRepository.findById(user_id).let { foundUser ->
                    foundUser.map { user ->
                        authorizedShoppingLists.map {
                            UserShoppingList(it.id, it.name, user.id, user.username)
                        }
                    }
                }
            }.orElseThrow {
                UnknownShoppingListException("No shopping lists found for user")
            }

    @GetMapping("{shopping-list_id}/entries")
    fun entries(
            @PathVariable("user_id") user_id: String,
            @PathVariable("shopping-list_id") shoppingListId: String,
            @RequestParam("term", required = false) term: String?) =
            findShoppinglistForUser(user_id, shoppingListId).shoppingListItems.let {
                logger.info("Find all shopping list items for user $user_id")
                when (term) {
                    null -> it
                    else -> {
                        logger.info("Filter shopping list for user $user_id with term $term")
                        it.filter {
                            it.name!!.contains(term, true)
                        }
                    }
                }
            }.sortedBy { it.order }

    @PostMapping("{shopping-list_id}/entries")
    fun entries(
            @PathVariable("user_id") user_id: String,
            @PathVariable("shopping-list_id") shoppingListId: String,
            @RequestBody list: Set<ShoppingListItem>) =
            findShoppinglistForUser(user_id, shoppingListId).let { shoppingList ->
                logger.debug("Shoppinglist: $shoppingList with id ${shoppingList.id}")
                list.onEach { item ->
                    item.userId = user_id
                    item.shoppingList = shoppingList
                    shoppingListItems.save(item)
                } .let {
                    logger.debug("Items to be added: $it")
                    shoppingList.shoppingListItems += it
                    logger.info("Added List of ShoppingListItems ${it.map { it.id }} to User $user_id")
                    shoppingLists.save(shoppingList)
                }.shoppingListItems.sortedBy { it.order }
            }

    @GetMapping("{shopping-list_id}/entries/{id}")
    fun entry(
            @PathVariable("user_id") user_id: String,
            @PathVariable("shopping-list_id") shoppingListId: String,
            @PathVariable("id") id: String) = shoppingListItem(user_id, shoppingListId, id)

    @PostMapping("{shopping-list_id}/entries/{id}")
    fun entry(
            @PathVariable("user_id") user_id: String,
            @PathVariable("shopping-list_id") shoppingListId: String,
            @PathVariable("id") id: String,
            @RequestBody shoppingListItem: ShoppingListItem) =
            shoppingListItem(user_id, shoppingListId, id).apply {
                name = shoppingListItem.name
                description = shoppingListItem.description
                order = shoppingListItem.order
                checked = shoppingListItem.checked
            }.let {
                logger.info("Updated ShoppingListItem $id for User $user_id")
                shoppingListItems.save(it)
            }

    @DeleteMapping("{shopping-list_id}/entries/{id}")
    fun entryDelete(
            @PathVariable("user_id") user_id: String,
            @PathVariable("shopping-list_id") shoppingListId: String,
            @PathVariable("id") id: String) =
            findShoppinglistForUser(user_id, shoppingListId).let { shoppingList ->
                shoppingListItems.getOne(id).let {
                    logger.info("Deleting ShoppingListItem $id for User $user_id")
                    shoppingList.shoppingListItems -= it
                    shoppingLists.save(shoppingList)
                    shoppingListItems.delete(it)
                }
                shoppingList
            }.shoppingListItems.sortedBy { it.order }

    private fun shoppingListItem(user_id: String, shoppingListId: String, id: String) =
            findShoppinglistForUser(user_id, shoppingListId).let { shoppingList ->
                shoppingList.shoppingListItems.single { it.id == id }
            }

    private fun findShoppinglistForUser(user_id: String, shoppingListId: String) =
            shoppingLists.findShoppingListsAuthorizedForUser(user_id).single {
                it.id == shoppingListId
            }

    companion object : KLogging()
}

class UnknownShoppingListException(override val message: String): RuntimeException(message)
