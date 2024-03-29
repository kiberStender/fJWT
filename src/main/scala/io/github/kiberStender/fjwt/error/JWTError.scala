package io.github.kiberStender
package fjwt
package error

/** Enum to delimit and map all possible errors that the API can "throw"
  */
enum JWTError(val message: String) extends Throwable(message):
  case NotMappedError(errorMessage: String) extends JWTError(errorMessage)
  case ExpiredToken extends JWTError("Token is expired")
  case InvalidSignature extends JWTError("JWT Signature does not match")
  case NullPrivateKey extends JWTError("Private key cannot null")
  case EmptyPrivateKey extends JWTError("Private key cannot be empty")
  case NullToken extends JWTError("Token cannot null")
  case EmptyToken extends JWTError("Token cannot be empty")
  case Not2TokenParts extends JWTError("Token has less than 2 parts: header.payload")
  case Not3TokenParts extends JWTError("Token has less than 3 parts: header.payload.signature")
