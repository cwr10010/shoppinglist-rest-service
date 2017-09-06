package de.cwrose.shoppinglist.rest

import de.cwrose.shoppinglist.ShoppingListItem
import de.cwrose.shoppinglist.ShoppingListsRepository
import de.cwrose.shoppinglist.UserRepository
import mu.KLogging
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users/{user_id}/shopping-list")
class ShoppingListResource(val shoppingLists: ShoppingListsRepository, val users: UserRepository) {

    @GetMapping
    fun index(
            @PathVariable("user_id") user_id: String,
            @RequestParam("term", required = false) term: String?): List<ShoppingListItem> {
        return users.findOne(user_id).shoppingList.let {
            logger.info("find all shopping list items for user $user_id")
            when (term) {
                null -> it
                else -> it.filter {
                    logger.info("filter shopping list for user $user_id with term $term")
                    it.name!!.contains(term, true)
                }
            } .sortedBy { it.order }
        }
    }

    @PostMapping
    fun index(@PathVariable("user_id") user_id: String, @RequestBody list: Set<ShoppingListItem>) =
        users.findOne(user_id).apply {
            list.forEach {
                it.userId = user_id
            }
            shoppingList += list
        } .let {
            shoppingLists.save(list)
            logger.info("Added List of ShoppingListItems ${list.map { it.id }} to User $user_id")
            users.save(it)
        } .shoppingList.sortedBy { it.order }

    @GetMapping("/{id}")
    fun entry(@PathVariable("user_id") user_id: String, @PathVariable("id") id: String) = shoppingListItem(user_id, id)

    @PostMapping("/{id}")
    fun entry(@PathVariable("user_id") user_id: String, @PathVariable("id") id: String, @RequestBody shoppingListItem: ShoppingListItem) =
        shoppingListItem(user_id, id).apply {
            name        = shoppingListItem.name
            description = shoppingListItem.description
            order       = shoppingListItem.order
            read        = shoppingListItem.read
        } .let {
            logger.info("Updated ShoppingListItem $id for User $user_id")
            shoppingLists.save(it)
        }

    private fun shoppingListItem(user_id: String, id: String) = users.getOne(user_id).shoppingList.single { item -> item.id == id }

    @DeleteMapping("/{id}")
    fun entryDelete(@PathVariable("user_id") user_id: String, @PathVariable("id") id: String) =
        users.getOne(user_id).let {
            user ->
            shoppingLists.getOne(id).let {
                logger.info("Deleting ShoppingListItem $id for User $user_id")
                user.shoppingList -= it
            }.let {
                users.save(user)
            }
        }.shoppingList.sortedBy { it.order }


    companion object: KLogging()

}