package de.cwrose.shoppinglist.ct

import de.cwrose.shoppinglist.Application
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import org.subethamail.wiser.Wiser
import javax.mail.internet.MimeMessage
import kotlin.test.assertEquals


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = arrayOf(Application::class))
class RegistrationResourceTest : TestBase() {

    var wiser: Wiser = Wiser()


    @Before
    override fun setup() {
        super.setup()
        wiser.setPort(2500)
        wiser.start()
    }

    @After
    override fun destroy() {
        wiser.stop()
    }

    @Test
    fun testSendRegistrationLink() {
        val user = USER_1.json
        HttpEntity<String>(user.toString(), standardHeaders("")).let {
            restTemplate.postForEntity("/register", it, String::class.java)
        } .let {
            assertEquals(HttpStatus.OK, it.statusCode)
        }
        val token = extractTokenFromMail(wiser.messages.first().mimeMessage)


        HttpEntity<String>(user.toString(), standardHeaders("")).let {
            restTemplate.getForEntity("/register?token=${token}", UserVO::class.java)
        }.let {
            assertEquals(HttpStatus.OK, it.statusCode)
            assertEquals("Max", it.body.username)
        }

    }
}

internal fun extractTokenFromMail(message: MimeMessage) =
    message.content.let {
        it as String
    } .let { it.replace("\r\n", "") }
