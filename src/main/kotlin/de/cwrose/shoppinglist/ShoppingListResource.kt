package de.cwrose.shoppinglist

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("users/{user_id}/shopping-list")
class ShoppingListResource(val shoppingLists: ShoppingListsRepository, val users: UserRepository) {

    @GetMapping
    fun index(@PathVariable("user_id") user_id: String): Set<ShoppingListEntry> {
        return users.findOne(user_id).shoppingList
    }

    @GetMapping("/{id}")
    fun entry(@PathVariable("user_id") user_id: String, @PathVariable("id") id: String): ShoppingListEntry {
        return users.getOne(user_id).shoppingList.single { (entry_id) -> id == entry_id }
    }

    @PostMapping("/{id}")
    fun entry(@PathVariable("user_id") user_id: String, @PathVariable("id") id: String, @RequestBody shoppingListEntry: ShoppingListEntry): ShoppingListEntry {
        return entry(user_id, id).apply {
            name = shoppingListEntry.name
            description = shoppingListEntry.description
            order = shoppingListEntry.order
        } .let {
            shoppingLists.save(it)
        }
    }

    @PostMapping
    fun index(@PathVariable("user_id") user_id: String, @RequestBody list: Set<ShoppingListEntry>): Set<ShoppingListEntry> {
        return users.findOne(user_id).apply {
            shoppingList += list
        } .let {
            shoppingLists.save(list)
            users.save(it)
        } .shoppingList
    }

}