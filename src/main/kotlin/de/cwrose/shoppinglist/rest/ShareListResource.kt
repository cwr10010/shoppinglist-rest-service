package de.cwrose.shoppinglist.rest

import de.cwrose.shoppinglist.*
import de.cwrose.shoppinglist.auth.JwtUser
import de.cwrose.shoppinglist.services.JwtService
import de.cwrose.shoppinglist.services.MailService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException


@RestController
@RequestMapping("/share")
class ShareListResource(
        val userRepository: UserRepository,
        val shoppingListsRepository: ShoppingListsRepository,
        val sharedShoppingListRepository: SharedShoppingListRepository,
        val jwtService: JwtService,
        val mailService: MailService) {


    @PostMapping
    fun sendInvitation(@RequestBody shareInvitation: ShareInvitation) : ResponseEntity<Unit> =
            (SecurityContextHolder.getContext().authentication.principal as JwtUser).let { currentPrincipal ->
                userRepository.findById(currentPrincipal.id).let { currentUser ->
                    currentUser.map { fromUser ->
                        userRepository.findById(shareInvitation.userId).let { foundForUser ->
                            foundForUser.map { forUser ->
                                shoppingListsRepository.findByOwnersUserIdAndId(fromUser.id!!, shareInvitation.shoppingListId).let { foundSharedShoppingList ->
                                    foundSharedShoppingList.map { sharedShoppingList ->
                                        sharedShoppingListRepository.save(SharedShoppingList(fromUser, forUser, sharedShoppingList)).let { sharedList ->
                                            jwtService.generateShareToken(sharedList.id!!).let { token ->
                                                mailService.sendShareListMail(sharedList, token)
                                            }
                                        }
                                    }.orElseThrow {
                                        UnknownShoppingListException("Unknow ShoppinList [${shareInvitation.shoppingListId}] for sharing request.")
                                    }
                                }
                            } .orElseThrow {
                                UnknownReceiverException("Unknown user [${shareInvitation.userId}] to receive invitation.")
                            }
                        }
                    }
                } .let {
                    ResponseEntity.ok().build()
                }
            }


    @GetMapping
    fun acceptInvitation(@RequestParam("token", required = true) token: String): Unit =
            jwtService.getSharedListId(token).let { sharedListId ->
                sharedShoppingListRepository.findById(sharedListId).let { sharedList ->
                    sharedList.map { thisSharedList ->
                        (SecurityContextHolder.getContext().authentication.principal as JwtUser).let { currentUser ->
                            userRepository.findById(currentUser.id).let { forUser ->
                                forUser.filter {
                                    it == thisSharedList.forUser
                                } .map { foundForUser ->
                                    thisSharedList.sharedList!!.apply {
                                        accessableForUser += foundForUser
                                    } .let { updatedList ->
                                        shoppingListsRepository.save(updatedList)
                                        sharedShoppingListRepository.delete(thisSharedList)
                                    }
                                } .orElseThrow {
                                    BadReceiverException("SharedListRequest was not ment for user ${currentUser.id}")
                                }
                            }
                        }
                    } .orElseThrow {
                        UnknownSharedListException("Requested shared list with id $sharedListId does not exist.")
                    }
                }
            }

}

class UnknownReceiverException(override val message: String): RuntimeException(message)

class BadReceiverException(override val message: String): RuntimeException(message)

class UnknownSharedListException(override val message: String): RuntimeException(message)

