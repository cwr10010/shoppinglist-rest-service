package de.cwrose.shoppinglist

import org.springframework.context.MessageSource
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.lang.RuntimeException
import java.util.*
import javax.persistence.EntityNotFoundException

@ControllerAdvice
class ExceptionHandlers(var messageSource: MessageSource) {

    @ExceptionHandler(EntityNotFoundException::class, NoSuchElementException::class, EmptyResultDataAccessException::class)
    fun resourceNotFoundException(exception: RuntimeException, locale: Locale) = notFound("RESOURCE NOT FOUND")
}

data class ErrorResponse(val status: HttpStatus, val error:String, val message:String, val timestamp: Date, val bindingErrors: List<String>) {

    constructor(status: HttpStatus, message:String) : this(status, status.reasonPhrase, message, Date(), ArrayList<String>())
}

class ErrorResponseEntity(body: ErrorResponse): ResponseEntity<ErrorResponse>(body, body.status)

internal fun notFound(message:String) = ErrorResponseEntity(ErrorResponse(HttpStatus.NOT_FOUND, message))