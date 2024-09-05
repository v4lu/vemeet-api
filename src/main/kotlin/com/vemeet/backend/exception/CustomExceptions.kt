package com.vemeet.backend.exception

class ConflictException(message: String) : RuntimeException(message)
class ResourceNotFoundException(message: String) : RuntimeException(message)
class NotAllowedException(message: String) : RuntimeException(message)
class ConfirmationCodeExpiredException(message: String) : RuntimeException(message)
class InvalidConfirmationCodeException(message: String) : RuntimeException(message)
class InvalidCredentialsException(message: String) : RuntimeException(message)
class EmailAlreadyExistsException(message: String) : RuntimeException(message)