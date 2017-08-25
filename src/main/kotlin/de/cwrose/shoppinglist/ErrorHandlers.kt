package de.cwrose.shoppinglist

import de.cwrose.shoppinglist.ErrorResponseEntity.Companion.notFound
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.util.*
import javax.persistence.EntityNotFoundException

@ControllerAdvice
class ExceptionHandlers @Autowired constructor(var messageSource: MessageSource) {

    @ExceptionHandler(EntityNotFoundException::class, NoSuchElementException::class)
    fun resourceNotFoundException(exception: EntityNotFoundException, locale: Locale) = notFound("NOT FOUND")

}

data class ErrorResponse(val status: HttpStatus
                         , val error:String
                         , val message:String
                         , val timestamp: Date
                         , val bindingErrors: List<String>) {

    constructor(status: HttpStatus, message:String, bindingErrors: List<String>) : this(status, status.reasonPhrase, message, Date(), bindingErrors)
    constructor(status: HttpStatus, error:String, message:String) : this(status, error, message, Date(), ArrayList<String>())
    constructor(status: HttpStatus, message:String) : this(status, status.reasonPhrase, message, Date(), ArrayList<String>())

}

class ErrorResponseEntity: ResponseEntity<ErrorResponse> {

    constructor(body:ErrorResponse) : super(body, body.status)
    constructor(body:ErrorResponse, headers: MultiValueMap<String, String>) : super(body, headers, body.status)


    companion object {

        fun badReqeust(message:String) = ErrorResponseEntity(ErrorResponse(HttpStatus.BAD_REQUEST, message))
        fun badReqeust(message:String, bindingErrors:List<String>) = ErrorResponseEntity(ErrorResponse(HttpStatus.BAD_REQUEST, message, bindingErrors))
        fun notFound(message:String) = ErrorResponseEntity(ErrorResponse(HttpStatus.NOT_FOUND, message))
        fun serverError(message:String) = ErrorResponseEntity(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message))

    }

}