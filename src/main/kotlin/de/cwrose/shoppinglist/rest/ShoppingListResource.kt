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
    fun index(@PathVariable("user_id") user_id: String) = users.findOne(user_id).shoppingList

    @PostMapping
    fun index(@PathVariable("user_id") user_id: String, @RequestBody list: Set<ShoppingListItem>) =
        users.findOne(user_id).apply {
            list.forEach {
                it.userId = user_id
            }
            shoppingList += list
        } .let {
            logger.info("Added ${list.map { it.id }} to user ${user_id}")
            shoppingLists.save(list)
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
            logger.info("Updated ${it} for user ${user_id}")
            shoppingLists.save(it)
        }

    private fun shoppingListItem(user_id: String, id: String) = users.getOne(user_id).shoppingList.single { item -> item.id == id }

    @DeleteMapping("/{id}")
    fun entryDelete(@PathVariable("user_id") user_id: String, @PathVariable("id") id: String) =
        users.getOne(user_id).let {
            user ->
            shoppingLists.getOne(id).let {
                user.shoppingList -= it
            }.let {
                logger.info("Deleting ${it} for user ${user_id}")
                users.save(user)
            }
        }.shoppingList.sortedBy { it.order }


    companion object: KLogging()

}