package de.cwrose.shoppinglist.ct

import de.cwrose.shoppinglist.Application
import de.cwrose.shoppinglist.main
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = arrayOf(Application::class))
class ApplicationTest {

    @Test
    fun test() {
        main(
                arrayOf()
        )
    }
}