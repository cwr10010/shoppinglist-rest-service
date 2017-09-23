package de.cwrose.shoppinglist.rest

import de.cwrose.shoppinglist.RegistrationData
import de.cwrose.shoppinglist.RegistrationDataRepository
import de.cwrose.shoppinglist.User
import de.cwrose.shoppinglist.UserRepository
import de.cwrose.shoppinglist.auth.generateRegistrationToken
import de.cwrose.shoppinglist.auth.getRegistrationTokenUser
import freemarker.template.Configuration
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import javax.annotation.PostConstruct
import javax.mail.internet.InternetAddress


@RestController
@RequestMapping("/register")
class RegistrationResource(val registrationDataRepository: RegistrationDataRepository, val mailService: MailService, val passwordEncoder: PasswordEncoder, val userRepository: UserRepository) {

    @PostMapping
    fun register(@RequestBody registrationData: RegistrationData) =
            registrationData.apply {
                registrationToken = generateRegistrationToken(username!!)
                passwordHash = passwordEncoder.encode(password)
            } .let {
                registrationDataRepository.save(it).let {
                    mailService.sendMail(it.username!!, it.emailAddress!!, it.registrationToken!!)
                }
            }

    @GetMapping
    fun activate(@RequestParam("token", required = true) token: String, uriComponentsBuilder: UriComponentsBuilder) =
            token.let {
                getRegistrationTokenUser(it).let {
                    registrationDataRepository.findByUsername(it).takeIf {
                        it?.registrationToken == token
                    } .let { registrationData ->
                        userRepository.save(User().apply {
                            username = registrationData!!.username
                            passwordHash = registrationData.passwordHash
                            emailAddress = registrationData.emailAddress
                        })
                    }
                }
            }


    companion object : KLogging()
}

@Service
class MailService(val mailSender: JavaMailSender, val freemarkerConfig: Configuration) {

    @Value("\${mail.registration.subject}")
    private lateinit var subject: String

    @Value("\${mail.registration.sender.displayName}")
    private lateinit var senderDisplayName: String

    @Value("\${mail.registration.sender.emailAddress}")
    private lateinit var senderEmailAddress: String

    @Value("\${mail.registration.registrationLink}")
    private lateinit var registrationLink: String

    @PostConstruct
    fun init() {
        freemarkerConfig.setClassForTemplateLoading(this.javaClass, "/templates")
    }

    fun sendMail(userName: String, emailAddress: String, registrationToken: String) =

        mailSender.createMimeMessage().let { message ->
            MimeMessageHelper(message).apply {
                setFrom(InternetAddress(senderEmailAddress, senderDisplayName))
                setTo(emailAddress)
                setText(template(userName, registrationToken), true)
                setSubject(subject)
            } .let {
                mailSender.send(message)
            }
        }


    fun template(username: String, registrationToken: String): String =
        freemarkerConfig.getTemplate("mail-registration.ftl").let { template ->
            FreeMarkerTemplateUtils.processTemplateIntoString(
                    template,
                    mapOf("username" to username,
                            "registrationlink" to registrationLink,
                            "registrationToken" to registrationToken))
        }


    companion object : KLogging()
}
