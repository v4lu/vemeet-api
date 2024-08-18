package com.vemeet.backend.exception

class BadRequestException(message: String) : RuntimeException(message)
class ResourceNotFoundException(message: String) : RuntimeException(message)
class UnauthorizedException(message: String) : RuntimeException(message)
class FileUploadException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class InvalidFileException(message: String) : RuntimeException(message)