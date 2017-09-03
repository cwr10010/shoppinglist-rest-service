package de.cwrose.shoppinglist

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(Application::class))
class ApplicationTest {

    @Test
    fun test() {
        main(
            arrayOf("--spring.main.web-environment=false")
        )
    }
}