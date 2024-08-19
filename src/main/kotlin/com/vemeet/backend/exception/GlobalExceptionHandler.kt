package com.vemeet.backend.exception

import com.amazonaws.services.cognitoidp.model.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException

@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(ex: BadRequestException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            statusCode = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ?: "Bad Request"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            statusCode = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Resource Not Found"
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(ex: NoHandlerFoundException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            statusCode = HttpStatus.NOT_FOUND.value(),
            message = "The requested resource was not found"
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(ex: NoResourceFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            statusCode = HttpStatus.NOT_FOUND.value(),
            message = "The requested resource was not found",
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(ex: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            statusCode = HttpStatus.METHOD_NOT_ALLOWED.value(),
            message = "The requested method is not allowed for this endpoint",
        )
        return ResponseEntity(errorResponse, HttpStatus.METHOD_NOT_ALLOWED)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map {
            FieldError(it.field, it.defaultMessage ?: "Validation error")
        }
        val errorResponse = ErrorResponse(
            statusCode = HttpStatus.BAD_REQUEST.value(),
            message = "Validation error",
            errors = errors
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExistsException(ex: EmailAlreadyExistsException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            statusCode = HttpStatus.CONFLICT.value(),
            message = ex.message ?: "Email already exists"
        )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(ConfirmationCodeExpiredException::class)
    fun handleConfirmationCodeExpiredException(ex: ConfirmationCodeExpiredException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            statusCode = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ?: "Confirmation code has expired"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(InvalidConfirmationCodeException::class)
    fun handleInvalidConfirmationCodeException(ex: InvalidConfirmationCodeException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            statusCode = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ?: "Invalid confirmation code"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            statusCode = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "User not found"
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentialsException(ex: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            statusCode = HttpStatus.UNAUTHORIZED.value(),
            message = ex.message ?: "Invalid credentials"
        )
        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(ex: UnauthorizedException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            statusCode = HttpStatus.UNAUTHORIZED.value(),
            message = ex.message ?: "Unauthorized"
        )
        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(Exception::class)
    fun handleAllUncaughtException(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error", ex)
        val errorResponse = ErrorResponse(
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "An unexpected error occurred"
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

}