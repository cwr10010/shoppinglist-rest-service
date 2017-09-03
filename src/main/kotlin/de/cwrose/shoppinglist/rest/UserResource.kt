package de.cwrose.shoppinglist.rest

import de.cwrose.shoppinglist.User
import de.cwrose.shoppinglist.UserRepository
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder


@RestController
@RequestMapping("/users")
class UserResource(val userRepository: UserRepository, val passwordEncoder: PasswordEncoder) {

    @PostMapping
    fun index(@RequestBody user: User, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Void> =
        when (userRepository.findByUsername(user.username!!)) {
            null -> user.apply {
                passwordHash = passwordEncoder.encode(user.password)
            }.let {
                userRepository.save(user).let {
                    logger.info("Added user ${it.id}")
                    uriComponentsBuilder.path("/users/{id}").buildAndExpand(it.id)
                }.let {
                    ResponseEntity.created(it.toUri())
                }
            }.build()
            else -> ResponseEntity.ok().build()
        }

    @GetMapping("{user_id}")
    fun entry(@PathVariable("user_id") user_id: String) = userRepository.getOne(user_id)

    @PostMapping("{user_id}")
    fun entry(@PathVariable("user_id") user_id: String, @RequestBody user: User): User =
        userRepository.getOne(user_id).apply {
            username = user.username
            passwordHash = passwordEncoder.encode(user.password)
        } .let {
            logger.info("Updating user ${it.id}")
            userRepository.save(it)
        }

    @DeleteMapping("{user_id}")
    fun delete(@PathVariable("user_id") user_id: String) {
        logger.info("Deleting ${user_id}")
        userRepository.delete(user_id)
    }

    companion object: KLogging()
}