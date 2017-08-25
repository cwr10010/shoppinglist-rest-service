package de.cwrose.shoppinglist

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder


@RestController
@RequestMapping("/users")
class UserResource(val userRepository: UserRepository) {

    @GetMapping
    fun index(): List<User> {
        return userRepository.findAll()
    }

    @PostMapping
    fun index(@RequestBody user: User, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Void> {
        return userRepository.save(user).let {
            uriComponentsBuilder.path("/users/{id}").buildAndExpand(it.id)
        } .let {
            ResponseEntity.created(it.toUri())
        } .build()
    }

    @GetMapping("{user_id}")
    fun entry(@PathVariable("user_id") user_id: String): User {
        return userRepository.getOne(user_id)
    }

    @PostMapping("{user_id}")
    fun entry(@PathVariable("user_id") user_id: String, @RequestBody user: User): User {
        return userRepository.getOne(user_id).apply {
            name = user.name
            password = user.password
        } .let {
            userRepository.save(it)
        }
    }

    @DeleteMapping("{user_id}")
    fun delete(@PathVariable("user_id") user_id: String) {
        userRepository.delete(user_id)
    }

}