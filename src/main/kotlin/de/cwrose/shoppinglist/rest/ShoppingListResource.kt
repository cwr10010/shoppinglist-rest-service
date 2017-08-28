package de.cwrose.shoppinglist.rest

import de.cwrose.shoppinglist.ShoppingListItem
import de.cwrose.shoppinglist.ShoppingListsRepository
import de.cwrose.shoppinglist.UserRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("users/{user_id}/shopping-list")
class ShoppingListResource(val shoppingLists: ShoppingListsRepository, val users: UserRepository) {

    @GetMapping
    fun index(@PathVariable("user_id") user_id: String) = users.findOne(user_id).shoppingList

    @PostMapping
    fun index(@PathVariable("user_id") user_id: String, @RequestBody list: Set<ShoppingListItem>) =
        users.findOne(user_id).apply {
            shoppingList += list
        } .let {
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
            shoppingLists.save(it)
        }


    private fun shoppingListItem(user_id: String, id: String) = users.getOne(user_id)
            .shoppingList.single { (item_id) -> id == item_id }


    @DeleteMapping("/{id}")
    fun entryDelete(@PathVariable("user_id") user_id: String, @PathVariable("id") id: String) =
        shoppingLists.getOne(id).let {
            users.getOne(user_id).apply {
                shoppingList -= it
            } .let {
                users.save(it)
            }
        }

}