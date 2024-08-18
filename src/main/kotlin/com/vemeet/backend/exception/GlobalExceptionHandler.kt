package com.vemeet.backend.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.NoHandlerFoundException

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