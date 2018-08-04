package de.cwrose.shoppinglist.services

import de.cwrose.shoppinglist.AuthorityName
import de.cwrose.shoppinglist.AuthorityRepository
import de.cwrose.shoppinglist.ShoppingList
import de.cwrose.shoppinglist.ShoppingListsRepository
import de.cwrose.shoppinglist.User
import de.cwrose.shoppinglist.UserRepository
import mu.KLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

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
                userRepository.save(it).let { savedUser ->
                    logger.info("Added user ${savedUser.id}")
                    shoppingListsRepository.save(
                            ShoppingList().apply {
                                name = "Shopping List"
                                ownersUserId = savedUser.id
                                accessableForUser += savedUser
                            }).let {
                        logger.info("Added default shoppinglist ${savedUser.id}")
                    }
                    savedUser
                }
            }

    fun defaultAuthority() = authorityRepository.findByName(AuthorityName.ROLE_USER).orElseThrow {
        IllegalStateException("ROLE_USER is unknown. System setup completed?")
    }

    companion object : KLogging()

}