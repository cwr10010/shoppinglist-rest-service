package de.cwrose.shoppinglist

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import org.springframework.context.MessageSource
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.lang.RuntimeException
import java.util.*
import javax.persistence.EntityNotFoundException

@ControllerAdvice
class ExceptionHandlers(var messageSource: MessageSource) {

    @ExceptionHandler(EntityNotFoundException::class, NoSuchElementException::class, EmptyResultDataAccessException::class)
    fun resourceNotFoundException(exception: RuntimeException, locale: Locale) = notFound("RESOURCE NOT FOUND")

    @ExceptionHandler(BadCredentialsException::class)
    fun badCredentialsException(exception: BadCredentialsException, locale: Locale): ErrorResponseEntity = badRequest(exception.message)

    @ExceptionHandler(ExpiredJwtException::class)
    fun expiredToken(exception: ExpiredJwtException, locale: Locale): ErrorResponseEntity = forbidden("TOKEN EXPIRED")
}

data class ErrorResponse(val status: HttpStatus, val error:String, val message:String?, val timestamp: Date, val bindingErrors: List<String>) {

    constructor(status: HttpStatus, message:String?) : this(status, status.reasonPhrase, message, Date(), listOf())
}

class ErrorResponseEntity(body: ErrorResponse): ResponseEntity<ErrorResponse>(body, body.status)

internal fun notFound(message:String) = ErrorResponseEntity(ErrorResponse(HttpStatus.NOT_FOUND, message))

internal fun badRequest(message: String?) = ErrorResponseEntity(ErrorResponse(HttpStatus.BAD_REQUEST, message))

internal fun forbidden(message: String) = ErrorResponseEntity(ErrorResponse(HttpStatus.FORBIDDEN, message))