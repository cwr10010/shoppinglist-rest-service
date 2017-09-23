package de.cwrose.shoppinglist.rest

import de.cwrose.shoppinglist.AuthorityName
import de.cwrose.shoppinglist.AuthorityRepository
import de.cwrose.shoppinglist.RefreshTokenRepository
import de.cwrose.shoppinglist.ShoppingList
import de.cwrose.shoppinglist.ShoppingListsRepository
import de.cwrose.shoppinglist.User
import de.cwrose.shoppinglist.UserRepository
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
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
            when (userRepository.findByUsername(user.username!!)) {
                null -> userService.createUser(user).let {
                    uriComponentsBuilder.path("/users/{id}").buildAndExpand(user.id).let {
                        ResponseEntity.created(it.toUri())
                    }
                }
                else -> ResponseEntity.ok()
            }.build()

    @GetMapping("{user_id}")
    fun entry(@PathVariable("user_id") user_id: String) = userRepository.getOne(user_id)

    @PostMapping("{user_id}")
    fun entry(@PathVariable("user_id") user_id: String, @RequestBody user: User) =
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
    fun delete(@PathVariable("user_id") user_id: String) =
            userRepository.findOne(user_id).let { user ->
                when (user) {
                    null -> logger.warn("Unknown user $user_id")
                    else -> refreshTokenRepository.findAllByUser(user).let { tokens ->
                        logger.info("Deleting ${user_id}")
                        refreshTokenRepository.delete(tokens)
                    }.let {
                        shoppingListsRepository.findByOwnersUserId(user_id).let {
                            shoppingListsRepository.delete(it)
                        }.let {
                            userRepository.delete(user)
                        }
                    }
                }
            }

    companion object : KLogging()
}

@Service
class UserService(
        val userRepository: UserRepository,
        val authorityRepository: AuthorityRepository,
        val shoppingListsRepository: ShoppingListsRepository,
        val passwordEncoder: PasswordEncoder) {

    fun createUser(user: User) =
            user.apply {
                passwordHash = when {
                    user.password != null -> passwordEncoder.encode(user.password)
                    else -> user.passwordHash
                }
                authorities = when {
                    user.authorities.isEmpty() -> setOf(defaultAuthority())
                    else -> user.authorities
                }
                active = true
            }.let {
                userRepository.save(user).let { user ->
                    logger.info("Added user ${user.id}")
                    shoppingListsRepository.save(
                            ShoppingList().apply {
                                name = "Shopping List"
                                ownersUserId = user.id
                                accessableForUserIds += user
                            }).let {
                        logger.info("Added default shoppinglist ${it.id}")
                    }
                    user
                }
            }!!

    fun defaultAuthority() = authorityRepository.findByName(AuthorityName.ROLE_USER)!!

    companion object : KLogging()

}