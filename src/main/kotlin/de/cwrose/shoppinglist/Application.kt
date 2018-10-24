package de.cwrose.shoppinglist

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration
import springfox.documentation.swagger2.annotations.EnableSwagger2

@SpringBootApplication(exclude = [ RepositoryRestMvcAutoConfiguration::class ])
@EnableSwagger2
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}