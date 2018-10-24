package de.cwrose.shoppinglist.rest

import de.cwrose.shoppinglist.RefreshTokenRepository
import de.cwrose.shoppinglist.ShoppingListsRepository
import de.cwrose.shoppinglist.User
import de.cwrose.shoppinglist.UserRepository
import de.cwrose.shoppinglist.services.UserService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/users")
class UserResource(
        val userRepository: UserRepository,
        val userService: UserService,
        val shoppingListsRepository: ShoppingListsRepository,
        val refreshTokenRepository: RefreshTokenRepository,
        val passwordEncoder: PasswordEncoder) {

    @PostMapping
    fun index(@RequestBody user: User, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Void> =
            userRepository.findByUsername(user.username!!).map {
                ResponseEntity.ok()
            }.orElseGet {
                userService.createUser(user).let {
                    uriComponentsBuilder.path("/users/{id}").buildAndExpand(user.id).let {uriComponents ->
                        ResponseEntity.created(uriComponents.toUri())
                    }
                }
            }.build()

    @GetMapping("{user_id}")
    fun entry(@PathVariable("user_id") user_id: String):User = userRepository.getOne(user_id)

    @PostMapping("{user_id}")
    fun entry(@PathVariable("user_id") user_id: String, @RequestBody user: User):User =
            userRepository.getOne(user_id).apply {
                username = user.username
                passwordHash = passwordEncoder.encode(user.password)
                emailAddress = user.emailAddress
                active = user.active
            }.let {
                logger.info("Updating user ${it.id}")
                userRepository.save(it)
            }

    @DeleteMapping("{user_id}")
    fun delete(@PathVariable("user_id") user_id: String): Unit =
            userRepository.findById(user_id).map { user ->
                refreshTokenRepository.findAllByUser(user).let { tokens ->
                    logger.info("Deleting $user_id")
                    refreshTokenRepository.deleteAll(tokens)
                } .let {
                    shoppingListsRepository.findByOwnersUserId(user_id).map { shoppingListToDelete ->
                        shoppingListsRepository.delete(shoppingListToDelete)
                    }
                } .let {
                    userRepository.delete(user)
                }
            } .orElseGet {
                logger.warn("Unknown user $user_id")
            }

    companion object : KLogging()
}

