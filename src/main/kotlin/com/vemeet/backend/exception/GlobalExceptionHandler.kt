package com.vemeet.backend.exception

import com.vemeet.backend.dto.ExceptionResponse
import com.vemeet.backend.dto.FieldException
import org.apache.coyote.BadRequestException
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException.BadRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.NoHandlerFoundException

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotConfirmedEmailException::class)
    fun handleUserNotConfirmedException(ex: NotConfirmedEmailException): ResponseEntity<ExceptionResponse> {
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.FORBIDDEN.value(),
            message = ex.message,
        )

        return ResponseEntity(errorResponse, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ExceptionResponse> {
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.CONFLICT.value(),
            message = ex.message,
        )

        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }


    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(ex: BadRequestException): ResponseEntity<ExceptionResponse> {
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.BAD_REQUEST.value(),
            message = ex.message,
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(BadRequest::class)
    fun handleBadRequest(ex: BadRequest): ResponseEntity<ExceptionResponse> {
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.BAD_REQUEST.value(),
            message = ex.message,
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ExceptionResponse> {
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.BAD_REQUEST.value(),
            message = ex.message,
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleAllUncaughtException(ex: Exception, request: WebRequest): ResponseEntity<ExceptionResponse> {
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "An unexpected error occurred"
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflictException(ex: ConflictException, request: WebRequest): ResponseEntity<ExceptionResponse> {
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.CONFLICT.value(),
            message = ex.message ?: "Bad Request"
        )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ExceptionResponse> {
        val errors = ex.bindingResult.fieldErrors.map {
            FieldException(it.field, it.defaultMessage ?: "Validation error")
        }
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.UNPROCESSABLE_ENTITY.value(),
            message = "Validation error",
            errors = errors
        )
        return ResponseEntity(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY)
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException, request: WebRequest): ResponseEntity<ExceptionResponse> {
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Resource Not Found"
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleRequestMethodNotSupportedException(ex: HttpRequestMethodNotSupportedException, request: WebRequest): ResponseEntity<ExceptionResponse> {
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.METHOD_NOT_ALLOWED.value(),
            message = "Method ${ex.method} not allowed"
        )
        return ResponseEntity(errorResponse, HttpStatus.METHOD_NOT_ALLOWED)
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(ex: NoHandlerFoundException, request: WebRequest): ResponseEntity<ExceptionResponse> {
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.NOT_FOUND.value(),
            message = "The requested resource was not found"
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(ex: BadCredentialsException, request: WebRequest): ResponseEntity<ExceptionResponse> {
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.UNAUTHORIZED.value(),
            message = "Wrong credentials"
        )

        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(NotAllowedException::class)
    fun handleNotAllowedException(ex: NotAllowedException, request: WebRequest): ResponseEntity<ExceptionResponse> {
        val exceptionResponse = ExceptionResponse(
            message = ex.message,
            statusCode = HttpStatus.FORBIDDEN.value(),
        )

        return ResponseEntity(exceptionResponse, HttpStatus.FORBIDDEN)
    }


    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentialsException(ex: InvalidCredentialsException): ResponseEntity<ExceptionResponse> {
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.UNAUTHORIZED.value(),
            message = ex.message ?: "Invalid credentials"
        )
        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(ConfirmationCodeExpiredException::class)
    fun handleConfirmationCodeExpiredException(ex: ConfirmationCodeExpiredException): ResponseEntity<ExceptionResponse> {
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ?: "Confirmation code has expired"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(InvalidConfirmationCodeException::class)
    fun handleInvalidConfirmationCodeException(ex: InvalidConfirmationCodeException): ResponseEntity<ExceptionResponse> {
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ?: "Invalid confirmation code"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }


    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExistsException(ex: EmailAlreadyExistsException): ResponseEntity<ExceptionResponse> {
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.CONFLICT.value(),
            message = ex.message ?: "Email already exists"
        )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException::class)
    fun handleInvalidDataAccessApiUsageException(ex: InvalidDataAccessApiUsageException): ResponseEntity<ExceptionResponse> {
       val errorResponse = ExceptionResponse(
           statusCode = HttpStatus.CONFLICT.value(),
           message = ex.message ?: "This action is not allowed"
       )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

}