package de.cwrose.shoppinglist.rest

import de.cwrose.shoppinglist.ShoppingListEntry
import de.cwrose.shoppinglist.ShoppingListsRepository
import de.cwrose.shoppinglist.UserRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("users/{user_id}/shopping-list")
class ShoppingListResource(val shoppingLists: ShoppingListsRepository, val users: UserRepository) {

    @GetMapping
    fun index(@PathVariable("user_id") user_id: String): Set<ShoppingListEntry> {
        return users.findOne(user_id).shoppingList
    }

    @PostMapping
    fun index(@PathVariable("user_id") user_id: String, @RequestBody list: Set<ShoppingListEntry>): List<ShoppingListEntry> {
        return users.findOne(user_id).apply {
            shoppingList += list
        } .let {
            shoppingLists.save(list)
            users.save(it)
        } .shoppingList.sortedBy { it.order }
    }

    @GetMapping("/{id}")
    fun entry(@PathVariable("user_id") user_id: String, @PathVariable("id") id: String): ShoppingListEntry {
        return getShoppingListEntry(user_id, id)
    }

    @PostMapping("/{id}")
    fun entry(@PathVariable("user_id") user_id: String, @PathVariable("id") id: String, @RequestBody shoppingListEntry: ShoppingListEntry): ShoppingListEntry {
        return getShoppingListEntry(user_id, id).apply {
            name = shoppingListEntry.name
            description = shoppingListEntry.description
            order = shoppingListEntry.order
            read = shoppingListEntry.read
        } .let {
            shoppingLists.save(it)
        }
    }

    private fun getShoppingListEntry(user_id: String, id: String): ShoppingListEntry {
        return users.getOne(user_id).shoppingList.single { (entry_id) -> id == entry_id }
    }

    @DeleteMapping("/{id}")
    fun entryDelete(@PathVariable("user_id") user_id: String, @PathVariable("id") id: String) {
        shoppingLists.getOne(id).let {
            users.getOne(user_id).apply {
                shoppingList -= it
            } .let {
                users.save(it)
            }
        }
    }
}