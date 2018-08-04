package de.cwrose.shoppinglist.services

import de.cwrose.shoppinglist.SharedShoppingList
import freemarker.template.Configuration
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils
import javax.annotation.PostConstruct
import javax.mail.internet.InternetAddress

@Service
class MailService(val mailSender: JavaMailSender, val freemarkerConfig: Configuration) {

    @Value("\${mail.sender.displayName}")
    private lateinit var senderDisplayName: String

    @Value("\${mail.sender.emailAddress}")
    private lateinit var senderEmailAddress: String

    @Value("\${mail.registration.Subject}")
    private lateinit var registrationSubject: String

    @Value("\${mail.registration.Link}")
    private lateinit var registrationLink: String

    @Value("\${mail.shareList.Subject}")
    private lateinit var shareListSubject: String

    @Value("\${mail.shareList.Link}")
    private lateinit var shareListLink: String

    @PostConstruct
    fun init() {
        freemarkerConfig.setClassForTemplateLoading(this.javaClass, "/templates")
    }

    fun sendRegistrationMail(userName: String, emailAddress: String, registrationToken: String) =
            mailSender.createMimeMessage().let { message ->
                MimeMessageHelper(message).apply {
                    setFrom(InternetAddress(senderEmailAddress, senderDisplayName))
                    setTo(emailAddress)
                    setText(registrationTemplate(userName, registrationToken), true)
                    setSubject(registrationSubject)
                } .let {
                    mailSender.send(message)
                }
            }

    fun registrationTemplate(username: String, registrationToken: String): String =
            freemarkerConfig.getTemplate("mail-registration.ftl").let { template ->
                FreeMarkerTemplateUtils.processTemplateIntoString(
                    template,
                    mapOf("username" to username,
                            "registrationlink" to registrationLink,
                            "registrationToken" to registrationToken))
            }

    fun sendShareListMail(sharedList: SharedShoppingList, shareListToken: String) =
            mailSender.createMimeMessage().let { message ->
                MimeMessageHelper(message).apply {
                    setFrom(InternetAddress(senderEmailAddress, senderDisplayName))
                    setTo(sharedList.forUser!!.emailAddress!!)
                    setText(shareListTemplate(sharedList, shareListToken))
                    setSubject(shareListSubject)
                } .let {
                    mailSender.send(message)
                }
            }

    fun shareListTemplate(sharedList: SharedShoppingList, shareListToken: String): String =
            freemarkerConfig.getTemplate("mail-share-list.ftl").let { template ->
                FreeMarkerTemplateUtils.processTemplateIntoString(
                    template,
                    mapOf("shareList" to sharedList,
                            "shareListLink" to shareListLink,
                            "shareListToken" to shareListToken))
            }

    companion object : KLogging()
}