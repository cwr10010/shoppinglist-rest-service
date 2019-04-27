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
import kotlin.test.assertEquals


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [Application::class])
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
        HttpEntity(user.toString(), standardHeaders("")).let { registrationRequest ->
            restTemplate.postForEntity("/register", registrationRequest, String::class.java)
        } .let { registrationResponse ->
            assertEquals(HttpStatus.OK, registrationResponse.statusCode)
        }
        val token = extractTokenFromMail(wiser.messages.first().mimeMessage)


        HttpEntity(user.toString(), standardHeaders("")).let {
            restTemplate.getForEntity("/register?token=$token", UserVO::class.java)
        }.let { commitRegistrationResponse ->
            assertEquals(HttpStatus.OK, commitRegistrationResponse.statusCode)
            assertEquals("Max", commitRegistrationResponse.body?.username)
        }
    }
}
