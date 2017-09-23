package de.cwrose.shoppinglist.rest

import de.cwrose.shoppinglist.ShoppingList
import de.cwrose.shoppinglist.ShoppingListItem
import de.cwrose.shoppinglist.ShoppingListItemsRepository
import de.cwrose.shoppinglist.ShoppingListsRepository
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

@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@RestController
@RequestMapping("/users/{user_id}/shopping-list")
class ShoppingListResource(val shoppingLists: ShoppingListsRepository, val shoppingListItems: ShoppingListItemsRepository) {

    @GetMapping
    fun index(@PathVariable("user_id") user_id: String, @RequestParam("term", required = false) term: String?) =
            findShoppinglistsOfUser(user_id).let {
                logger.info("Find all shopping list items for user $user_id")
                when (term) {
                    null -> it.flatMap { shoppingList ->  shoppingList.shoppingListItems }
                    else -> it.flatMap {
                        logger.info("Filter shopping list for user $user_id with term $term")
                        it.shoppingListItems.filter {
                            it.name!!.contains(term, true)
                        }
                    }
                }
            }.sortedBy { it.order }

    @PostMapping
    fun index(@PathVariable("user_id") user_id: String, @RequestBody list: Set<ShoppingListItem>) =
            findShoppinglistsOfUser(user_id).let {
                it.single().let { shoppingList ->
                    logger.debug("Shoppinglist: ${shoppingList} with id ${shoppingList.id}")
                    list.onEach { item ->
                        item.userId = user_id
                        item.shoppingList = shoppingList
                    } .let {
                        logger.debug("Items to be added: ${it}")
                        shoppingList.shoppingListItems += it
                        logger.info("Added List of ShoppingListItems ${it.map { it.id }} to User $user_id")
                        shoppingLists.save(shoppingList)
                    }
                }.shoppingListItems.sortedBy { it.order }
            }

    @GetMapping("/{id}")
    fun entry(@PathVariable("user_id") user_id: String, @PathVariable("id") id: String) =
            shoppingListItem(user_id, id)

    @PostMapping("/{id}")
    fun entry(@PathVariable("user_id") user_id: String, @PathVariable("id") id: String, @RequestBody shoppingListItem: ShoppingListItem) =
            shoppingListItem(user_id, id).apply {
                name = shoppingListItem.name
                description = shoppingListItem.description
                order = shoppingListItem.order
                checked = shoppingListItem.checked
            }.let {
                logger.info("Updated ShoppingListItem $id for User $user_id")
                shoppingListItems.save(it)
            }

    @DeleteMapping("/{id}")
    fun entryDelete(@PathVariable("user_id") user_id: String, @PathVariable("id") id: String) =
            findShoppinglistsOfUser(user_id).let { usersShoppingLists ->
                usersShoppingLists.single().let { shoppingList ->
                    shoppingListItems.getOne(id).let {
                        logger.info("Deleting ShoppingListItem $id for User $user_id")
                        shoppingList.shoppingListItems -= it
                        shoppingLists.save(shoppingList)
                        shoppingListItems.delete(it)
                    }
                    shoppingList
                }
            }.shoppingListItems.sortedBy { it.order }

    private fun shoppingListItem(user_id: String, id: String) =
            findShoppinglistsOfUser(user_id).let { shoppingList ->
                shoppingList.single().let {
                    it.shoppingListItems.single { it.id == id }
                }
            }

    private fun findShoppinglistsOfUser(user_id: String): List<ShoppingList> =
        shoppingLists.findAll().filter {
            it.accessableForUserIds.any { it.id == user_id }
        }

    companion object : KLogging()
}