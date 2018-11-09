package de.cwrose.shoppinglist.rest

import de.cwrose.shoppinglist.RegistrationData
import de.cwrose.shoppinglist.RegistrationDataRepository
import de.cwrose.shoppinglist.User
import de.cwrose.shoppinglist.UserRepository
import de.cwrose.shoppinglist.services.JwtService
import de.cwrose.shoppinglist.services.MailService
import de.cwrose.shoppinglist.services.UserService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/register")
class RegistrationResource(
        val registrationDataRepository: RegistrationDataRepository,
        val mailService: MailService,
        val passwordEncoder: PasswordEncoder,
        val userRepository: UserRepository,
        val userService: UserService,
        val jwtService: JwtService) {

    @PostMapping
    fun register(@RequestBody registrationData: RegistrationData): ResponseEntity<Void> =
            registrationData.apply {
                username = username?.trim()
            } .let {
                userRepository.findByUsername(registrationData.username!!).map {
                    logger.info("User already exists. Ignore registration attempt")
                }.orElseGet {
                    registrationData.apply {
                        registrationToken = jwtService.generateRegistrationToken(username!!)
                        passwordHash = passwordEncoder.encode(password)
                    }.let { enrichedRegistrationData ->
                        registrationDataRepository.findByUsername(enrichedRegistrationData.username!!).map {
                            logger.info("Name already reserved. Don't save registration data.")
                        } .orElseGet {
                            registrationDataRepository.save(enrichedRegistrationData).let { updatedData ->
                                mailService.sendRegistrationMail(updatedData.username!!, updatedData.emailAddress!!, updatedData.registrationToken!!)
                            }
                        }
                    }

                }
            } .let {
                ResponseEntity.ok().build<Void>()
            }

    @GetMapping
    fun activate(@RequestParam("token", required = true) token: String): User =
            token.let {
                jwtService.getRegistrationTokenUser(it).let { registeredUserName ->
                    registrationDataRepository.findByUsername(registeredUserName).filter { registrationData ->
                        registrationData.registrationToken == token && ! userRepository.findByUsername(registrationData.username!!).isPresent
                    } .map { registrationData ->
                        userService.createUser(
                                User().apply {
                                    username = registrationData.username
                                    passwordHash = registrationData.passwordHash
                                    emailAddress = registrationData.emailAddress
                        })
                    } .orElseThrow {
                        UnknownTokenUserException("No registration known to token")
                    }
                }
            }

    companion object : KLogging()
}
