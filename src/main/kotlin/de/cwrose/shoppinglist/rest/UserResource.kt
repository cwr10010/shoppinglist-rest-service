package de.cwrose.shoppinglist.rest

import de.cwrose.shoppinglist.RefreshTokenRepository
import de.cwrose.shoppinglist.User
import de.cwrose.shoppinglist.UserRepository
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder


@RestController
@RequestMapping("/users")
class UserResource(val userRepository: UserRepository, val refreshTokenRepository: RefreshTokenRepository, val passwordEncoder: PasswordEncoder) {

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
                        userRepository.delete(user)
                    }
                }
            }

    companion object : KLogging()
}