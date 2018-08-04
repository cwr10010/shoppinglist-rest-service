package de.cwrose.shoppinglist

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import springfox.documentation.swagger2.annotations.EnableSwagger2

@SpringBootApplication
@EnableSwagger2
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}