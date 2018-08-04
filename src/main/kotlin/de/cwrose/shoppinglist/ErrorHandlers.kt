package de.cwrose.shoppinglist

import de.cwrose.shoppinglist.rest.BadReceiverException
import de.cwrose.shoppinglist.rest.UnknownReceiverException
import de.cwrose.shoppinglist.rest.UnknownShoppingListException
import io.jsonwebtoken.ExpiredJwtException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.lang.RuntimeException
import java.time.Instant
import java.util.Locale
import javax.persistence.EntityNotFoundException
import kotlin.IllegalArgumentException
import kotlin.NoSuchElementException
import kotlin.String

@ControllerAdvice
class ExceptionHandlers() {

    @ExceptionHandler(EntityNotFoundException::class, NoSuchElementException::class, EmptyResultDataAccessException::class, IllegalArgumentException::class)
    fun resourceNotFoundException(exception: RuntimeException, locale: Locale) = notFound("RESOURCE NOT FOUND")

    @ExceptionHandler(BadCredentialsException::class)
    fun badCredentialsException(exception: BadCredentialsException, locale: Locale): ErrorResponseEntity = badRequest(exception.message)

    @ExceptionHandler(ExpiredJwtException::class)
    fun expiredToken(exception: ExpiredJwtException, locale: Locale): ErrorResponseEntity = forbidden("TOKEN EXPIRED")

    @ExceptionHandler(UnknownReceiverException::class, UnknownShoppingListException::class, BadReceiverException::class)
    fun sharingFailed(exception: RuntimeException, locale: Locale) = badRequest(exception.message)
}

data class ErrorResponse(val status: HttpStatus, val error: String, val message: String?, val timestamp: Instant, val bindingErrors: List<String>) {

    constructor(status: HttpStatus, message: String?) : this(status, status.reasonPhrase, message, Instant.now(), listOf())
}

class ErrorResponseEntity(body: ErrorResponse) : ResponseEntity<ErrorResponse>(body, body.status)

internal fun notFound(message: String) = ErrorResponseEntity(ErrorResponse(HttpStatus.NOT_FOUND, message))

internal fun badRequest(message: String?) = ErrorResponseEntity(ErrorResponse(HttpStatus.BAD_REQUEST, message))

internal fun forbidden(message: String) = ErrorResponseEntity(ErrorResponse(HttpStatus.FORBIDDEN, message))
