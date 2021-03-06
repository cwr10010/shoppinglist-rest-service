package de.cwrose.shoppinglist

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration

@SpringBootApplication(exclude = [ RepositoryRestMvcAutoConfiguration::class ])
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}