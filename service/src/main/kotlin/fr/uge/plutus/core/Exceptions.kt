package fr.uge.plutus.core

class ExpiredOrInvalidTokenException : RuntimeException("Expired or invalid token")

class MissingHeaderException(header: String) : RuntimeException("Missing header: $header")

class MultipartParseException(message: String = "Multipart parse error") : RuntimeException(message)
